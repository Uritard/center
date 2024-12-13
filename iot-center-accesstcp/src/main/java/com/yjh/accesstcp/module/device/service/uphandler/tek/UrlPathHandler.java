package com.yjh.accesstcp.module.device.service.uphandler.tek;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.HttpClientUtils;
import com.yjh.accesstcp.module.device.service.impl.UpType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.http.client.protocol.HttpClientContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
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
    private final static String KEY = "systemConfigKey:upSystem";
    private final static String OAUTH_URL = "/auth/oauth/token";

    private final RedisTemplate redisTemplate;
    private final TaskScheduler taskScheduler;

    @Value("${up.tek.prefix:}")
    private String prefix;
    @Value("${up.tek.username:}")
    private String username;
    @Value("${up.tek.password:}")
    private String password;

    public String getTekUrl(String url) {
        return HTTP + Constant.upSystemIp() + ":" + Constant.upSystemPort() + url;
    }

    public String authToken() {
        String url = getTekUrl(OAUTH_URL);
        if (UpType.TEK.getType() != NumberUtils.toInt(Constant.upSystemFlag())) {
            log.warn("科大上级系统未配置，upSystemFlag: {}", Constant.upSystemFlag());
            return null;
        }

        Map<String, Object> params = new HashMap<>(8);
        params.put("username", username);
        params.put("password", password);
        params.put("grant_type", "password");
        params.put("scope", "server");

        Map<String, String> heardAttrs = new HashMap<>(4);
        heardAttrs.put("authorization", "Basic aW9tczppb21z");

        try {
            String ret = HttpClientUtils.getInstance().tokenAuth(url, params, heardAttrs, this::authToken);
            log.info("鉴权登录：{}", ret);
            JSONObject json = JSON.parseObject(ret);
            String token = json.getString("access_token");
            long expires = json.getLongValue("expires_in");
            // 提前10分钟刷新，若刷新时间不满600，则固定为12小时，43200秒
            expires = expires > 600L ? expires - 600L : 43200L;

            // 将 token 更新入 context
            HttpClientContext context = HttpClientContext.create();
            context.setUserToken(token);
            HttpClientUtils.getInstance().updateContext(url, context);

            // 设置定时器，超期之前刷新
            taskScheduler.schedule(this::authToken, Instant.now().plusSeconds(expires));
            return token;
        } catch (IOException e) {
            log.error("登录鉴权失败", e);
        }
        return null;
    }
}
