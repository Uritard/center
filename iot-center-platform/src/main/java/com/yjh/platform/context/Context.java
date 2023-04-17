package com.yjh.platform.context;

import com.yjh.messager.api.socket.BaseSocketServer;
import com.yjh.platform.common.Constant;
import com.yjh.platform.module.config.entity.SystemConfig;
import com.yjh.platform.module.config.service.SystemConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class Context {

    private final BaseSocketServer socketServer;

    private final RedisTemplate redisTemplate;

    private final SystemConfigService systemConfigService;

    private final String systemConfigKey="systemConfigKey:";

    public Context(BaseSocketServer socketServer,RedisTemplate redisTemplate,SystemConfigService systemConfigService) {
        this.socketServer = socketServer;
        this.redisTemplate = redisTemplate;
        this.systemConfigService = systemConfigService;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.warn("<========== app ready ==========>");
        try {
            socketServer.start();
            deleteSecondSilentRedisConf();
            systemConfigInfoToRedis();
            systemConfigService.flushCatch();
        } catch (Exception e) {
            throw new RuntimeException("Socket服务启动失败", e);
        }

    }

    private void deleteSecondSilentRedisConf(){
        String key = Constant.SILENT_SECOND+"**";
        Set<String> keys = redisTemplate.keys(key);
        if (CollectionUtils.isNotEmpty(keys)) {
            redisTemplate.delete(keys);
        }
    }

    private void systemConfigInfoToRedis(){
        List<SystemConfig> configList = systemConfigService.selectAll();
        configList.forEach(systemConfig ->{
            Map<String,String> map = new HashMap<>();
            map.put(systemConfig.getConfigKey(),systemConfig.getConfigValue());
            redisTemplate.opsForHash().putAll(systemConfigKey+systemConfig.getConfigType(),map);
        });
    }

    @EventListener
    public void onContextClosed(ContextClosedEvent ctxCloseEvt) {
        log.warn("shutdown...");
        socketServer.destroy();
    }
}

