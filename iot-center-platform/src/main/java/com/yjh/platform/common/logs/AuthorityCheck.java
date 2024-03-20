package com.yjh.platform.common.logs;

import java.lang.annotation.*;

/**
 * @author zhangyuyi
 * @create 2024-03-13
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface AuthorityCheck {

    //业务操作内容
    String content() default "用户操作";

    String authority();
}
