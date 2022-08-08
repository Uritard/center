package com.yjh.demo.context;

import com.yjh.messager.api.socket.BaseSocketClient;
import com.yjh.messager.api.socket.BaseSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author zilong
 * @since 2022/1/28
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */
@Slf4j
@Component
public class Context {

    private final BaseSocketServer socketServer;
//    private final BaseSocketClient socketClient;

    public Context(BaseSocketServer socketServer/*, BaseSocketClient socketClient*/) {
        this.socketServer = socketServer;
//        this.socketClient = socketClient;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.warn("<========== app ready ==========>");
        try {
            socketServer.start();
        } catch (Exception e) {
            throw new RuntimeException("Socket服务启动失败", e);
        }

//        socketClient.start();
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent ctxCloseEvt) {
        log.warn("shutdown...");
        socketServer.destroy();
//        socketClient.stop();
    }
}
