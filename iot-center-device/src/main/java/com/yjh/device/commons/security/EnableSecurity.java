package com.yjh.device.commons.security;


import com.yjh.device.commons.logs.LogsRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({LogsRegister.class})
public @interface EnableSecurity {
}
