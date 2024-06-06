/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.demo.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.config.ClientConfig;
import com.yjh.demo.entity.ResultBean;
import com.yjh.demo.ws.message.AInterfaceMessage;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.messager.api.channel.RxBusMsgChannel;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.Msg;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.messager.api.socket.BaseSocketClient;
import com.yjh.protocol_a.InboundMessage;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.OutboundMessage;
import com.yjh.protocol_a.impl.MessageCodec;
import com.yjh.protocol_a.impl.MessageCodecHandler;
import com.yjh.protocol_a.impl.PacketCodecFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.ExecutorService;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/29
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClientService implements IMessageSender {
    private final MessageSender clientMessageSender;
    @Qualifier("clientRxBus")
    private final RxBus clientRxBus;
    private final ExecutorService clientOutboundExecutor;
    private final Timer reconnectionTimer;
    private final ClientConfig clientConfig;
    private final SimpleMessageSender wsMessageSender;

    private BaseSocketClient socketClient;
    private MessageCodec messageCodec;

    private volatile long REQ_MSG_ID;
    private final Cache<Long, InboundMessage> REQ_MSG_CACHE =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofMinutes(30)).maximumSize(10000).build();

    /**
     *
     */
    public ResultBean createClient(Integer port, String ip, String sendCode, String receiveCode, String rootTag) {
        if (socketClient != null) {
            socketClient.stop();
        }
        clientConfig.setSendCode(sendCode, receiveCode);

        messageCodec = new MessageCodec(rootTag, true);
        MsgChannel clientMsgChannel =
            new RxBusMsgChannel(clientRxBus, msg -> msg instanceof Msg.Outbound, messageCodec, clientOutboundExecutor);

        socketClient = new BaseSocketClient(clientMsgChannel, ip, port, reconnectionTimer, new PacketCodecFactory());
        socketClient.start();
        return new ResultBean(200, "success");
    }

    public void stop() {
        if (socketClient != null) {
            try {
                socketClient.stop();
                socketClient = null;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public OutboundMessage sendMessage(OutboundMessage msg) {
        msg.setSessionId(ClientConfig.sessionId);
        clientMessageSender.send(msg);
        return msg;
    }

    @Override
    public void sendWs(OutboundMessage outboundMessage) {
        String xml = MessageCodecHandler.encodeXml(getMessageCodec(), outboundMessage.getMsg());
        InboundMessage inboundMessage = (InboundMessage)outboundMessage.getOriginReq();
        // 打印发送日志
        AInterfaceMessage.AInterfaceData data = new AInterfaceMessage.AInterfaceData(
            "<<<<<< 给服务端发送会话序列号：" + outboundMessage.getMsgId() + "        " + "接收会话序列号：" + (Optional.ofNullable(
                inboundMessage).map(BaseMessage::getMsgId).orElse(0L)) + "        " + "会话源标识：0x0" + (inboundMessage != null ? 1 : 0)
                + "        " + "xml内容：\n" + xml, "");
        wsMessageSender.send(new AInterfaceMessage(data));
    }

    @Override
    public String sendCode() {
        return clientConfig.getSendCode();
    }

    @Override
    public String receiveCode() {
        return clientConfig.getReceiveCode();
    }

    @Override
    public MessageCodec getMessageCodec() {
        return messageCodec;
    }

    @Override
    public void updateReqMsg(InboundMessage inboundMessage) {
        long msgId = (long)inboundMessage.getMsgId();
        REQ_MSG_ID = msgId;
        REQ_MSG_CACHE.put(msgId, inboundMessage);
    }

    @Override
    public InboundMessage getReqMsg(long msgId) {
        InboundMessage msg = null;
        if (msgId >= 0) {
            msg = REQ_MSG_CACHE.getIfPresent(msgId);
        }
        if (msg == null) {
            msg = REQ_MSG_CACHE.getIfPresent(REQ_MSG_ID);
        }
        return msg;
    }

}
