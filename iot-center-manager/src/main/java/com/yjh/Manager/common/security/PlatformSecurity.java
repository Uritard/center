package com.yjh.manager.common.security;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PlatformSecurity {
    String[] auth() default "";
}
