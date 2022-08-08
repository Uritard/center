package com.yjh.demo.controller;

import com.yjh.demo.entity.MessageParam;
import com.yjh.demo.handler.DemoClientHandler;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.demo.ws.message.AInterfaceMessage;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.messager.api.socket.BaseSocketClient;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.MessageIdGenerator;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.OutboundMessage;
import com.yjh.protocol_a.impl.PacketCodecFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.bridge.MessageUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.Timer;

/**
 * @author zilong
 * @since 2022/1/29
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */
@RestController
@RequestMapping("/demo-client")
@Slf4j
public class DemoClientController {

    private final MessageSender clientMessageSender;

    private final DemoClientHandler demoClientHandler;

    private final MsgChannel clientMsgChannel;

    private final Timer reconnectionTimer;

    private BaseSocketClient socketClient;

    private final SimpleMessageSender wsMessageSender;

    public DemoClientController(MessageSender clientMessageSender,
                                DemoClientHandler demoClientHandler,
                                MsgChannel clientMsgChannel,
                                Timer reconnectionTimer/*,
                                BaseSocketClient socketClient*/,
                                SimpleMessageSender wsMessageSender
                                ){
        this.clientMessageSender = clientMessageSender;
        this.demoClientHandler = demoClientHandler;
        this.clientMsgChannel = clientMsgChannel;
        this.reconnectionTimer = reconnectionTimer;
//        this.socketClient = socketClient;
        this.wsMessageSender = wsMessageSender;
    }

    @PostMapping(value = "/out-msg")
    public void sendOutMessage(@RequestBody MessageParam xml) {
        Message message = null;
        try {
            message = XmlToMessageUtil.decode(xml.getXml());
        }catch (Exception e){
            log.error("xml格式有误", e);
        }
        OutboundMessage msg = new OutboundMessage(message);
        msg.setSessionId(demoClientHandler.getSessionId());
        clientMessageSender.send(msg);
    }

    @PostMapping(value = "/create")
    public void createClient(@RequestParam(defaultValue = "10011") Integer port,
                             @RequestParam String ip){
//        socketClient.stop();
        socketClient = new BaseSocketClient(clientMsgChannel, ip, port, reconnectionTimer, new PacketCodecFactory());
        socketClient.start();
    }

    @PostMapping(value = "/destroy")
    public void destroyClient(){
        this.socketClient.stop();
    }

    @PostMapping(value = "/send-ws")
    public void sendWs(@RequestParam String xml){
        AInterfaceMessage.AInterfaceData data = new AInterfaceMessage.AInterfaceData(xml);
        wsMessageSender.send(new AInterfaceMessage(data));
    }


}
