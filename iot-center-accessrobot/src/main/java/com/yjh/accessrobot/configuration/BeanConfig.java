package com.yjh.accessrobot.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author hyh
 * @since 2022/8/24
 **/
@Component
public class BeanConfig {

    @Value("${heart.beat.interval}")
    private Integer heartBeatInterval;

    @Bean
    public String getIntervalValue() {
        return (heartBeatInterval * 60 * 1000) + "";
    }
}
