package com.yjh.platform.common.mqtt;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Spring 获取 bean工具类
 */
@Component("getSpring")
public class GetSpringUtil implements ApplicationContextAware {

    private static ApplicationContext context = null;
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        context = applicationContext;
    }

    public static <T> T getBean(String beanName){
        return (T) context.getBean(beanName);
    }

    public static String getMessage(String key){
        return context.getMessage(key, null, Locale.getDefault());
    }

}

