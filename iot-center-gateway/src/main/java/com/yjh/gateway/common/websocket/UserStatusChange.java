/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.gateway.common.websocket;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/6/19
 * @since [产品/模块版本] （可选）
 */
@Component
public class UserStatusChange {
    private static Logger log = LoggerFactory.getLogger(UserStatusChange.class);

    private final static int DELAY_TIME = 10;
    private static RedisTemplate redisTemplate;
    private static String LOGOUT_GATEWAY_URL;
    /**
     * 退出登录定时器
     **/
    private static Map<String, ScheduledFuture<?>> outFutureMap = new ConcurrentHashMap<>();

    private static ScheduledExecutorService executorService = new ScheduledThreadPoolExecutor(1,
        new BasicThreadFactory.Builder().namingPattern("wsLogout-schedule-pool-%d").daemon(true).build());

    @Autowired
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        UserStatusChange.redisTemplate = redisTemplate;
    }

    @Value("${spring.logout.path}")
    public void setLOGOUT_GATEWAY_URL(String LOGOUT_GATEWAY_URL) {
        UserStatusChange.LOGOUT_GATEWAY_URL = LOGOUT_GATEWAY_URL;
    }

    /**
     * 清除定时器和redis过期时间，避免刷新导致自动退出
     */
    public static void logIn(String userId, String token) {
        try {
            ScheduledFuture<?> outFuture = outFutureMap.get(userId + "_" + token);
            log.info("websocket 上线，恢复登录状态，userId: {}, token: {}, outFuture: {}", userId, token, outFuture);
            if (StringUtils.isAnyEmpty(userId, token)) {
                return;
            }
            if (outFuture != null) {
                outFuture.cancel(true);
            }
            String isLogin = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isLogin", "content"));
            Long ttl = redisTemplate.getExpire("appKey:" + userId + ":" + token);
            if (ttl != null && ttl > 0) {
                redisTemplate.persist("appKey:" + userId + ":" + token);
                if ("true".equals(isLogin)) {
                    redisTemplate.persist("appKey:" + userId);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 定时 10s 后退出
     */
    public static void logOut(String userId, String token) {
        try {
            boolean loginKeepByWs =
                Boolean.parseBoolean(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:loginKeepByWs", "content")));
            log.info("websocket 关闭，退出登录状态，loginKeepByWs: {}, userId: {}, token: {}", loginKeepByWs, userId, token);
            if (!loginKeepByWs || StringUtils.isAnyEmpty(userId, token)) {
                return;
            }
            String isLogin = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:isLogin", "content"));
            Map<String, String> appKeymap = redisTemplate.opsForHash().entries("appKey:" + userId + ":" + token);
            if (MapUtils.isNotEmpty(appKeymap)) {
                redisTemplate.expire("appKey:" + userId + ":" + token, DELAY_TIME, TimeUnit.SECONDS);
                if ("true".equals(isLogin)) {
                    redisTemplate.expire("appKey:" + userId, DELAY_TIME, TimeUnit.SECONDS);
                }
                // 设置一个定时器，10s 后调用退出登录接口
                String remoteIp = appKeymap.getOrDefault("remoteIp", "");
                String userName = appKeymap.getOrDefault("userName", "");
                ScheduledFuture<?> outFuture = executorService.schedule(() -> {
                    try {

                        String res = UserStatusChange.postUrl(token, remoteIp, userId, userName);
                        log.info("10S 延迟已至，执行退出登录，userId: {}, token: {}, res: {}", userId, token, res);
                    } catch (Exception e) {
                        log.error("退出登录失败：{} {}", userId, token, e);
                    }
                }, DELAY_TIME, TimeUnit.SECONDS);
                outFutureMap.put(userId + "_" + token, outFuture);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static String postUrl(String token, String ip, String userId, String userName) throws IOException, URISyntaxException {
        CloseableHttpClient client = HttpClients.createDefault();
        URI uri = new URIBuilder(LOGOUT_GATEWAY_URL).setParameter("token", token).setParameter("ip", ip).setParameter("userId", userId)
            .setParameter("userName", userName).build();
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("Content-type", "application/json;charset=utf-8");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setEntity(new StringEntity(token, StandardCharsets.UTF_8));
        httpPost.setEntity(new StringEntity(ip, StandardCharsets.UTF_8));
        httpPost.setEntity(new StringEntity(userId, StandardCharsets.UTF_8));
        httpPost.setEntity(new StringEntity(userName, StandardCharsets.UTF_8));
        CloseableHttpResponse response = client.execute(httpPost);
        HttpEntity entity = response.getEntity();
        return EntityUtils.toString(entity, "UTF-8");
    }
}
