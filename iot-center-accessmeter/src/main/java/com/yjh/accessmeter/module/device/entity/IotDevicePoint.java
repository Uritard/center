/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.device.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@Data
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

}
