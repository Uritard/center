/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.configuration;

import cn.hutool.core.io.FileUtil;
import com.yjh.platform.common.Constant;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

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

    @Value("${spring.servlet.multipart.location:}")
    private String multipartLocation;

    @Autowired
    private RedisTemplate redisTemplate;

    private final static Map<String, String> SYS_PARAM_CACHE_MAP = new HashMap<>(256);

    public static final String SYS_PREFIX = "t_sys_param:";

    private Set<String> dirs = new HashSet<>();

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
            if (StringUtils.startsWith(value, "/home/yjh_iot_center")) {
                dirs.add(value);
            }
            contents.put("paramCode", key);
            contents.put("paramName", key);
            contents.put("content", value);
            String str = SYS_PREFIX + key;
            log.info("paramKey: {}, value: {}", key, value);
            redisTemplate.opsForHash().putAll(str, contents);
        }
        edgeToRedis();
        initDir();
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
            redisTemplate.opsForHash().putAll(SYS_PREFIX + "edgeNode", contents);
            return;
        }

        for (Map.Entry<String, String> entry : edge.entrySet()) {
            String key = "edge" + StringUtils.capitalize(entry.getKey());
            String value = entry.getValue();
            contents.put("paramCode", key);
            contents.put("paramName", key);
            contents.put("content", value);
            String str = SYS_PREFIX + key;
            log.info("paramKey: {}, value: {}", key, value);
            redisTemplate.opsForHash().putAll(str, contents);
        }
    }

    /**
     * 初始化文件夹
     */
    public void initDir() {
        dirs.add(multipartLocation);
        log.info("创建目录：{}", dirs);
        for (String dir : dirs) {
            FileUtil.mkdir(dir);
        }
    }

    /**
     * 将Redis中保存的配置信息缓存到内存中，减少对Redis使用
     */
    public void initParamCache() {
        Set<String> tasKeys = RedisUtil.redisScan(SYS_PREFIX);
        if (CollectionUtils.isEmpty(tasKeys)) {
            log.error("未查询到配置数据: {}", SYS_PREFIX);
        }

        List<Map<String, String>> sysParamList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>)connection -> {
            tasKeys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
            return null;
        });

        for (Map<String, String> sys : sysParamList) {
            SYS_PARAM_CACHE_MAP.put(sys.get("paramCode"), sys.get("content"));
        }

        Constant.apiPermissions = Boolean.parseBoolean(SYS_PARAM_CACHE_MAP.get("springInterfaceApi"));
    }

    public static String getSysContent(String paramCode) {
        return SYS_PARAM_CACHE_MAP.get(paramCode);
    }

    public static boolean getBooleanContent(String paramCode) {
        return Boolean.parseBoolean(SYS_PARAM_CACHE_MAP.get(paramCode));
    }
}
