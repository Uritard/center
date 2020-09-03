package com.yjh.gateway.commons.security;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PlatformSecurity {
    String[] auth() default "";
}
