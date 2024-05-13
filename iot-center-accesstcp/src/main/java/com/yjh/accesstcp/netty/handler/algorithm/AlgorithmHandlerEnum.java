package com.yjh.accesstcp.netty.handler.algorithm;

import com.yjh.accesstcp.netty.handler.IHandlerEnum;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/15
 * @since [产品/模块版本] （可选）
 */
public enum AlgorithmHandlerEnum implements IHandlerEnum {
    /**
     * 消息返回
     * 所有消息返回统一处理
     */
    RESPONSE(RES + "251", "response"),

    RESPONSE_MSG("251", "response"),
    /**
     * 可靠性指标统计查询
     */
    RELIABILITY_INDEX("317", "reliabilityIndex");


    AlgorithmHandlerEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    static {
        for (AlgorithmHandlerEnum value : AlgorithmHandlerEnum.values()) {
            handlerEnumHashMap.put(value.getType(), value);
            handlerEnumHashMap.put(value.getDesc(), value);
        }
    }

    /**
     * 消息类型，若是请求返回消息以R开头
     */
    private final String type;
    /**
     * 协议描述
     */
    private final String desc;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getDesc() {
        return desc;
    }

}
