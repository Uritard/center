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
    MODBUS_RTU(841, "MODBUS_RTU");

    final int code;
    final String desc;

    private static final Map<Integer, ProtocolEnum> CRUISE_ENUM_MAP = new HashMap<>();

    ProtocolEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    static {
        for (ProtocolEnum val : ProtocolEnum.values()) {
            CRUISE_ENUM_MAP.put(val.getCode(), val);
        }
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ProtocolEnum getEnum(int code) {
        return CRUISE_ENUM_MAP.get(code);
    }
}
