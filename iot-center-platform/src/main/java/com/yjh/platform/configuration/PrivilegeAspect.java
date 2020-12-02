package com.yjh.platform.configuration;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Aspect
@Component
@Slf4j
public class PrivilegeAspect {

    @Pointcut("@annotation(com.yjh.platform.configuration.PrivilegeInfo)")
    private void checkCut(){ }

    // 前置通知
    @Before("checkCut()")
    public void myBefore(JoinPoint joinPoint) {
        log.info("前置通知，目标：");
        log.info(joinPoint.getTarget() + "方法名称:");
        log.info(joinPoint.getSignature().getName());
    }

    // 前置通知
    @After("checkCut()")
    public void myAfter(JoinPoint joinPoint) {
        log.info("hou置通知，目标：");
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
            log.info("methAccess: "+methAccess);
            Object[] parm = joinPoint.getArgs();
            String[] roleIds = methAccess.split(",");
            for (String roleId:roleIds) {
                if (parm[0].equals(roleId)){
                    log.info("获取到该权限");
                    return joinPoint.proceed();
                }
            }
            log.info("没有执行权限");
        }
        return "环绕结束";
    }
}
