package com.yjh.accesstcp.module.device.callback;

import com.yjh.accesstcp.netty.entiy.BaseModel;
import com.yjh.accesstcp.netty.handler.IHandlerEnum;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/5/10
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class CallbackHandlerStrategyFactory {
    /**
     * 缓存所有的策略，当前是无状态的，可以共享策略类对象
     */
    private static final Map<String, CallbackHandlerStrategy<? extends BaseModel>> LISTENER = new HashMap<>();

    private CallbackHandlerStrategyFactory() {
        // do nothing
    }

    public static CallbackHandlerStrategy<? extends BaseModel> getStrategyType(@NonNull String callbackKey) {
        log.info("获取 callbackKey = {}", callbackKey);
        return LISTENER.remove(callbackKey);
    }


    public static void createCallback(@NonNull String callbackKey, CallbackHandlerStrategy<? extends BaseModel> strategy) {
        log.info("创建 callbackKey = {}", callbackKey);
        LISTENER.put(callbackKey, strategy);
    }
}
