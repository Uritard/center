package com.yjh.demo.controller;

import com.yjh.demo.entity.MessageParam;
import com.yjh.demo.handler.DemoServerHandler;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.MessageIdGenerator;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.OutboundMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author zilong
 * @since 2022/1/29
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */
@RestController
@RequestMapping("/demo-server")
@RequiredArgsConstructor
@Slf4j
public class DemoServerController {

    private final MessageSender serverMessageSender;
    private final DemoServerHandler demoServerHandler;

    @PostMapping(value = "/out-msg")
    public void sendOutMessage(@RequestBody MessageParam xml) {
        Message message = null;
        try {
            message = XmlToMessageUtil.decode(xml.getXml());
        }catch (Exception e){
            log.error("xml格式有误", e);
        }
        OutboundMessage msg = new OutboundMessage(message);
        msg.setSessionId(demoServerHandler.getSessionId());
        serverMessageSender.send(msg);
    }

}
