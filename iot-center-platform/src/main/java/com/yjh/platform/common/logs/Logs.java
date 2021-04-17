package com.yjh.platform.common.logs;

import java.lang.annotation.*;

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