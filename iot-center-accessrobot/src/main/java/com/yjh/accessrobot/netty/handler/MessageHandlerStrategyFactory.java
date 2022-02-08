package com.yjh.accessrobot.netty.handler;

import com.baomidou.mybatisplus.core.toolkit.Assert;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/14
 */
public class MessageHandlerStrategyFactory {
    /**
     * 缓存所有的策略，当前是无状态的，可以共享策略类对象
     */
    private static Map<String,MessageHandlerStrategy> strategies = new ConcurrentHashMap<String,MessageHandlerStrategy>();

    public static MessageHandlerStrategy getStrategyType(String type) {
        Assert.notNull(type, "type should not be empty...");
        return strategies.get(type);
    }

    public static void register(String type,MessageHandlerStrategy strategy){
        Assert.notNull(type, "type should not be empty...");
        strategies.put(type,strategy);
    }

}
