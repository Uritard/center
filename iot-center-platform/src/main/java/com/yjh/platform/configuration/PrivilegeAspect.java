package com.yjh.platform.configuration;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

@Aspect
@Component
@Slf4j
public class PrivilegeAspect {

    @Autowired
    private RedisTemplate redisTemplate;

    @Pointcut("@annotation(com.yjh.platform.configuration.PrivilegeInfo)")
    private void checkCut(){ }

    // 后置通知
    @After("checkCut()")
    public void myAfter(JoinPoint joinPoint) {
        log.info("后置通知，目标：");
        log.info(joinPoint.getTarget() + "方法名称:");
        log.info(joinPoint.getSignature().getName());
    }

    @Around("checkCut()")
    public Object parse(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("环绕开始");

        // 获取目标方法
//        Method method = targetClassName.getMethod(methodName);
        Signature signature = joinPoint.getSignature();

        MethodSignature methodSignature = (MethodSignature) signature;
        Method method = methodSignature.getMethod();
        String methAccess = "";

        // 判断注解中是否存在@PrivilegeInfo注解
        if(method.isAnnotationPresent(PrivilegeInfo.class)){
            PrivilegeInfo privilegeInfo = method.getAnnotation(PrivilegeInfo.class);
            // 获取到注解中的name值
            methAccess = privilegeInfo.roleIds();
            String roleIdParam = "";
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            HttpServletRequest request = servletRequestAttributes.getRequest();
            if (Objects.nonNull(request.getHeader("appKey"))) {
                String appKey = request.getHeader("appKey");
                Map<String, Object> map = (Map<String, Object>)redisTemplate.opsForHash().entries(appKey);
                roleIdParam = String.valueOf(map.get("roleId"));
            }
            String[] roleIds = methAccess.split(",");
            for (String roleId:roleIds) {
                if (roleIdParam.equals(roleId)){
                    log.info("获取到该权限");
                    return joinPoint.proceed();
                }
            }
            log.info("没有执行权限");
        }
        return "环绕结束";
    }
}
