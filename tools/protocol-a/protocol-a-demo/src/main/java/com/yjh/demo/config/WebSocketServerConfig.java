package com.yjh.demo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.ws.WsMessageSender;
import com.yjh.demo.ws.WsMessageTypeRegistry;
import com.yjh.demo.ws.WsSessionManager;
import com.yjh.messager.api.channel.RxBusMsgChannel;
import com.yjh.messager.api.msg.*;
import com.yjh.messager.api.ws.BaseWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.concurrent.*;

/**
 * WebSocket服务配置
 *
 * @author zilong
 * @date 2022/4/1
 * @since 1.0
 */
@Configuration
public class WebSocketServerConfig {

    /*
    * ws地址：ws://127.0.0.1:18088/demo/message
    */

    @Bean
    RxBus wsMsgBus() {
        return new RxBus();
    }

    @Bean
    MessageTypeRegistry messageTypeRegistry() {
        return new WsMessageTypeRegistry();
    }

    @Bean
    BaseJsonCodec wsJsonCodec(MessageTypeRegistry messageTypeRegistry) {
        return new BaseJsonCodec(new ObjectMapper(), messageTypeRegistry, "msgType");
    }

    @Bean
    Executor wsMessageProcessingExecutor() {
        return new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1000),
                Executors.defaultThreadFactory()
        );
    }

    @Bean
    Executor wsOutboundExecutor() {
        return new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<>(1000),
                Executors.defaultThreadFactory()
        );
    }

    @Bean
    RxBusMsgChannel wsMsgChannel(RxBus wsMsgBus, BaseJsonCodec wsJsonCodec, Executor wsOutboundExecutor) {
        return new RxBusMsgChannel(
                wsMsgBus,
                msg -> msg instanceof Msg.Outbound,
                wsJsonCodec,
                wsOutboundExecutor
        );
    }

    @Bean
    BaseWebSocketHandler webSocketHandler(RxBusMsgChannel wsMsgChannel) {
        return new BaseWebSocketHandler(wsMsgChannel);
    }

    @Bean
    EventBus wsMessageDispatchBus() {
        return new EventBus("WsMessageDispatchBus");
    }

    @Bean
    SimpleMessageSender wsMessageSender(RxBus wsMsgBus, WsSessionManager wsSessionManager) {
        return new WsMessageSender(wsMsgBus,wsSessionManager);
    }

    @Bean
    SimpleMessageDispatcher wsMessageDispatcher(Executor wsMessageProcessingExecutor,
                                                RxBus wsMsgBus,
                                                EventBus wsMessageDispatchBus
    ) {
        return new SimpleMessageDispatcher(wsMessageProcessingExecutor, wsMsgBus, wsMessageDispatchBus);
    }

    @Component
    @EnableWebSocket
    static final class WebSocketHandlerRegister implements WebSocketConfigurer {

        final BaseWebSocketHandler webSocketHandler;

        WebSocketHandlerRegister(BaseWebSocketHandler webSocketHandler) {
            this.webSocketHandler = webSocketHandler;
        }

        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(webSocketHandler, "/message").setAllowedOrigins("*");
        }
    }

}
