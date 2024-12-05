package com.yjh.accesstcp.module.device.service.uphandler;

import com.yjh.accesstcp.module.device.service.impl.UpType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/7/2
 * @since [产品/模块版本] （可选）
 */
@Component
public class UpHandlerFactory implements BeanPostProcessor {
    /**
     * 缓存所有的策略，当前是无状态的，可以共享策略类对象
     */
    private static final Map<String, IUpHandler> STRATEGIES = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (bean instanceof IUpHandler) {
            IUpHandler handler = (IUpHandler)bean;
            for (String command : handler.command()) {
                STRATEGIES.put(command, handler);
            }
        }
        return bean;
    }

    /**
     * 根据协议获取协议具体实现
     */
    public static IUpHandler getService(@NonNull UpType protocolType, @NonNull String type) {
        Assert.notNull(type, "topic should not be empty...");
        return STRATEGIES.get(protocolType.name() + "_" + type);
    }

}
