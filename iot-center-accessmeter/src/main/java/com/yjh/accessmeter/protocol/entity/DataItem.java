/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/1
 * @since [产品/模块版本] （可选）
 */
@Data
public class DataItem {
    /**
     * 设备位号
     */
    String pIndex;
    /**
     * 状态 0：正常（开） 1：异常（关） 2：故障
     */
    String status;
    /**
     * 单位
     */
    String symbol;
    /**
     * 值
     */
    String textValue;
}
