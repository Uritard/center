package com.yjh.platform.context;

import com.yjh.messager.api.socket.BaseSocketServer;
import com.yjh.platform.common.Constant;
import com.yjh.platform.configuration.RedisUtil;
import com.yjh.platform.module.iot.service.TIotDeviceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class Context {

    private final BaseSocketServer socketServer;

    private final RedisTemplate redisTemplate;

    private final TIotDeviceService tIotDeviceService;

    public Context(BaseSocketServer socketServer,RedisTemplate redisTemplate,TIotDeviceService tIotDeviceService) {
        this.socketServer = socketServer;
        this.redisTemplate = redisTemplate;
        this.tIotDeviceService = tIotDeviceService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.warn("<========== app ready ==========>");
        try {
            socketServer.start();
            deleteSecondSilentRedisConf();
            tIotDeviceService.deviceUpload();
        } catch (Exception e) {
            throw new RuntimeException("Socket服务启动失败", e);
        }

    }

    private void deleteSecondSilentRedisConf(){
        String key = Constant.SILENT_SECOND;
        Set<String> keys = RedisUtil.redisScan(key);
        if (CollectionUtils.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent ctxCloseEvt) {
        log.warn("shutdown...");
        socketServer.destroy();
    }
}

