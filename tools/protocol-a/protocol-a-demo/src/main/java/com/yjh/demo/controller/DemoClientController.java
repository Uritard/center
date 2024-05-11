package com.yjh.demo.controller;

import com.yjh.demo.entity.MessageParam;
import com.yjh.demo.entity.ResultBean;
import com.yjh.demo.service.ClientService;
import com.yjh.demo.task.AutomationTask;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.demo.ws.message.AInterfaceMessage;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.messager.api.socket.BaseSocketClient;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.OutboundMessage;
import com.yjh.protocol_a.impl.PacketCodecFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * @author zilong
 * @since 2022/1/29
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */
@RestController
@RequestMapping("/demo-client")
@Slf4j
@RequiredArgsConstructor
public class DemoClientController {

    private final ClientService clientService;
    private final SimpleMessageSender wsMessageSender;
    private final AutomationTask automationTask;


    @PostMapping(value = "/out-msg")
    public ResultBean sendOutMessage(@RequestBody MessageParam xml) {
        try {
            clientService.sendMessage(xml.getXml(), xml.getMsgId());
            return new ResultBean(200, "success");
        }catch (Exception e){
            log.error("xml格式有误", e);
            return new ResultBean(500, "error");
        }
    }

    @PostMapping(value = "/create")
    public ResultBean createClient(@RequestParam String station, @RequestParam String platform,
        @RequestParam(defaultValue = "10011") Integer port, @RequestParam String ip, @RequestParam(required = false) String sendCode,
        @RequestParam(required = false) String receiveCode) {
        try {
            String rootTag = automationTask.startProtocolTask(station, platform, false);
            return clientService.createClient(port, ip, sendCode, receiveCode, rootTag);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return new ResultBean(500, "error");
        }
    }

    @PostMapping(value = "/destroy")
    public ResultBean destroyClient(){
        clientService.stop();
        return new ResultBean(200, "success");
    }

    @PostMapping(value = "/send-ws")
    public ResultBean sendWs(@RequestParam String xml){
        AInterfaceMessage.AInterfaceData data = new AInterfaceMessage.AInterfaceData(xml, "");
        wsMessageSender.send(new AInterfaceMessage(data));

        return new ResultBean(200, "success");
    }


}
