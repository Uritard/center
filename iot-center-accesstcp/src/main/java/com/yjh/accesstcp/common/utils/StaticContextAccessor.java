package com.yjh.accesstcp.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @author tt on 2019/9/6
 */
@Component
@Slf4j
public class StaticContextAccessor {
    private static StaticContextAccessor instance;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void registerInstance() {
        log.info("StaticContextAccessor registerInstance");
        instance = this;
    }

//    ModelResults results = StaticContextAccessor.getBean(YouDianTabService.class).queryBCode(channel);
    public static <T> T getBean(Class<T> clazz) {
        return instance.applicationContext.getBean(clazz);
    }
}
