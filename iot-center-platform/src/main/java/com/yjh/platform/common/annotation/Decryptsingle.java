package com.yjh.platform.common.annotation;

import java.lang.annotation.*;

/**
 * 解密参数get参数
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decryptsingle {
    String value() default "";
}
