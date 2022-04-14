package com.yjh.logs.common.logs;

import com.yjh.logs.common.Constant;
import com.yjh.logs.commons.restTemplate.ServiceRestTemplate;
import com.yjh.logs.commons.result.BusinessException;
import com.yjh.logs.commons.result.Result;
import com.yjh.logs.module.log.controller.SysLogController;
import com.yjh.logs.module.log.service.SysLogService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Description
 * @Author tt
 * @Date 2019/7/30
 **/
@Aspect
@Component
public class LogsAspect {
    private static final Logger log = LoggerFactory.getLogger(LogsAspect.class);

    private static final String LOG_URL = "http://iot-center-share/sysLog/v1/add";
    @Autowired
    private LogsConfig logsConfig;
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SysLogController sysLogService;

    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    RestTemplate serviceRestTemplate() {
        return new ServiceRestTemplate();
    }

    @Pointcut("execution(* com.yjh..controller..*.*(..))")
    public void selectAspect() {
    }

    @Around("selectAspect()")
    public Object checkSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Logs annotation = signature.getMethod().getAnnotation(Logs.class);
        Map<String, Object> params = new HashMap<>();
        StringBuilder content = new StringBuilder("");
        String userId = "99999", userName = null, serviceId = null, ip = null,userRole=null;
        HttpServletRequest request = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        if (null != servletRequestAttributes) {
            request = servletRequestAttributes.getRequest();
            if (Objects.nonNull(request.getHeader("userId")) && !Objects.equals(request.getHeader("userId"), "undefined")) {
                userId = request.getHeader("userId");
                userName = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("userName"));
                userRole = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("roleId"));
            } else { userName = "admin"; }
        }
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        log.info("ttIp: "+ip);
        Object result = null;
        if (annotation != null) {
            content.append(annotation.content());
            params.put("userId", userId);
            params.put("userName", userName);
            params.put("requestOrigin", request.getRequestURL());
            params.put("requestPath", request.getRequestURI());
            params.put("requestMethod", request.getMethod());
            if(Constant.apiPermissions){
                try {
                    if (!"".equals(annotation.authority())) {
                        assert userRole != null;
                        if (!userRole.equals(annotation.authority())) {
                            //todo 越权访问入日志
                            params.put("logType", "24");
                            params.put("ip", ip);
                            params.put("title", annotation.title());
                            params.put("state", "3");
                            Map<String, String> jsonMap = new HashMap<>(8);
                            jsonMap.put("type", "alarmPopUp");
                            jsonMap.put("ip", ip);
                            jsonMap.put("warningInfo", "越权访问告警！！！");
                            jsonMap.put("userId", userId);
                            jsonMap.put("logType", "24");
                            jsonMap.put("userName", userName);
                            jsonMap.put("title", annotation.title());
                            jsonMap.put("content", String.valueOf(content));
                            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jsonMap);
                            content.append(";用户").append(userName).append("存在越权访问!");
                            params.put("content", content.toString());
                            post(params);
                            Result re = new Result();
                            re.setCode(209, "此用户无权限");
                            return re;
                        }
                    }

                }catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }


            try {
//                serviceId = logsConfig.getName();
                params.put("logType", annotation.logType());
                params.put("ip", ip);
                params.put("title", annotation.title());
                params.put("state", 1);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            try {
                // 记录操作日志...谁..在什么时间..做了什么事情..
                result = joinPoint.proceed();
                params.put("content", content.toString());
                post(params);
                return result;
            } catch (BusinessException e) {
                params.put("state", 2);
                params.put("content", content.toString() + "；错误信息：" + e.getMessage());
                post(params);
                throw e;
            } catch (Throwable e) {
                params.put("content", content.toString() + "；异常信息：" + e.getMessage());
                params.put("state", 3);
                post(params);
                throw e;
            }
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            serviceId = logsConfig.getName();
            params.put("logType", annotation.logType());
            params.put("ip", ip);
            params.put("title", "内部接口错误");
            params.put("state", 3);
            params.put("content", "异常信息: " + e.getMessage() + "\n" + getStackMsg(e));
            params.put("userId", userId);
            params.put("userName", userName);
            post(params);
            throw e;
        }
    }

    public void post(Map<String, Object> params) {
        try {
            log.info("params {}", params);
            sysLogService.insert(String.valueOf(params.get("logType")),
                    String.valueOf(params.get("ip")),
                    String.valueOf(params.get("title")),
                    Integer.valueOf(String.valueOf(params.get("state"))),
                    String.valueOf(params.get("content")),
                    Long.parseLong(String.valueOf(params.get("userId"))),
                    String.valueOf(params.get("userName")),
                    String.valueOf(params.get("requestOrigin")),
                    String.valueOf(params.get("requestPath")),
                    String.valueOf(params.get("requestMethod")));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static String getStackMsg(Throwable e) {
        try {
            System.out.println("platformStack");
            StringBuffer sb = new StringBuffer();
            sb.append(e.getMessage() + "\n");
            StackTraceElement[] stackArray = e.getStackTrace();
            for (StackTraceElement element : stackArray) {
                sb.append(element.toString() + "\n");
            }
            return sb.toString();
        } catch (Exception e1) {
            return "";
        }
    }
}
