package com.yjh.demo.context;

import com.yjh.demo.service.ClientService;
import com.yjh.demo.service.ServerService;
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

    private final ClientService clientService;
   private final ServerService serverService;

    public Context(ClientService clientService, ServerService serverService) {
        this.clientService = clientService;
        this.serverService = serverService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.warn("<========== app ready ==========>");
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent ctxCloseEvt) {
        log.warn("shutdown...");
        clientService.stop();
        serverService.destroy();
    }
}
