package com.yjh.device.commons.logs;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @Description
 * @Author tt
 * @Date 2019/9/17
 **/
@Component
public class SpringBeanUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 设置applicationContext
     * 覆盖ApplicationContextAware方法
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {

        if (Objects.isNull(SpringBeanUtils.applicationContext)) {
            SpringBeanUtils.applicationContext = applicationContext;
        }
    }

    /**
     * 获取applicationContext
     *
     * @return applicationContext
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }


    /**
     * 根据bean名称和bean class获取bean
     *
     * @param name
     * @param clazz
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        try {
            return getApplicationContext().getBean(name, clazz);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }
}
