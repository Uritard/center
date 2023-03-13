package com.yjh.demo.config;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.messager.api.channel.RxBusMsgChannel;
import com.yjh.messager.api.msg.Msg;
import com.yjh.messager.api.socket.BaseSocketServer;
import com.yjh.protocol_a.IdentityProvider;
import com.yjh.protocol_a.MessageIdGenerator;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.SessionConnectionStateMonitor;
import com.yjh.protocol_a.impl.IdentityProviderImpl;
import com.yjh.protocol_a.impl.MessageCodec;
import com.yjh.protocol_a.impl.PacketCodecFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zilong
 * @since 2022/1/28
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */
@Configuration
public class ServerConfig {
    @Value("${tcp.port}")
    int tcpPort;

    @Bean
    public RxBus serverRxBus() {
        return new RxBus();
    }

    @Bean
    public ExecutorService serverOutboundExecutor() {
        return Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
    }

    @Bean
    public ExecutorService serverMsgProcessingExecutor() {
        return Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
    }

    @Bean
    public MsgChannel serverMsgChannel(ExecutorService serverOutboundExecutor, RxBus serverRxBus) {
        return new RxBusMsgChannel(
                serverRxBus,
                msg -> msg instanceof Msg.Outbound,
                new MessageCodec("PatrolHost", true),
                serverOutboundExecutor
        );
    }

    @Bean
    public BaseSocketServer socketServer(MsgChannel serverMsgChannel) {
        return new BaseSocketServer(
                "0.0.0.0", tcpPort, serverMsgChannel, new PacketCodecFactory(), 8
        );
    }

    @Bean
    MessageIdGenerator serverMessageIdGenerator() {
        return new MessageIdGenerator();
    }

    @Bean
    IdentityProvider serverIdentityProvider() {
        return new IdentityProviderImpl("Server001");
    }

    @Bean
    MessageSender serverMessageSender(
            MessageIdGenerator serverMessageIdGenerator,
            IdentityProvider serverIdentityProvider,
            RxBus serverRxBus
    ) {
        return new MessageSender(serverMessageIdGenerator, serverIdentityProvider, serverRxBus);
    }

    @Bean
    SessionConnectionStateMonitor sessionConnectionStateMonitor(
            RxBus serverRxBus,
            ExecutorService serverMsgProcessingExecutor,
            MessageIdGenerator serverMessageIdGenerator,
            IdentityProvider serverIdentityProvider
    ) {
        return new SessionConnectionStateMonitor(serverRxBus, serverMsgProcessingExecutor,
                serverMessageIdGenerator, serverIdentityProvider);
    }
}
