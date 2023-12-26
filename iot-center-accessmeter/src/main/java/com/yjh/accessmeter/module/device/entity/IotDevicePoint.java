/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.device.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class IotDevicePoint {
    /**
     *
     */
    private Long id;
    /**
     * 物联设备Id
     */
    private Long iotDeviceId;
    /**
     * 物联设备名称
     */
    private String iotDeviceName;
    /**
     * 通道号
     */
    private Integer channelNum;
    /**
     * 测点名称
     */
    private String pointName;
    /**
     * 单位
     */
    private String unit;
    /**
     * 额外参数，如起始位置，返回数据，json字符串存储
     * {"slaveId": 1,"start": "起始位置 0x43", "length": 返回长度}
     */
    private String extend;
}
