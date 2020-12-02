package com.yjh.platform.configuration;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//注解的作用目标：方法
@Target(ElementType.METHOD)
//注解传递存活时间，在运行时可以通过反射获取到
@Retention(RetentionPolicy.RUNTIME)
public @interface PrivilegeInfo {
    //角色ID
    String roleIds() default "";
    //角色名称
    String roleName() default "";
}
