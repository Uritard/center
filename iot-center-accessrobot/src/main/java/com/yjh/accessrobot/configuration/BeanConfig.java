package com.yjh.accessrobot.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author hyh
 * @since 2022/8/24
 **/
@Component
@Slf4j
public class BeanConfig {

    @Resource
    private RedisTemplate redisTemplate;

    @Bean
    public String getIntervalValue() {
        String heartBeatInterval = (String)redisTemplate.opsForHash().get("systemConfigKey:intervalConfig", "heartBeatInterval");

        long interval = NumberUtils.toLong(heartBeatInterval, 60) * 1000;
        log.info("心跳定时时间：{}", interval);
        return String.valueOf(interval);
    }
}
