package com.yjh.gateway.commons.logs;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Logs {
    //标题
    String title() default "";

    //业务编码
    String code() default "";

}
