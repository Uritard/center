package com.yjh.accesstcp.commons.security;


import com.yjh.accesstcp.commons.logs.LogsRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({LogsRegister.class})
public @interface EnableSecurity {
}
