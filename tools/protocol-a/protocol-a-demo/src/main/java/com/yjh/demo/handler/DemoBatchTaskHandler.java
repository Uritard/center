package com.yjh.demo.handler;

import com.alibaba.fastjson.JSON;
import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.config.ClientConfig;
import com.yjh.demo.controller.DemoClientBatchController;
import com.yjh.demo.controller.DemoClientTaskContoller;
import com.yjh.demo.util.FtpsUtil;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.demo.ws.message.BatchTaskMessage;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.BaseMessageHandler;
import com.yjh.messager.api.msg.ExtPeerState;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.protocol_a.InboundMessage;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.OutboundMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName: DemoBatchTaskHandler
 * @Description:
 * @author: yanhao
 * @date: 2022/9/7
 */
@Slf4j
public class DemoBatchTaskHandler extends BaseMessageHandler {

    public AtomicLong taskPatrolledId = new AtomicLong(1000000000L);

    private final int index;

    private final String sendCode;

    private final String receiveCode;

    private final MessageSender sender;

    private final SimpleMessageSender wsMessageSender;

    private final int threadCount;

    public static final byte[] lock = new byte[0];
    public static volatile ExecutorService executorService = null;
    long sessionId = 0L;

    public DemoBatchTaskHandler(Executor messageProcessingExecutor, RxBus bus, MessageSender sender, SimpleMessageSender wsMessageSender,
        String sendCode, String receiveCode, int index, int threadCount) {
        super(messageProcessingExecutor, bus);
        this.sender = sender;
        this.wsMessageSender = wsMessageSender;
        this.sendCode = sendCode;
        this.receiveCode = receiveCode;
        this.index = index;
        this.threadCount = threadCount;
        if (executorService == null) {
            synchronized (lock) {
                if (executorService == null) {
                    executorService = Executors.newFixedThreadPool(threadCount);
                    log.info("Executors pool: {}", executorService);
                }
            }
        }
        subscribeInbound(ExtPeerState.class, InboundMessage.class);

    }

