package com.yjh.demo.controller;

import com.yjh.demo.config.ClientConfig;
import com.yjh.demo.entity.MessageParam;
import com.yjh.demo.entity.ResultBean;
import com.yjh.demo.handler.DemoClientHandler;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.demo.ws.message.AInterfaceMessage;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.messager.api.socket.BaseSocketClient;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.OutboundMessage;
import com.yjh.protocol_a.impl.PacketCodecFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    private final ClientConfig clientConfig;

    public DemoClientController(MessageSender clientMessageSender, DemoClientHandler demoClientHandler, MsgChannel clientMsgChannel, Timer reconnectionTimer/*,
                                BaseSocketClient socketClient*/, SimpleMessageSender wsMessageSender, ClientConfig clientConfig){
        this.clientMessageSender = clientMessageSender;
        this.demoClientHandler = demoClientHandler;
        this.clientMsgChannel = clientMsgChannel;
        this.reconnectionTimer = reconnectionTimer;
//        this.socketClient = socketClient;
        this.wsMessageSender = wsMessageSender;
        this.clientConfig = clientConfig;
    }

    @PostMapping(value = "/out-msg")
    public ResultBean sendOutMessage(@RequestBody MessageParam xml) {
        Message message = null;
        try {
            message = XmlToMessageUtil.decode(xml.getXml());
        }catch (Exception e){
            log.error("xml格式有误", e);
        }
        OutboundMessage msg = new OutboundMessage(message);
        msg.setSessionId(demoClientHandler.getSessionId());
        clientMessageSender.send(msg);
        return new ResultBean(200, "success");
    }

    @PostMapping(value = "/create")
    public ResultBean createClient(@RequestParam(defaultValue = "10011") Integer port,
                             @RequestParam String ip, @RequestParam(required = false) String sendCode,
                             @RequestParam(required = false) String receiveCode){
//        socketClient.stop();
        clientConfig.setSendCode(sendCode, receiveCode);
        socketClient = new BaseSocketClient(clientMsgChannel, ip, port, reconnectionTimer, new PacketCodecFactory());
        socketClient.start();
        return new ResultBean(200, "success");
    }

    @PostMapping(value = "/destroy")
    public ResultBean destroyClient(){
        this.socketClient.stop();
        return new ResultBean(200, "success");
    }

    @PostMapping(value = "/send-ws")
    public ResultBean sendWs(@RequestParam String xml){
        AInterfaceMessage.AInterfaceData data = new AInterfaceMessage.AInterfaceData(xml);
        wsMessageSender.send(new AInterfaceMessage(data));

        return new ResultBean(200, "success");
    }


}
