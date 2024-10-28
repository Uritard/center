/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol;

import java.util.HashMap;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/27
 * @since [产品/模块版本] （可选）
 */
public enum ProtocolEnum {
    /**
     * 协议类型
     */
    MODBUS_RTU("1", "MODBUS_RTU", 1),
    MODBUS_TCP("2", "MODBUS_TCP", 1),
    RS485("3", "RS485", 1),
    DLT645_97("4", "DLT645_97", 1),
    DLT645_07("5", "DLT645_07", 1),
    ENV_TERMINAL("6", "环控终端", 1),
    ROBOT("7", "机器人", 0),
    AI_GATEWAY("9", "智能网关", 1),
    NAN("NAN", "未定义协议", 0)
    ;

    /**
     * 协议编码
     */
    final String code;
    /**
     * 协议说明
     */
    final String desc;
    /**
     * 协议类型，0-不建立连接 1-主动连接获取 2-主动连接等待推送 3-开启端口等待被连接后推送
     */
    final int type;


    private static final Map<String, ProtocolEnum> CRUISE_ENUM_MAP = new HashMap<>();

    ProtocolEnum(String code, String desc, int type) {
        this.code = code;
        this.desc = desc;
        this.type = type;
    }

    static {
        for (ProtocolEnum val : ProtocolEnum.values()) {
            CRUISE_ENUM_MAP.put(val.getCode(), val);
        }
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public int getType() {
        return type;
    }

    public static ProtocolEnum getEnum(String code) {
        return CRUISE_ENUM_MAP.getOrDefault(code, NAN);
    }
}
