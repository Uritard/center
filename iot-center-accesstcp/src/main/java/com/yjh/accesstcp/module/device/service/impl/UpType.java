/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.impl;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 上级系统类型枚举类
 *
 * @author Chenfei
 * @date 2024/12/4
 * @since [产品/模块版本] （可选）
 */
@Getter
public enum UpType {
    /**
     * 电网标准
     */
    STATE_GRID(Name.STATE_GRID, 1),
    /**
     * 科大上级
     */
    TEK(Name.TEK, 2);

    /**
     * 接口名称
     */
    private final String implName;
    /**
     * 协议类型
     */
    private final int type;

    /**
     * 枚举Map，用以通过 type 快速获取枚举类型
     */
    private static final Map<Integer, UpType> ENUM_MAP = new HashMap<>();

    static {
        for (UpType val : UpType.values()) {
            ENUM_MAP.put(val.getType(), val);
        }
    }

    UpType(String implName, int type) {
        this.implName = implName;
        this.type = type;
    }

    public static UpType getEnum(int type) {
        return ENUM_MAP.getOrDefault(type, UpType.STATE_GRID);
    }

    /**
     * 服务定义 beanName
     */
    public static class Name {

        public static final String STATE_GRID = "stateGridService";
        public static final String TEK = "tekService";
    }
}
