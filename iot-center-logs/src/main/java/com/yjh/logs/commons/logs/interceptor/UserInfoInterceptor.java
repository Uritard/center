package com.yjh.logs.commons.logs.interceptor;

import com.alibaba.fastjson.JSON;
import com.yjh.logs.common.context.UserContext;
import com.yjh.logs.common.context.UserContext.OperatorDto;
import com.yjh.logs.commons.logs.track.HttpTracing;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;

/**
 * @author lichensi
 * @date 2020/12/10 14:15
 */
@Configuration
@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor, HttpTracing {

    @Autowired
    private RedisTemplate redisTemplate;
    private static final String USER_INFO = "userId";

    public boolean preHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler) {
        try {
            final String userInfoJson = request.getHeader(USER_INFO);
            if (StringUtils.isNotBlank(userInfoJson)) {
                OperatorDto operatorDto = new OperatorDto();
                Map userInfoMap = redisTemplate.opsForHash().entries("userInfo:"+userInfoJson);
                operatorDto.setUserId(Long.parseLong(String.valueOf(userInfoMap.get("userId"))));
                operatorDto.setUserName(String.valueOf(userInfoMap.get("userName")));
                if(Objects.nonNull(operatorDto)){ UserContext.init(operatorDto); }
            }
        } catch (Exception ex) {
            log.error("[yjh-user-track] [用户信息] ", ex);
        }
        return true;
    }

    public void afterCompletion(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final Exception ex) {
        UserContext.clear();
    }
}
