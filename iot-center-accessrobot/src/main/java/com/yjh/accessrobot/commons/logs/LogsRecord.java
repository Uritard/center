package com.yjh.accessrobot.commons.logs;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Component
public class LogsRecord {

    @Resource
    private  RedisTemplate redisTemplate;

    public void LogsSend(HttpServletRequest request,String type,String title,String content){
        Long userIds = Long.valueOf(request.getHeader("userId"));
//        Long userIds = 10011L;

//        String userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userIds).get("roleId"));
//        if(Constant.apiPermissions) {
//            if (!"1234".equals(userRole)) {
//                //权限不够；
//                throw new BusinessException(10008, "用户无权限");
//                //return -1;
//            }
//        }
        String userName=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userIds,"userName"));
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.set("logType", type);
        param.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
        param.set("title", title);
        param.set("state", 1);
        param.set("userId", userIds);
        param.set("userName", userName);
        param.set("requestOrigin", request.getRequestURL());
        param.set("requestPath", request.getRequestURI());
        param.set("requestMethod", request.getMethod());
        param.set("content", content);
        LogsAspect logsAspects = new LogsAspect();
        logsAspects.post(param);
    }

    public void LoginLogsSend(HttpServletRequest request,String type,String title,String content,String userName,String userIds,Integer state){
        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
        param.set("logType", type);
        param.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
        param.set("title", title);
        param.set("state", state);
        param.set("userId", userIds);
        param.set("userName", userName);
        param.set("requestOrigin", request.getRequestURL());
        param.set("requestPath", request.getRequestURI());
        param.set("requestMethod", request.getMethod());
        param.set("content", content);
        LogsAspect logsAspects = new LogsAspect();
        logsAspects.post(param);
    }
}
