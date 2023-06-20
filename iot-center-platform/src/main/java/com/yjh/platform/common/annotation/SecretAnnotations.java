package com.yjh.platform.common.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解加解密.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface SecretAnnotations {
    /**
     * 是否加密.
     */
    boolean encode() default false;

    /**
     * 是否解密.
     */
    boolean decode() default false;
}
