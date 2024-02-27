/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/23
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class EnvResult extends EnvBase {
    public static final String SUCCESS = "1";
    /**
     * 结果 1成功 2失败
     */
    String value;

    public boolean isSuccess() {
        return SUCCESS.equals(this.value);
    }
}
