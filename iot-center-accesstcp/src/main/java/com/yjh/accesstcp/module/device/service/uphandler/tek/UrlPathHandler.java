package com.yjh.accesstcp.module.device.service.uphandler.tek;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @Author: lqh
 * @Date: 2024/12/09
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UrlPathHandler {

    private final static String HTTP = "http://";
    private final static String key = "systemConfigKey:upSystem";

    @Autowired
    private RedisTemplate redisTemplate;

    public String getTekUrl(String url){
        Map<String,String> map = redisTemplate.opsForHash().entries(key);
        String ip = map.get("upSystemIp");
        String port = map.get("upSystemPort");
        return HTTP+ip+":"+port+url;
    }
}
