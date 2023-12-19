/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/12/18
 * @since [产品/模块版本] （可选）
 */
@Data
public class ModbusExtend {
    /**
     * slaveId
     */
    private Integer slaveId;
    /**
     * 起始位置
     */
    private Integer start;
    /**
     * 返回数据长度
     */
    private Integer length;
    /**
     * 返回数据类型，com.serotonin.modbus4j.code.DataType
     */
    private Integer dataType;
    /**
     * 系数
     */
    private Float coefficient;
}
