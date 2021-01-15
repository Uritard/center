package com.yjh.accessudp.commons.logs;


import com.yjh.accessudp.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessudp.commons.result.BusinessException;
import com.yjh.accessudp.commons.utils.http.IPUtil;
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
import java.util.Arrays;
import java.util.Objects;

/**
 * @Description
 * @Author tt
 * @Date 2019/7/30
 **/
@Aspect
@Component
public class LogsAspect {
    private static final Logger logger = LoggerFactory.getLogger(IPUtil.class);

    private static final String LOG_URL = "http://energy-platform-logs/log/add";
    @Autowired
    private LogsConfig logsConfig;

    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    RestTemplate serviceRestTemplate() {
        return new ServiceRestTemplate();
    }

    @Autowired
    private RedisTemplate redisTemplate;

    @Pointcut("execution(* com.yjh..service..*.*(..))")
    public void selectAspect() {
    }

    @Around("selectAspect()")
    public Object checkSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Logs annotation = signature.getMethod().getAnnotation(Logs.class);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        StringBuilder content = new StringBuilder("");
        String userId = "18714", userName = null, serviceId = null, ip = null;
        HttpServletRequest request = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        if (null != servletRequestAttributes) {
            request = servletRequestAttributes.getRequest();
            if (Objects.nonNull(request.getHeader("userId")) && !Objects.equals(request.getHeader("userId"), "undefined")) {
                userId = request.getHeader("userId");
                userName = String.valueOf(redisTemplate.opsForHash().entries("account_lock_times:"+userId).get("userName"));
            } else {
                userName = request.getParameter("userName");
            }
        }
        ip = IPUtil.getRemoteIP(request);
        Object result = null;
        if (annotation != null) {
            try {
                serviceId = logsConfig.getName();
                params.set("serviceId", serviceId);
                params.set("state", 1);
                params.set("title", annotation.title());
                params.set("userId", userId);
                params.set("userName", userName);
                params.set("code", annotation.code());
                params.set("ip", ip);
                content.append(content.toString());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                // 记录操作日志...谁..在什么时间..做了什么事情..
                result = joinPoint.proceed();
                params.set("content", content.toString());
                post(params);
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
            params.set("logType", serviceId);
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

    private void post(MultiValueMap<String, Object> params) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(LOG_URL, params, String.class);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static String getStackMsg(Throwable e) {
        try {
            StringBuffer sb = new StringBuffer();
            sb.append(e.getMessage() + "\n");
            StackTraceElement[] stackArray = e.getStackTrace();
            for (int i = 0; i < stackArray.length; i++) {
                StackTraceElement element = stackArray[i];
                sb.append(element.toString() + "\n");
            }
            return sb.toString();
        } catch (Exception e1) {
            return "";
        }
    }
}
