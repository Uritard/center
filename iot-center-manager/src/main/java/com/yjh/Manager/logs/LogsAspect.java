package com.yjh.Manager.logs;

import com.yjh.Manager.common.restTemplate.ServiceRestTemplate;
import com.yjh.Manager.common.result.BusinessException;
import com.yjh.Manager.common.utils.IPUtil;
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
    private static final Logger log = LoggerFactory.getLogger(IPUtil.class);

    private static final String LOG_URL = "http://iot-center-logs/sysLogs/v1/add";
    @Autowired
    private LogsConfig logsConfig;
    @Autowired
    private RedisTemplate redisTemplate;

    @LoadBalanced
    @Bean(name = "serviceRestTemplate")
    RestTemplate serviceRestTemplate() {
        return new ServiceRestTemplate();
    }

    @Pointcut("execution(* com.yjh..service..*.*(..))")
    public void selectAspect() {
    }

    @Around("selectAspect()")
    public Object checkSecurity(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Logs annotation = signature.getMethod().getAnnotation(Logs.class);
        MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
        StringBuilder content = new StringBuilder("");
        HttpServletRequest request = null;
        String userId = "", userName = "", serviceId = "", ip = "";
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
        if (null != servletRequestAttributes) {
            request = servletRequestAttributes.getRequest();
            if (Objects.nonNull(request.getHeader("userId"))) {
                userId = request.getHeader("userId");
                userName = String.valueOf(redisTemplate.opsForHash().entries("account_lock_times:"+userId).get("userName"));
            } else {
                userId = "18710";
                userName = request.getParameter("userName");
            }

            ip = IPUtil.getRemoteIP(request);
        }
        Object result = null;
        if (annotation != null) {
            try {
                serviceId = logsConfig.getName();
                params.set("logType", serviceId+":"+annotation.code());
                params.set("ip", ip);
                params.set("title", annotation.title());
                params.set("state", 1);
                content.append(Arrays.toString(joinPoint.getArgs()));
                System.out.println("content: "+content.toString());
                params.set("userId", userId);
                params.set("userName", userName);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            try {
                // 记录操作日志...谁..在什么时间..做了什么事情..
                result = joinPoint.proceed();
                params.set("content", "参数：" + content.toString());
                post(params);
                return result;
            } catch (BusinessException e) {
                params.set("content", "参数：" + content.toString() + "；错误信息：" + e.getMessage());
                params.set("state", 2);
                post(params);
                throw e;
            } catch (Throwable e) {
                params.set("content", "参数：" + content.toString() + "；异常信息：" + e.getMessage());
                params.set("state", 3);
                post(params);
                throw e;
            }
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            serviceId = logsConfig.getName();
            params.set("logType", serviceId+":"+annotation.code());
            params.set("ip", ip);
            params.set("title", "内部接口错误");
            params.set("state", 2);
            params.set("content", "参数：" + Arrays.toString(joinPoint.getArgs()) + "；异常信息：" + e.getMessage() + "\n" + getStackMsg(e));
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
            log.error(e.getMessage(), e);
        }
    }

    private static String getStackMsg(Throwable e) {
        try {
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
