package com.yjh.demo.aop;


import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

/**
 * Copyright (c) 2021 Yijiahe Technology Co., Ltd. All rights reserved.
 *
 * @author wuyu
 * @date 3/14/21
 */
@Aspect
@Component
@Slf4j
public class LogPrintAspect {


    @Pointcut("within(com.yjh.demo..*) && @within(org.springframework.web.bind.annotation.RestController)")
    public void printParams() {

    }

    /**
     * 打印请求参数
     *
     * @param joinPoint
     */
    @Before(value = "printParams()")
    public void paramsLog(JoinPoint joinPoint) {
        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .ifPresent(request -> {
                    log.info("{} - {}", request.getMethod(), request.getRequestURL());
                    Object[] args = joinPoint.getArgs();
                    for (int i = 0; i < args.length; ++i) {
                        log.info("arg[{}]: {}", i, args[i]);
                    }
                });
    }

    @After(value = "printParams()")
    public void afterLog() {
        Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .ifPresent(request -> {
                    log.info("Done - {}", request.getRequestURI());
                });
    }

}