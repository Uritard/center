package com.yjh.logs.commons.logs;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Logs {
    //业务码
    String title();

    //日志类型
    int logType() default 1;

    //操作类型
    String code() default "module";

    //业务操作内容
    String content() default "";
}
