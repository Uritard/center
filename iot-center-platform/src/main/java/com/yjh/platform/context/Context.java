package com.yjh.platform.context;

import com.yjh.messager.api.socket.BaseSocketServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Context {

    private final BaseSocketServer socketServer;

    public Context(BaseSocketServer socketServer) {
        this.socketServer = socketServer;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.warn("<========== app ready ==========>");
        try {
            socketServer.start();
        } catch (Exception e) {
            throw new RuntimeException("Socket服务启动失败", e);
        }

    }

    @EventListener
    public void onContextClosed(ContextClosedEvent ctxCloseEvt) {
        log.warn("shutdown...");
        socketServer.destroy();
    }
}

