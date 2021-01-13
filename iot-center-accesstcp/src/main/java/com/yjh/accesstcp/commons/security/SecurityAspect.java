package com.yjh.accesstcp.commons.security;


import com.yjh.accesstcp.commons.result.BusinessException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @Description
 * @Author tt
 * @Date 2019/7/30
 **/
@Aspect
@Component
public class SecurityAspect {
    @Pointcut("execution(* *..controller..*.*(..))")
    public void controllerAspect() {
    }

    @Around("controllerAspect()")
    public Object checkSecurity(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        PlatformSecurity annotation = signature.getMethod().getAnnotation(PlatformSecurity.class);
        if (annotation != null) {
            // 获取request
            RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
            ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) requestAttributes;
            HttpServletRequest request = servletRequestAttributes.getRequest();
            String[] auth = annotation.auth();
            HttpSession session = request.getSession();
            if (auth.length == 1 && "".equals(auth[0])) {
                //默认注解需要有userId就行
                if (null == session.getAttribute("userId")) {
                    throw new BusinessException(403, "访问被禁止");
                }
            } else {
                if (null == session.getAttribute("sysAdmin")) {
                    List<String> auths = (List<String>) session.getAttribute("auth");
                    if (null == auths) {
                        throw new BusinessException(403, "访问被禁止");
                    } else {
                        boolean flag = false;
                        for (String temp : auth) {
                            if (auths.contains(temp)) {
                                flag = true;
                                break;
                            }
                        }
                        if (!flag) {
                            throw new BusinessException(403, "访问被禁止");
                        }
                    }
                }
            }
        }
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }
}
