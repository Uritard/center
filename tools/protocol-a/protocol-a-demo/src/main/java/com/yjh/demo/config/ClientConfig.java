package com.yjh.demo.config;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.protocol_a.IdentityProvider;
import com.yjh.protocol_a.MessageIdGenerator;
import com.yjh.protocol_a.MessageSender;
import org.apache.commons.lang3.StringUtils;
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

    String sendCode;

    String receiveCode;

    public static boolean sendBatch;

    public static long sessionId = 0l;

    @Value("${send.batch:true}")
    public void setSendBatch(boolean sendBatch) {
        ClientConfig.sendBatch = sendBatch;
    }

    public void setSendCode(String sendCode, String receiveCode) {
        if (StringUtils.isNotEmpty(sendCode)) {
            this.sendCode = sendCode;
        }
        if (StringUtils.isNotEmpty(receiveCode)) {
            this.receiveCode = receiveCode;
        }
    }

    @Bean("clientRxBus")
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

/*
    @Bean
    public MsgChannel clientMsgChannel(ExecutorService clientOutboundExecutor, RxBus clientRxBus) {
        return new RxBusMsgChannel(
                clientRxBus,
                msg -> msg instanceof Msg.Outbound,
                new MessageCodec("PatrolDevice", true),
                clientOutboundExecutor
        );
    }
*/

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

    public String getSendCode() {
        return sendCode;
    }

    public String getReceiveCode() {
        return receiveCode;
    }
}
