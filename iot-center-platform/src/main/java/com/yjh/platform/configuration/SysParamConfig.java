/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 安全配置从数据库迁移至 application.properties
 *
 * @author Chenfei
 * @date 2022/7/22
 * @since [产品/模块版本] （可选）
 */
@Component
@ConfigurationProperties(prefix = "sys.param")
@Data
@Slf4j
public class SysParamConfig {
    private Map<String, String> secure;

    /**
     * 节点配置
     */
    private Map<String, String> edge;

    @Autowired
    private RedisTemplate redisTemplate;

    public void putToRedis() {
        if (secure == null) {
            log.error("sys.param.secure is null");
            return;
        }
        Map<String, String> contents = new LinkedHashMap<>();

        contents.put("paramId", "-1");
        contents.put("paramType", "402");
        contents.put("remark", "系统安全参数");
        for (Map.Entry<String, String> entry : secure.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            contents.put("paramCode", key);
            contents.put("paramName", key);
            contents.put("content", value);
            String str = "t_sys_param:" + key;
            log.info("paramKey: {}, value: {}", key, value);
            redisTemplate.opsForHash().putAll(str, contents);
        }
        edgeToRedis();
    }

    /**
     * 节点信息存储
     */
    public void edgeToRedis() {
        Map<String, String> contents = new LinkedHashMap<>();

        contents.put("paramId", "-1");
        contents.put("paramType", "404");
        contents.put("remark", "节点配置信息");
        if (edge == null) {
            log.error("sys.param.edge is null");
            contents.put("paramCode", "edgeNode");
            contents.put("paramName", "edgeNode");
            contents.put("content", "false");
            redisTemplate.opsForHash().putAll("t_sys_param:edgeNode", contents);
            return;
        }

        for (Map.Entry<String, String> entry : edge.entrySet()) {
            String key = "edge" + StringUtils.capitalize(entry.getKey());
            String value = entry.getValue();
            contents.put("paramCode", key);
            contents.put("paramName", key);
            contents.put("content", value);
            String str = "t_sys_param:" + key;
            log.info("paramKey: {}, value: {}", key, value);
            redisTemplate.opsForHash().putAll(str, contents);
        }
    }
}
