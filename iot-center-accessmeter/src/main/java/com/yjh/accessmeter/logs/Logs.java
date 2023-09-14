package com.yjh.accessmeter.logs;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Logs {
    //标题
    String title() default "";

    //业务操作
    String code() default "add";

    //业务操作内容
    String content() default "用户操作";

    //日志类型
    int logType() default 1;

    String authority() default "";
}