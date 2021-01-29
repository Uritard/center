package com.yjh.platform.common.logs;


import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({LogsRegister.class})
public @interface EnableLogs {
}
