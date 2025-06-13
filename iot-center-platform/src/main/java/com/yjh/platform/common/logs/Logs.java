package com.yjh.platform.common.logs;

import java.lang.annotation.*;

/**
 * @author tt
 */
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

    /**
     * 日志类型
     * @see com.yjh.manager.common.utils.NumToStringUtil
     */
    int logType() default 1;

    /**
     * 1234	管理员
     * 1235	业务员
     * 1236	审计员
     */
    String authority() default "";

    String codeName() default "";

}