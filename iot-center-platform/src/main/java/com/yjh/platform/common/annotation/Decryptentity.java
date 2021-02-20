package com.yjh.platform.common.annotation;

import java.lang.annotation.*;

/**
 * 解密参数class
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Decryptentity {
    String value() default "";
}
