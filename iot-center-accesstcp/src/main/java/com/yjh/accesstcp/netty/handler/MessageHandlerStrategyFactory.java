package com.yjh.accesstcp.netty.handler;

import com.yjh.accesstcp.netty.entiy.BaseModel;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/14
 */
public class MessageHandlerStrategyFactory {

    /**
     * 缓存所有的策略，当前是无状态的，可以共享策略类对象
     */
    private static final Map<String, MessageHandlerStrategy<? extends BaseModel>> STRATEGIES = new HashMap<>();

    private MessageHandlerStrategyFactory() {
        // do nothing
    }

    public static MessageHandlerStrategy<? extends BaseModel> getStrategyType(@NonNull ProtocolEnum protocolType, @NonNull String type) {
        return STRATEGIES.get(protocolType.name() + "_" + type);
    }

    public static void register(@NonNull ProtocolEnum protocolType, @NonNull IHandlerEnum type, MessageHandlerStrategy<? extends BaseModel> strategy) {
        String[] types = type.getType().split(",");
        for (String s : types) {
            STRATEGIES.put(protocolType.name() + "_" + s, strategy);
        }
    }

}
