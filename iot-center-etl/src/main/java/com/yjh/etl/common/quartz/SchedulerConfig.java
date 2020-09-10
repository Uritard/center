package com.yjh.etl.common.quartz;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.quartz.SchedulerFactoryBeanCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

@Configuration
public class SchedulerConfig implements SchedulerFactoryBeanCustomizer {
    @Value("${spring.quartz.schedulerName}")
    private String schedulerName;

    @Override
    public void customize(SchedulerFactoryBean schedulerFactoryBean) {
        schedulerFactoryBean.setBeanName(schedulerName);
    }
}
