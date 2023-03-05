package com.yjh.demo.task;

import com.alibaba.fastjson.JSON;
import com.yjh.commons.DateFormat;
import com.yjh.commons.DateUtils;
import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.controller.DemoClientBatchController;
import com.yjh.demo.controller.DemoClientTaskContoller;
import com.yjh.demo.entity.BatchMessageParam;
import com.yjh.demo.handler.DemoBatchClientHandler;
import com.yjh.demo.util.FtpsUtil;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.messager.api.channel.RxBusMsgChannel;
import com.yjh.messager.api.msg.Msg;
import com.yjh.messager.api.socket.BaseSocketClient;
import com.yjh.protocol_a.*;
import com.yjh.protocol_a.impl.MessageCodec;
import com.yjh.protocol_a.impl.PacketCodecFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName: SendTask
 * @Description:
 * @author: yanhao
 * @date: 2022/8/27
 */
@Slf4j
public class SendTask implements Runnable {
    /**
     * 任务序号
     */
    private int index;
    private String ip;
    private String sendCode;
    private int ftpPort;
    private int socketPort;
    private String filePath;
    private String remoteFilename;
    private String username;
    private String password;
    private String keyPw;
    private String xml;
    private String suffix;
    private ExecutorService executorService;
    private MessageIdGenerator messageIdGenerator;
    private int messageCount;

    private byte[] data;

    private RxBus rxBus;

    private MsgChannel msgChannel;
    AtomicLong sessionId = new AtomicLong(100000);

    public SendTask(int index, String sendCode, BatchMessageParam batchMessageParam, ExecutorService executorService,
        MessageIdGenerator messageIdGenerator, byte[] data, RxBus rxBus, MsgChannel msgChannel) {
        this.index = index;
        this.sendCode = sendCode;
        this.ip = batchMessageParam.getIp();
        this.ftpPort = batchMessageParam.getFtpPort();
        this.socketPort = batchMessageParam.getSocketPort();
        this.filePath = batchMessageParam.getFilePath();
        this.remoteFilename = batchMessageParam.getRemoteFilename();
        this.username = batchMessageParam.getUsername();
        this.password = batchMessageParam.getPassword();
        this.keyPw = batchMessageParam.getKeyPw();
        this.messageCount = batchMessageParam.getTaskCount();
        this.xml = batchMessageParam.getXml();
        this.executorService = executorService;
        this.messageIdGenerator = messageIdGenerator;
        this.suffix = String.format("%06d", index);
        this.data = data;
        this.rxBus = rxBus;
        this.msgChannel = msgChannel;
    }

    @Override
    public void run() {
        String[] deviceIds = DemoClientBatchController.deviceArrayMap.get(sendCode);
        for (int i = 0; i < messageCount; i++) {
            try {
                long startTime = System.currentTimeMillis();
                /*String[] fileName = StringUtils.split(remoteFilename, ".");
                if (fileName == null || fileName.length <= 1) {
                    log.error("远程文件名错误  remoteFilename=" + remoteFilename);
                    return;
                }
                String realRmoteFileName = fileName[0] + suffix + "." + fileName[1];
                log.info("realRmoteFileName=" + realRmoteFileName);
                //上传文件
                FtpsUtil.putFile(data, realRmoteFileName, ip, ftpPort, keyPw, username, password);
                long uploadFileEndTime = System.currentTimeMillis();*/
                //发送消息
                String deviceId = deviceIds[i % deviceIds.length];
                sendMsg(deviceId);
                long endTime = System.currentTimeMillis();

                DemoClientBatchController.finishCount.incrementAndGet();
                log.info("任务 {}  耗时:{}ms deviceId: {}", index, endTime - startTime, deviceId);

                TimeUnit.MILLISECONDS.sleep(DemoClientTaskContoller.sleepTime);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

    }

    private void sendMsg(String deviceId) {
        long startTime = System.currentTimeMillis();
        //设置请求消息
        Message message = null;
        try {
            message = XmlToMessageUtil.decode(xml);
        } catch (Exception e) {
            log.error("xml格式有误", e);
            return;
        }
        //设置sendcode,receiveCode
        String sendCode = this.sendCode;
        message.setSendCode(sendCode);
        String receiveCode = message.getReceiveCode();
        message.setReceiveCode(receiveCode);

        long sid = generateMessage(message, deviceId);
        long uploadFileEndTime = System.currentTimeMillis();
        OutboundMessage msg = new OutboundMessage(message);
        msg.setSessionId(sid);

        MessageSender messageSender = DemoClientTaskContoller.getSender().get(sendCode);
        messageSender.send(msg);
        long endTime = System.currentTimeMillis();
        log.info("任务 {}  上传文件耗时:{}ms 发送消息: {} deviceId: {}", index, uploadFileEndTime - startTime, endTime - uploadFileEndTime, deviceId);
/*
        //发送命令
        BaseSocketClient socketClient = new BaseSocketClient(msgChannel, ip, socketPort, new Timer(), new PacketCodecFactory());
        socketClient.start();
        IdentityProvider identityProvider = new IdentityProvider() {
            @Override
            public String sendCode() {
                return sendCode;
            }

            @Override
            public String receiveCode(long l) {
                return receiveCode;
            }

            @Override
            public void setReceiveCode(long l, String s) {

            }

            @Override
            public void removeReceiveCode(long l) {

            }
        };
        MessageSender messageSender = new MessageSender(messageIdGenerator, identityProvider, rxBus, msg1 -> true);
        DemoBatchClientHandler demoBatchClientHandler = new DemoBatchClientHandler(executorService, rxBus, messageSender, message, socketClient, index);
        msg.setSessionId(demoBatchClientHandler.getSessionId());
        messageSender.send(msg);
        */
    }

    private long generateMessage(Message message, String deviceId) {
        long sid = sessionId.incrementAndGet();
        //设置返回item
        for (Map<String, Object> item : message.getItems()) {
            String taskCode = DemoClientBatchController.taskCodeMap.get(sendCode);
            String taskName = DemoClientBatchController.taskNameMap.get(sendCode);

            String filePath = (String)item.get("file_path");
            String date = DateUtils.dateToString(new Date(), DateFormat.YYYY_MM_DD);
            date = date.replace("-", "/");

            byte[] finalData = data;
            String remoteFile = sendCode + "/" + date + "/" + taskCode + "/CCD/" + deviceId + "_" + sendCode + "_" + sid + ".jpg";
            item.put("task_code", taskCode);
            item.put("task_name", taskName);
            item.put("device_id", deviceId);
            item.put("task_patrolled_id", taskCode + DateUtils.dateToString(new Date(), DateFormat.YYYYMMDDHHMMSS));
            item.put("file_path", remoteFile);

            FtpsUtil.putFile(finalData, remoteFile, ip, ftpPort, keyPw, username, password);

        }
        return sid;
    }
}
