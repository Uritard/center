package com.yjh.demo.config;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.messager.api.channel.RxBusMsgChannel;
import com.yjh.messager.api.msg.Msg;
import com.yjh.messager.api.socket.BaseSocketClient;
import com.yjh.protocol_a.IdentityProvider;
import com.yjh.protocol_a.MessageIdGenerator;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.impl.MessageCodec;
import com.yjh.protocol_a.impl.PacketCodecFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author zilong
 * @since 2022/1/28
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */
@Configuration
public class ClientConfig {

    @Value("${platform.ip}")
    String platformIp;

    @Value("${platform.port}")
    int platformPort;

    @Value("${platform.send-code}")
    String sendCode;

    @Value("${platform.receive-code}")
    String receiveCode;

    public static boolean sendBatch;

    @Value("${send.batch:true}")
    public void setSendBatch(boolean sendBatch) {
        ClientConfig.sendBatch = sendBatch;
    }

    @Bean
    public RxBus clientRxBus() {
        return new RxBus();
    }

    @Bean
    public ExecutorService clientOutboundExecutor() {
        return Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
    }

    @Bean
    public ExecutorService clientMsgProcessingExecutor() {
        return Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
    }

    @Bean
    public MsgChannel clientMsgChannel(ExecutorService clientOutboundExecutor, RxBus clientRxBus) {
        return new RxBusMsgChannel(
                clientRxBus,
                msg -> msg instanceof Msg.Outbound,
                new MessageCodec("PatrolDevice", true),
                clientOutboundExecutor
        );
    }

    @Bean
    public Timer reconnectionTimer() {
        return new Timer();
    }

//    @Bean
//    public BaseSocketClient socketClient(MsgChannel clientMsgChannel, Timer reconnectionTimer) {
//        return new BaseSocketClient(clientMsgChannel, platformIp, platformPort, reconnectionTimer, new PacketCodecFactory());
//    }

    @Bean
    MessageIdGenerator clientMessageIdGenerator() {
        return new MessageIdGenerator();
    }

    @Bean
    IdentityProvider clientIdentityProvider() {
        return new IdentityProvider() {

            @Override
            public String sendCode() {
                return sendCode;
            }

            @Override
            public String receiveCode(long receiverId) {
                return receiveCode;
            }

            @Override
            public void setReceiveCode(long receiverSessionId, String receiveCode) {

            }

            @Override
            public void removeReceiveCode(long receiverSessionId) {

            }
        };
    }

    @Bean
    MessageSender clientMessageSender(
            MessageIdGenerator clientMessageIdGenerator,
            IdentityProvider clientIdentityProvider,
            RxBus clientRxBus
    ) {
        return new MessageSender(clientMessageIdGenerator, clientIdentityProvider, clientRxBus, msg -> true);
    }
}
