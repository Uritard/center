/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.demo.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yjh.commons.NamedThreadFactory;
import com.yjh.demo.util.StringVariableUtil;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.Msg;
import com.yjh.protocol_a.InboundMessage;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.OutboundMessage;
import com.yjh.protocol_a.impl.MessageCodec;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/29
 * @since [产品/模块版本] （可选）
 */
public interface IMessageSender {
    ScheduledThreadPoolExecutor
        SCHEDULED_THREAD_POOLS = new ScheduledThreadPoolExecutor(4, new NamedThreadFactory("interval-scheduled"));


    default BaseMessage sendMessage(String xml, long msgId) {
        try {
            Message message = XmlToMessageUtil.decode(xml);

            InboundMessage inboundMessage = null;
            if (isResponse(message)) {
                if (msgId >= 0) {
                    inboundMessage = getReqMsg(msgId);
                }
            }
            StringVariableUtil.updateParams(message);
            return sendMessage(message, inboundMessage);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    default boolean isResponse(Message message) {
        return "251".equals(message.getType()) && StringUtils.equalsAny(message.getCommand(), "3", "4");
    }

    default BaseMessage sendMessage(Message message, InboundMessage inboundMessage) {
        OutboundMessage msg = new OutboundMessage(message);
        if (inboundMessage != null) {
            msg.setOriginReq(inboundMessage);
        }
        OutboundMessage baseMessage = sendMessage(msg);
        sendWs(baseMessage);
        return baseMessage;
    }

    OutboundMessage sendMessage(OutboundMessage outboundMessage);

    void sendWs(OutboundMessage outboundMessage);

    String sendCode();

    String receiveCode();

    MessageCodec getMessageCodec();

    void updateReqMsg(InboundMessage inboundMessage);

    InboundMessage getReqMsg(long msgId);
}
