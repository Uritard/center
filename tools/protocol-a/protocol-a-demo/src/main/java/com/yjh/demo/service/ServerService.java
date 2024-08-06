/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.demo.service;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.config.ServerConfig;
import com.yjh.demo.entity.ResultBean;
import com.yjh.demo.ws.message.AInterfaceMessage;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.messager.api.channel.RxBusMsgChannel;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.Msg;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.messager.api.socket.BaseSocketServer;
import com.yjh.protocol_a.InboundMessage;
import com.yjh.protocol_a.MessageIdGenerator;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.OutboundMessage;
import com.yjh.protocol_a.impl.IdentityProviderImpl;
import com.yjh.protocol_a.impl.MessageCodec;
import com.yjh.protocol_a.impl.MessageCodecHandler;
import com.yjh.protocol_a.impl.PacketCodecFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;
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
public class ServerService implements IMessageSender {

    private final MessageIdGenerator serverMessageIdGenerator;
    @Qualifier("serverRxBus")
    private final RxBus serverRxBus;
    private final ExecutorService serverOutboundExecutor;
    private final ServerConfig serverConfig;
    private final SimpleMessageSender wsMessageSender;

    private MessageSender serverMessageSender;
    private BaseSocketServer socketServer;
    private MessageCodec messageCodec;

    private volatile long REQ_MSG_ID;
    private final Cache<Long, InboundMessage> REQ_MSG_CACHE =
        CacheBuilder.newBuilder().expireAfterWrite(Duration.ofSeconds(30)).maximumSize(10000).build();

    /**
     *
     */
    public ResultBean createServer(Integer port, String sendCode, String rootTag) throws IOException {

        if (socketServer != null) {
            socketServer.destroy();
        }
        serverConfig.setSendCode(sendCode);

        serverMessageSender = new MessageSender(serverMessageIdGenerator, new IdentityProviderImpl(sendCode), serverRxBus);

        messageCodec = new MessageCodec(rootTag, true);
        MsgChannel serverMsgChannel =
            new RxBusMsgChannel(serverRxBus, msg -> msg instanceof Msg.Outbound, messageCodec, serverOutboundExecutor);

        socketServer = new BaseSocketServer("0.0.0.0", port, serverMsgChannel, new PacketCodecFactory(), 8);
        socketServer.start();
        return new ResultBean(200, "success");

    }

    public void destroy() {
        if (socketServer != null) {
            try {
                socketServer.destroy();
                socketServer = null;
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @Override
    public OutboundMessage sendMessage(OutboundMessage msg) {
        msg.setSessionId(ServerConfig.sessionId);
        serverMessageSender.send(msg);
        return msg;
    }

    @Override
    public void sendWs(OutboundMessage outboundMessage) {
        String xml = MessageCodecHandler.encodeXml(getMessageCodec(), outboundMessage.getMsg());
        InboundMessage inboundMessage = (InboundMessage)outboundMessage.getOriginReq();
        // 打印发送日志
        AInterfaceMessage.AInterfaceData data = new AInterfaceMessage.AInterfaceData(
            "<<<<<< 给客户端发送会话序列号：" + outboundMessage.getMsgId() + "        " + "接收会话序列号：" + (Optional.ofNullable(
                inboundMessage).map(BaseMessage::getMsgId).orElse(0L)) + "        " + "会话源标识：0x0" + (inboundMessage != null ? 1 : 0)
                + "        " + "xml内容：\n" + xml, "");
        wsMessageSender.send(new AInterfaceMessage(data));
    }

    @Override
    public String sendCode() {
        return serverConfig.getSendCode();
    }

    @Override
    public String receiveCode() {
        return serverConfig.getReceiveCode();
    }

    @Override
    public MessageCodec getMessageCodec() {
        return messageCodec;
    }

    public void setReceiveCode(String receiveCode) {
        serverConfig.setReceiveCode(receiveCode);
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
