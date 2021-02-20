package com.yjh.platform.common.utils;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
public class SecurityProperties {

    @Value("${spring.security.isDecode}")
    private String isDecode;
}
