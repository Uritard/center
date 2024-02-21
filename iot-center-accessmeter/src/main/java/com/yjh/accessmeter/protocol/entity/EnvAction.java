/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/1
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class EnvAction extends EnvBase {
    /**
     * 设备位号
     */
    String pIndex;
    /**
     * 设备控制 on：开 off ：关
     */
    Action action;

    @Data
    @Accessors(chain = true)
    public static class Action {
        /**
         * 值
         */
        String textValue;

        /**
         * 设备控制扩展属性 空调：1：制热、2：制冷
         */
        Map<String, String> attr;

        public Action addAttr(String name, String value) {
            if (this.attr == null) {
                this.attr = new LinkedHashMap<>(16);
            }
            attr.put(name, value);
            return this;
        }
    }
}
