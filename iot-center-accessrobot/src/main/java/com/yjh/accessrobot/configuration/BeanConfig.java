package com.yjh.accessrobot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author hyh
 * @since 2022/8/24
 **/
@Component
public class BeanConfig {

    @Resource
    private RedisTemplate redisTemplate;

    @Bean
    public String getIntervalValue() {
        String heartBeatInterval = "60";
        Map<String, String> intervalConfig = redisTemplate.opsForHash().entries("systemConfigKey:intervalConfig");
        String key = "heartBeatInterval";
        if (intervalConfig.containsKey(key)){
            heartBeatInterval = String.valueOf(intervalConfig.get("heartBeatInterval"));
        }
        return (Integer.parseInt(heartBeatInterval) * 1000) + "";
    }
}
