package com.yjh.demo.controller;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.entity.BatchClientParam;
import com.yjh.demo.entity.MessageParam;
import com.yjh.demo.handler.DemoBatchTaskHandler;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.messager.api.channel.RxBusMsgChannel;
import com.yjh.messager.api.msg.Msg;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.messager.api.socket.BaseSocketClient;
import com.yjh.protocol_a.*;
import com.yjh.protocol_a.impl.MessageCodec;
import com.yjh.protocol_a.impl.PacketCodecFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName: DemoClientTaskContoller
 * @Description:
 * @author: yanhao
 * @date: 2022/9/7
 */
@RestController
@RequestMapping("/demo-client-task")
@Slf4j
public class DemoClientTaskContoller {

    public static String taskXml;

    private List<BaseSocketClient> baseSocketClientList = new ArrayList<>();


    private List<String> sendCodeList = new ArrayList<>();

    private List<MessageSender> messageSenderList = new ArrayList<>();

    private List<DemoBatchTaskHandler> demoBatchTaskHandlerList = new ArrayList<>();
    private String receiveCode;

    @Autowired
    private RxBus clientBatchRxBus;

    @Autowired
    private MsgChannel clientBatchMsgChannel;

    @Autowired
    private ExecutorService clientBatchOutboundExecutor;

    @Autowired
    private SimpleMessageSender wsMessageSender;

    public static void main(String[] args) {
        ExecutorService clientBatchOutboundExecutor = Executors.newSingleThreadExecutor();
        for (int i = 0; i < 5; i++) {
            RxBus rxBus = new RxBus();
            MsgChannel msgChannel = new RxBusMsgChannel(
                    rxBus,
                    msg -> msg instanceof Msg.Outbound,
                    new MessageCodec("PatrolDevice", true),
                    clientBatchOutboundExecutor
            );
            BaseSocketClient socketClient = new BaseSocketClient(msgChannel, "192.168.33.20", 10011, new Timer(), new PacketCodecFactory());
            socketClient.start();
        }
        System.out.println("ok");
    }

    @PostMapping(value = "/create")
    public String createClient(@RequestBody BatchClientParam batchClientParam) {
        baseSocketClientList.forEach(BaseSocketClient::stop);
        baseSocketClientList.clear();
        messageSenderList.clear();
        sendCodeList.clear();
        demoBatchTaskHandlerList.clear();
        receiveCode = batchClientParam.getReceiveCode();
        sendCodeList = batchClientParam.getSendCodeList();
        for (int i = 0; i < sendCodeList.size(); i++) {
            String sendCode = sendCodeList.get(i);
            RxBus rxBus = new RxBus();
            MsgChannel msgChannel = new RxBusMsgChannel(
                    rxBus,
                    msg -> msg instanceof Msg.Outbound,
                    new MessageCodec("PatrolDevice", true),
                    clientBatchOutboundExecutor
            );
            BaseSocketClient socketClient = new BaseSocketClient(msgChannel, batchClientParam.getIp(), batchClientParam.getPort(), new Timer(), new PacketCodecFactory());
            socketClient.start();
            baseSocketClientList.add(socketClient);
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
            MessageSender messageSender = new MessageSender(new MessageIdGenerator(), identityProvider, rxBus, msg1 -> true);
            messageSenderList.add(messageSender);
            DemoBatchTaskHandler demoBatchTaskHandler = new DemoBatchTaskHandler(clientBatchOutboundExecutor, rxBus, messageSender, wsMessageSender, sendCode, receiveCode, i);
            demoBatchTaskHandlerList.add(demoBatchTaskHandler);
        }
        return "ok";
    }

    @GetMapping(value = "/destroy")
    public String destroyClient() {
        if (!baseSocketClientList.isEmpty()) {
            for (BaseSocketClient socketClient : baseSocketClientList) {
                socketClient.stop();
            }
        }
        baseSocketClientList.clear();
        messageSenderList.clear();
        sendCodeList.clear();
        demoBatchTaskHandlerList.clear();
        return "ok";
    }


    @PostMapping("/batchsend")
    public String sendMsg(@RequestBody MessageParam messageParam) {
        //设置请求消息
        Message message = null;
        try {
            message = XmlToMessageUtil.decode(messageParam.getXml());
        } catch (Exception e) {
            log.error("xml格式有误", e);
            return "error";
        }
        for (int i = 0; i < messageSenderList.size(); i++) {
            Message sendMessage = new Message();
            sendMessage.setSendCode(sendCodeList.get(i));
            sendMessage.setReceiveCode(receiveCode);
            sendMessage.setCommand(message.getCommand());
            sendMessage.setCode(message.getCode());
            sendMessage.setTime(message.getTime());
            sendMessage.setType(message.getType());
            sendMessage.setItems(message.getItems());
            OutboundMessage outboundMessage = new OutboundMessage(sendMessage);
            outboundMessage.setSessionId(demoBatchTaskHandlerList.get(i).getSessionId());
            MessageSender messageSender = messageSenderList.get(i);
            messageSender.send(outboundMessage);
        }
        return "ok";
    }

    @PostMapping("/settaskresult")
    public String setTaskResult(@RequestBody MessageParam messageParam) {
        taskXml = messageParam.getXml();
        return "ok";
    }


}
