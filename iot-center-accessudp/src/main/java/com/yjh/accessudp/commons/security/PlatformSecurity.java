package com.yjh.accessudp.commons.security;


import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface PlatformSecurity {
    String[] auth() default "";
}
