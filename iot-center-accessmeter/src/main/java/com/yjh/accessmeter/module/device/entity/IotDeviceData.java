/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.device.entity;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class IotDeviceData {
    /**
     *
     */
    private Long id;
    /**
     * 测点Id
     */
    private Long pointId;
    /**
     * 测点名称
     */
    private String pointName;
    /**
     * 物联设备Id
     */
    private Long iotDeviceId;
    /**
     * 物联设备名称
     */
    private String iotDeviceName;
    /**
     * 值
     */
    private String value;
    /**
     * 单位
     */
    private String unit;
    /**
     * 上级区域id
     */
    private Long upRegionId;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 系数
     */
    private Float magnificationCoefficient = 1F;
    /**
     * 所属节点
     */
    private String edgeCode;
    /**
     * 设备类型
     */
    private Integer iotDeviceType;
}
