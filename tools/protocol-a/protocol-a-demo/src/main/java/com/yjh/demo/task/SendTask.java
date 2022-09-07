package com.yjh.demo.task;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.controller.DemoClientBatchController;
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

import java.util.Timer;
import java.util.concurrent.ExecutorService;


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

    private byte[] data;

    private RxBus rxBus;

    private MsgChannel msgChannel;

    public SendTask(int index, BatchMessageParam batchMessageParam, ExecutorService executorService, MessageIdGenerator messageIdGenerator, byte[] data, RxBus rxBus, MsgChannel msgChannel) {
        this.index = index;
        this.ip = batchMessageParam.getIp();
        this.ftpPort = batchMessageParam.getFtpPort();
        this.socketPort = batchMessageParam.getSocketPort();
        this.filePath = batchMessageParam.getFilePath();
        this.remoteFilename = batchMessageParam.getRemoteFilename();
        this.username = batchMessageParam.getUsername();
        this.password = batchMessageParam.getPassword();
        this.keyPw = batchMessageParam.getKeyPw();
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
        long startTime = System.currentTimeMillis();
        String[] fileName = StringUtils.split(remoteFilename, ".");
        if (fileName == null || fileName.length <= 1) {
            log.error("远程文件名错误  remoteFilename=" + remoteFilename);
            return;
        }
        String realRmoteFileName = fileName[0] + suffix + "." + fileName[1];
        log.info("realRmoteFileName=" + realRmoteFileName);
        //上传文件
        FtpsUtil.putFile(data, realRmoteFileName, ip, ftpPort, keyPw, username, password);
        long uploadFileEndTime = System.currentTimeMillis();
        //发送消息
        sendMsg();
        long endTime = System.currentTimeMillis();

        DemoClientBatchController.finishCount.incrementAndGet();
        log.info("任务 {}  耗时:{}ms, 上传文件:{}ms, 发送消息 {}ms", index, endTime - startTime, uploadFileEndTime - startTime, endTime - uploadFileEndTime);

    }

    private void sendMsg() {
        //设置请求消息
        Message message = null;
        try {
            message = XmlToMessageUtil.decode(xml);
        } catch (Exception e) {
            log.error("xml格式有误", e);
            return;
        }
        //设置sendcode,receiveCode
        String sendCode = message.getSendCode() + suffix;
        message.setSendCode(sendCode);
        String receiveCode = message.getReceiveCode() + suffix;
        message.setReceiveCode(receiveCode);
        OutboundMessage msg = new OutboundMessage(message);


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
    }
}
