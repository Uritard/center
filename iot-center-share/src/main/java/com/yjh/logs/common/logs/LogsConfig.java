package com.yjh.logs.common.logs;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description  配置文件读取
 * @Author tt
 * @Date 2019/9/12
 **/
@Component
@ConfigurationProperties("spring.application")
@EnableConfigurationProperties({LogsConfig.class})
public class LogsConfig {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
