package com.yjh.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Copyright (c) 2021 Yijiahe Technology Co., Ltd. All rights reserved.
 *
 * @author zilong
 * @date 2/18/21
 */
@Configuration
public class BeanConfig {

    public BeanConfig() {
    }

    //设置允许跨域请求
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        //设置允许跨域请求的域名
                        .allowedOriginPatterns("*")
                        //这里：是否允许证书 不再默认开启
                        .allowCredentials(true)
                        //设置允许的方法
                        .allowedMethods("*")
                        //跨域允许时间
                        .maxAge(3600);
            }
        };
    }
}
