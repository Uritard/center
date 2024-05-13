package com.yjh.accesstcp.netty.handler;

import java.util.HashMap;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/15
 * @since [产品/模块版本] （可选）
 */
public interface IHandlerEnum {
    String RES = "R";
    Map<String, IHandlerEnum> handlerEnumHashMap = new HashMap<>(32);


    String getType();

    String getDesc();


    static IHandlerEnum getEnm(String type) {
        return handlerEnumHashMap.get(type);
    }
}
