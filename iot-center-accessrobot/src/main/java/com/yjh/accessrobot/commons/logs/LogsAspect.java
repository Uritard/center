package com.yjh.accessrobot.commons.logs;



import com.alibaba.fastjson.JSON;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.BusinessException;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.utils.http.IPUtil;
import org.apache.commons.lang3.StringUtils;
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
import java.util.*;

/**
 * @Description
 * @Author tt
 * @Date 2019/7/30
 **/
@Aspect
@Component
public class LogsAspect {
    private static final Logger log = LoggerFactory.getLogger(IPUtil.class);

    private static final String LOG_URL = "http://iot-center-share/sysLog/v1/add";
    @Autowired
    private LogsConfig logsConfig;
    @Autowired
    private RedisTemplate redisTemplate;

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
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
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
            } else {
                try {
                    return joinPoint.proceed();
                } catch (Throwable e) {
                    serviceId = logsConfig.getName();
                    params.set("logType", annotation.logType());
                    params.set("ip", ip);
                    params.set("title", "内部接口错误");
                    params.set("state", 3);
                    params.set("content", "异常信息: " + e.getMessage() + "\n" + getStackMsg(e));
                    params.set("userId", userId);
                    params.set("userName", userName);
                    post(params);
                    throw e;
                }
            }
        }
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        Object result = null;
        if (annotation != null) {
            content.append(annotation.content());
            params.set("logType", annotation.logType());
            params.set("state", 1);
            params.set("ip", ip);
            params.set("title", annotation.title());
            params.set("userId", userId);
            params.set("userName", userName);
            params.set("requestOrigin", request.getRequestURL());
            params.set("requestPath", request.getRequestURI());
            params.set("requestMethod", request.getMethod());
            if(Constant.apiPermissions()){
                try {
                    if (!"".equals(annotation.authority())) {
                        String ans = annotation.authority();
//                        List<String> ans = new ArrayList<String>(annotation.authority().split(","));
//                        for (String an : ans) {
                            if (StringUtils.isNotEmpty(userRole) && !ans.contains(userRole)) {
                                //todo 越权访问入日志
                                params.set("logType", "24");
                                params.set("ip", ip);
                                params.set("title", "用户越权访问");
                                params.set("state", 3);
                                Map<String, String> jsonMap = new HashMap<>(8);
                                jsonMap.put("type", "alarmPopUp");
                                jsonMap.put("ip", ip);
                                jsonMap.put("warningInfo", "越权访问告警！！！");
                                jsonMap.put("userId", userId);
                                jsonMap.put("logType", "24");
                                jsonMap.put("userName", userName);
                                jsonMap.put("title", annotation.title());
                                jsonMap.put("content", String.valueOf(content));
                                String webSocketUrl = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:webSocketUrl","content"));
                                Constant.postUrl(webSocketUrl, JSON.toJSONString(jsonMap));
                                content.append(";用户").append(userName).append("存在越权访问!");
                                params.set("content", content.toString());
                                if (!"控制巡视设备".equals(annotation.title())) {
                                    post(params);
                                }
                                Result re = new Result();
                                re.setCode(209, "此用户无权限");
                                return re;
                            }
//                        }
                    }

                }catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
            }
            try {
                // 记录操作日志...谁..在什么时间..做了什么事情..
                result = joinPoint.proceed();
                params.set("content", content.toString());
                if (!"控制巡视设备".equals(annotation.title())) {
                    post(params);
                }
                return result;
            } catch (BusinessException e) {
                params.set("state", 2);
                params.set("content", content.toString() + "；错误信息：" + e.getMessage());
                post(params);
                throw e;
            } catch (Throwable e) {
                params.set("content", content.toString() + "；异常信息：" + e.getMessage());
                params.set("state", 3);
                post(params);
                throw e;
            }
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            serviceId = logsConfig.getName();
            params.set("logType", annotation.logType());
            params.set("ip", ip);
            params.set("title", "内部接口错误");
            params.set("state", 3);
            params.set("content", "异常信息: " + e.getMessage() + "\n" + getStackMsg(e));
            params.set("userId", userId);
            params.set("userName", userName);
            post(params);
            throw e;
        }
    }

    public void post(MultiValueMap<String, Object> params) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                System.out.println("platformLogAdd...");
                serviceRestTemplate.postForObject(LOG_URL, params, String.class);
            }
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
