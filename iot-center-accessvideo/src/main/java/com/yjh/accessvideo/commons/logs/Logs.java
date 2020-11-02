package com.yjh.accessvideo.commons.logs;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Logs {
    //标题
    String title() default "";

    //业务编码
    String code() default "";

    //业务操作内容
    String content() default "用户操作";

}
