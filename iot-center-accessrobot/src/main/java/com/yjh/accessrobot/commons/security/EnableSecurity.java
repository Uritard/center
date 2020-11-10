package com.yjh.accessrobot.commons.security;


import com.yjh.accessudp.commons.logs.LogsRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({LogsRegister.class})
public @interface EnableSecurity {
}
