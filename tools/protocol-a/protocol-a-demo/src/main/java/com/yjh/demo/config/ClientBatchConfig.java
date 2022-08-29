package com.yjh.demo.config;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.messager.api.channel.MsgChannel;
import com.yjh.messager.api.channel.RxBusMsgChannel;
import com.yjh.messager.api.msg.Msg;
import com.yjh.protocol_a.impl.MessageCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @ClassName: ClientBatchConfig
 * @Description:
 * @author: yanhao
 * @date: 2022/8/29
 */
@Configuration
public class ClientBatchConfig {

    @Bean
    public RxBus clientBatchRxBus() {
        return new RxBus();
    }

    @Bean
    public ExecutorService clientBatchOutboundExecutor() {
        return Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
    }

    @Bean
    public ExecutorService clientMsgBatchProcessingExecutor() {
        return Executors.newSingleThreadExecutor(Executors.defaultThreadFactory());
    }

    @Bean
    public MsgChannel clientBatchMsgChannel(ExecutorService clientBatchOutboundExecutor, RxBus clientBatchRxBus) {
        return new RxBusMsgChannel(
                clientBatchRxBus,
                msg -> msg instanceof Msg.Outbound,
                new MessageCodec("PatrolDevice", true),
                clientBatchOutboundExecutor
        );
    }

}
