/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.gateway.common.handler;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/8/2
 * @since [产品/模块版本] （可选）
 */
@Component
public class StaticResourceHandler extends HandlerInterceptorAdapter {
    private static final Logger log = LoggerFactory.getLogger(StaticResourceHandler.class);

    public static final String UNAUTH_MSG = "<html><head><title>404 Not Found</title></head><body bgcolor=\"white\"><center><h1>404 Not Found</h1></center><hr><center>webServer</center></body></html>";
    @Value("${sys.resource.send-error:false}")
    private boolean sendError;

    public static String[] NOAUTH_PATH;
    public static String[] RESOURCE_PATH;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("static resource >>> {}", request.getRequestURL());

        if (!needAuthResource(request)) {
            return true;
        }

        ImmutablePair<String, String> authPair = getAuthPair(request);

        String userId = authPair.getLeft();
        String token = authPair.getRight();
        if (StringUtils.isEmpty(token)) {
            log.error("静态资源用户未登陆 >>>>>>>>>>>>>>>>");
            return errorResponse(response, HttpStatus.SC_NOT_FOUND, UNAUTH_MSG, sendError);
        }

        Map<String, String> appKeymap = redisTemplate.opsForHash().entries("appKey:" + userId + ":" + token);
        if (appKeymap.size() == 0) {
            log.error("静态资源用户已失效 >>>>>>>>>>>>>>>>");
            return errorResponse(response, HttpStatus.SC_NOT_FOUND, UNAUTH_MSG, sendError);
        } else {
            Long expireTime = Long.valueOf(appKeymap.get("expireTime"));
            int logoutTime = Integer.valueOf(String.valueOf(redisTemplate.opsForHash().get("t_sys_param:logoutTime", "content")));
            if (System.currentTimeMillis() - expireTime > 60000 * logoutTime) {
                log.error("静态资源 token 已过期 >>>>>>>>>>>>>>>>");
                return errorResponse(response, HttpStatus.SC_NOT_FOUND, UNAUTH_MSG, sendError);
            }
        }

        return true;
    }

    public boolean needAuthResource(HttpServletRequest request) {
        String uri = request.getRequestURI();
        log.info("resource uri: {}", uri);
        // if (!StringUtils.startsWithAny(uri, RESOURCE_PATH)) {
        //     return false;
        // }
        return !StringUtils.startsWithAny(uri, NOAUTH_PATH);
    }

    public ImmutablePair<String, String> getAuthPair(HttpServletRequest request) {
        String userId = request.getHeader("userId") != null ? request.getHeader("userId") : "";
        String token = request.getHeader("token") != null ? request.getHeader("token") : "";
        String from = "header";
        if (StringUtils.isEmpty(token)) {
            Cookie[] cookies = request.getCookies();
            log.info("cookies: {}", JSON.toJSONString(cookies));
            if (cookies != null) {
                from = "cookies";
                for (Cookie c : cookies) {
                    if ("userId".equals(c.getName())) {
                        userId = c.getValue();
                    } else if ("token".equals(c.getName())) {
                        token = c.getValue();
                    }
                }
            }
        }
        log.info("get auth pair from [{}], userId: {}, token: {}", from, userId, token);
        return new ImmutablePair<>(userId, token);
    }

    boolean errorResponse(HttpServletResponse response, int nStatusCode, String errorMsg, boolean sendError) throws IOException {
        if (sendError) {
            response.sendError(nStatusCode);
        } else {
            response.setContentType(MediaType.TEXT_HTML_VALUE);
            response.setStatus(nStatusCode);
            if (StringUtils.isNotEmpty(errorMsg)) {
                response.getWriter().write(errorMsg);
            }
        }
        return false;
    }

    @Value("${sys.resource.noauthpath:}")
    public void setNoauthPath(String noauthPath) {
        if (StringUtils.isNotEmpty(noauthPath)) {
            NOAUTH_PATH = noauthPath.split(",");
        }
    }

    @Value("${sys.resource.path:}")
    public void setResourcePath(String resourcePath) {
        if (StringUtils.isNotEmpty(resourcePath)) {
            RESOURCE_PATH = resourcePath.split(",");
        }
    }

}
