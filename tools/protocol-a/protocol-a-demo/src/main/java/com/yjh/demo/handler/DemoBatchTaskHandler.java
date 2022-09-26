package com.yjh.demo.handler;

import com.alibaba.fastjson.JSON;
import com.yjh.commons.rxbus.RxBus;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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


    public AtomicLong taskPatrolledId = new AtomicLong(1000000000l);

    private int index;

    private String sendCode;

    private String receiveCode;

    private MessageSender sender;

    private SimpleMessageSender wsMessageSender;

    public static ExecutorService executorService = Executors.newFixedThreadPool(20);
    long sessionId = 0l;

    public DemoBatchTaskHandler(Executor messageProcessingExecutor, RxBus bus, MessageSender sender, SimpleMessageSender wsMessageSender, String sendCode, String receiveCode, int index) {
        super(messageProcessingExecutor, bus);
        this.sender = sender;
        this.wsMessageSender = wsMessageSender;
        this.sendCode = sendCode;
        this.receiveCode = receiveCode;
        this.index = index;
        subscribeInbound(ExtPeerState.class, InboundMessage.class);

    }

    @Override
    protected void handleMessage(BaseMessage baseMsg) {
        log.info("接收消息:sendCode:{}   sessionId:{}   index:{}  Recv Msg: {}", sendCode, sessionId, index, baseMsg);
        if (baseMsg instanceof ExtPeerState) {
            ExtPeerState peerState = (ExtPeerState) baseMsg;
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
            InboundMessage inboundMessage = (InboundMessage) baseMsg;
            this.sessionId = inboundMessage.getSessionId();
            log.info("接收到服务端的sessionId :{} sendCode {}  接收到服务端的内容: \n{}\n", sessionId, sendCode, new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8));
            String xml = new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8);
            String msg =
                    "发送会话序列号：" + inboundMessage.getPacket().getSendSessionId() + "        " +
                            "接收会话序列号：" + inboundMessage.getPacket().getReceiveSessionId() + "        " +
                            "会话源标识：0x0" + inboundMessage.getPacket().getSessionType() + "        " +
                            "xml内容：" + xml + "\n";
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
            return;
        }
        String[] fileName = org.springframework.util.StringUtils.split(DemoClientTaskContoller.localFilePath, ".");
        if (fileName == null || fileName.length <= 1) {
            log.error("文件名错误  Filename=" + DemoClientTaskContoller.localFilePath);
            return;
        }
        byte[] data;
        try {
            data = IOUtils.toByteArray(Files.newInputStream(Paths.get(DemoClientTaskContoller.localFilePath)));
        } catch (Exception e) {
            log.error("文件 解析失败", e);
            return;
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

            Map<String, Object> taskResponseItem = taskResponseMsg.getItems().get(0);
            taskResponseItem.put("task_code", taskCode);
            taskResponseItem.put("task_name", taskName);

            byte[] finalData = data;
            for (String deviceId : deviceArray) {
                taskResponseItem.put("device_id", deviceId);
                taskResponseItem.put("task_patrolled_id", taskPatrolledId.getAndIncrement());
                taskResponseItem.put("file_path",taskCode + "/" + deviceId + "." + fileName[1]);
                //深复制 防止消息错误
                String jsonStr = JSON.toJSONString(taskResponseMsg);
                Message taskResponseMsgClone = JSON.parseObject(jsonStr, Message.class);
                OutboundMessage outboundMessage = new OutboundMessage(taskResponseMsgClone);
                outboundMessage.setSessionId(sessionId);
                executorService.submit(() -> {
                    FtpsUtil.putFile(finalData, taskCode + "/" + deviceId + "." + fileName[1], DemoClientTaskContoller.ip, DemoClientTaskContoller.ftpPort, DemoClientTaskContoller.keyPw, DemoClientTaskContoller.username, DemoClientTaskContoller.password);
                    sender.send(outboundMessage);
                    log.info("任务发送响应：taskResponse:{}", taskResponseMsgClone);
                });

            }
        }
    }

    public Long getSessionId() {
        return sessionId;
    }


}