    @Override
    protected void handleMessage(BaseMessage baseMsg) {
        log.info("接收消息:sendCode:{}   sessionId:{}   index:{}  Recv Msg: {}", sendCode, sessionId, index, baseMsg);
        if (baseMsg instanceof ExtPeerState) {
            ExtPeerState peerState = (ExtPeerState)baseMsg;
            if (peerState.getData().isConnected()) {
                Message msg = new Message();
                msg.setType("251");
                msg.setCommand("1");
                msg.setSendCode(sendCode);
                msg.setReceiveCode(receiveCode);
                OutboundMessage outboundMessage = new OutboundMessage(msg);
                outboundMessage.setSessionId(peerState.getSessionId());
                sender.send(outboundMessage);
            }
        } else if (baseMsg instanceof InboundMessage) {
            InboundMessage inboundMessage = (InboundMessage)baseMsg;
            this.sessionId = inboundMessage.getSessionId();
            log.info("接收到服务端的sessionId :{} sendCode {}  接收到服务端的内容: \n{}\n", sessionId, sendCode,
                new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8));
            String xml = new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8);
            String msg = "发送会话序列号：" + inboundMessage.getPacket().getSendSessionId() + "        " + "接收会话序列号：" + inboundMessage.getPacket()
                .getReceiveSessionId() + "        " + "会话源标识：0x0" + inboundMessage.getPacket().getSessionType() + "        " + "xml内容："
                + xml + "\n";
            BatchTaskMessage batchTaskMessage = new BatchTaskMessage();
            batchTaskMessage.setIndex(index);
            batchTaskMessage.setMsg(msg);
            wsMessageSender.send(batchTaskMessage);
            //String转XML
            Message message = null;
            try {
                message = XmlToMessageUtil.decode(xml);
            } catch (Exception e) {
                log.error("xml 格式解析失败 xml=" + xml, e);
                return;
            }
            //如果是任务需要返回
            if (StringUtils.equals("101", message.getType()) && StringUtils.equals("1", message.getCommand())) {
                sendTaskResult(message);
            }

        }
    }

    private void sendTaskResult(Message message) {
        // 获取前端配置的任务响应报文
        Message taskResponseMsg = null;
        try {
            taskResponseMsg = XmlToMessageUtil.decode(DemoClientTaskContoller.taskXml);
        } catch (Exception e) {
            log.error("xml 解析失败", e);
            taskResponseMsg = new Message();
        }
        /*String[] fileName = StringUtils.split(DemoClientTaskContoller.localFilePath, ".");
        if (fileName == null || fileName.length <= 1) {
            log.error("文件名错误  Filename=" + DemoClientTaskContoller.localFilePath);
            return;
        }*/
        byte[][] data = null;
        String[] fileNames = null;
        int len = 1;
        try {
            File file = Paths.get(DemoClientTaskContoller.localFilePath).toFile();
            if (!file.exists()) {
                log.error("文件不存在  Filename={}", DemoClientTaskContoller.localFilePath);
            }
            if (!file.isDirectory()) {
                data = new byte[][] {IOUtils.toByteArray(Files.newInputStream(file.toPath()))};
                fileNames = new String[] {StringUtils.substringAfterLast(file.getName(), ".")};
            } else {
                File[] files = file.listFiles((dir, name) -> StringUtils.endsWith(name, ".jpg"));
                if (files == null || files.length == 0) {
                    log.error("文件夹下不存在jpg图片  Filename={}", DemoClientTaskContoller.localFilePath);
                }
                len = files.length;
                data = new byte[len][];
                fileNames = new String[len];
                for (int i = 0; i < len; i++) {
                    data[i] = IOUtils.toByteArray(Files.newInputStream(files[i].toPath()));
                    fileNames[i] = StringUtils.substringAfterLast(files[i].getName(), ".");
                }
            }
        } catch (Exception e) {
            log.error("文件 解析失败", e);
            if (!ClientConfig.sendBatch) {
                return;
            }
        }

        //设置返回 receiveCode  sendCode
        taskResponseMsg.setReceiveCode(receiveCode);
        taskResponseMsg.setSendCode(sendCode);

        //设置返回item
        for (Map<String, Object> item : message.getItems()) {
            String taskCode = String.valueOf(item.get("task_code"));
            String taskName = String.valueOf(item.get("task_name"));
            String deviceList = item.get("device_list").toString();
            String[] deviceArray = StringUtils.split(deviceList, ",");
            if (ClientConfig.sendBatch) {
                DemoClientBatchController.deviceArrayMap.put(sendCode, deviceArray);
                DemoClientBatchController.taskCodeMap.put(sendCode, taskCode);
                DemoClientBatchController.taskNameMap.put(sendCode, taskName);
                continue;
            }

            Map<String, Object> taskResponseItem = taskResponseMsg.getItems().get(0);
            taskResponseItem.put("task_code", taskCode);
            taskResponseItem.put("task_name", taskName);
            assert data != null;
            assert fileNames != null;
            int i = 0;
            for (String deviceId : deviceArray) {
                int idx = i++ % data.length;
                byte[] finalData = data[idx];
                String fileName = fileNames[idx];
                String remoteFile = sendCode + "/" + taskCode + "/" + deviceId + "." + fileName;
                taskResponseItem.put("device_id", deviceId);
                taskResponseItem.put("task_patrolled_id", taskPatrolledId.getAndIncrement());
                taskResponseItem.put("file_path", remoteFile);
                //深复制 防止消息错误
                String jsonStr = JSON.toJSONString(taskResponseMsg);
                Message taskResponseMsgClone = JSON.parseObject(jsonStr, Message.class);
                OutboundMessage outboundMessage = new OutboundMessage(taskResponseMsgClone);
                outboundMessage.setSessionId(sessionId);
                executorService.execute(() -> {
                    uplaodAndResponse(remoteFile, taskCode, finalData, deviceId, taskResponseMsgClone, outboundMessage);
                });
                if (DemoClientTaskContoller.sleepTime > 0 && i > threadCount) {
                    try {
                        Thread.sleep(DemoClientTaskContoller.sleepTime);
                    } catch (InterruptedException e) {
                        log.error("sleep error", e);
                    }
                }
            }
        }
    }

    private void uplaodAndResponse(String remoteFile, String taskCode, byte[] finalData, String deviceId, Message taskResponseMsgClone,
        OutboundMessage outboundMessage) {

        FtpsUtil.putFile(finalData, remoteFile, DemoClientTaskContoller.ip, DemoClientTaskContoller.ftpPort, DemoClientTaskContoller.keyPw,
            DemoClientTaskContoller.username, DemoClientTaskContoller.password);
        sender.send(outboundMessage);
        log.info("任务发送响应：taskResponse:{}", taskResponseMsgClone);
    }

    public Long getSessionId() {
        return sessionId;
    }

}
