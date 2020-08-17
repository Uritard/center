package com.yjh.Manager.logs;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Logs {
    //标题
    String title() default "";

    //业务操作
    String code() default "add";

}