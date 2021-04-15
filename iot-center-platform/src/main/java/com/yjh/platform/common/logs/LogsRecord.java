//package com.yjh.platform.common.logs;
//
//import org.springframework.data.redis.core.RedisTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//
//import javax.annotation.Resource;
//import javax.servlet.http.HttpServletRequest;
//
//@Component
//public class LogsRecord {
//
//    @Resource
//    private  RedisTemplate redisTemplate;
//
//    public void LogsSend(HttpServletRequest request,String type,String title,String content){
//        Long userIds = Long.valueOf(request.getHeader("userId"));
//        String userName=String.valueOf(redisTemplate.opsForHash().get("userInfo:"+userIds,"userName"));
//        MultiValueMap<String, Object> param = new LinkedMultiValueMap<>();
//        param.set("logType", type);
//        param.set("ip", request.getHeader("HTTP_X_FORWARDED_FOR"));
//        param.set("title", title);
//        param.set("state", 1);
//        param.set("userId", userIds);
//        param.set("userName", userName);
//        param.set("requestOrigin", request.getRequestURL());
//        param.set("requestPath", request.getRequestURI());
//        param.set("requestMethod", request.getMethod());
//        param.set("content", content);
//        LogsAspect logsAspects = new LogsAspect();
//        logsAspects.post(param);
//    }
//}
