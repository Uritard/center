/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/1
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class EnvBase {
    /**
     * 请求数据类型
     */
    String type;
    /**
     * 协议类型编号
     */
    String msgType;
    /**
     * 站所ID
     */
    String StationId;
}
