package com.yjh.platform.common.annotation;

import java.lang.annotation.*;

/**
 *
 *解密参数map
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decrypt {
    String value() default "";
}
