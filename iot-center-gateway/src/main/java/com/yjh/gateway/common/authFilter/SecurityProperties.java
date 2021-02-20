package com.yjh.gateway.common.authFilter;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class SecurityProperties {

    @Value("${spring.security.isDecode}")
    private String isDecode;
}
