package com.yjh.accesstcp.commons.logs;



import com.yjh.accesstcp.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accesstcp.commons.result.BusinessException;
import com.yjh.accesstcp.commons.utils.http.IPUtil;
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
import java.util.Objects;

/**
 * @Description
 * @Author tt
 * @Date 2019/7/30
 **/
@Aspect
@Component
public class LogsAspect {
    private static final Logger log = LoggerFactory.getLogger(IPUtil.class);

    private static final String LOG_URL = "http://iot-center-logs/sysLog/v1/add";
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
        String userId = "99999", userName = null, serviceId = null, ip = null;
        HttpServletRequest request = null;
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        if (null != servletRequestAttributes) {
            request = servletRequestAttributes.getRequest();
            if (Objects.nonNull(request.getHeader("userId")) && !Objects.equals(request.getHeader("userId"), "undefined")) {
                userId = request.getHeader("userId");
                userName = String.valueOf(redisTemplate.opsForHash().entries("userInfo:"+userId).get("userName"));
            } else { userName = "admin"; }
        }
//        ip = IPUtil.getRemoteIP(request);
        ip = request.getRemoteHost();
        Object result = null;
        if (annotation != null) {
            try {
//                serviceId = logsConfig.getName();
                params.set("logType", annotation.logType());
                params.set("ip", ip);
                params.set("title", annotation.title());
                params.set("state", 1);
                content.append(annotation.content());
                params.set("userId", userId);
                params.set("userName", userName);
                params.set("requestOrigin", request.getRequestURL());
                params.set("requestPath", request.getRequestURI());
                params.set("requestMethod", request.getMethod());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
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
