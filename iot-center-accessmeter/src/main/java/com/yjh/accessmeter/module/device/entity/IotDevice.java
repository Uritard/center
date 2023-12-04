/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.device.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@Data
public class IotDevice {
    /**
     *
     */
    @EqualsAndHashCode.Include
    private Long id;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 设备IP
     */
    private String ip;
    /**
     * 设备端口
     */
    private Integer port;
    /**
     * 设备地址
     */
    private String address;
    /**
     * 协议类型
     */
    private Integer iotDeviceType;
    /**
     * 协议模式 TCP-ACTIVE,TCP-PASSIVE
     */
    private String protocolModel;
    /**
     * 关联设备Id
     */
    private Long deviceId;
    /**
     * 上级区域id
     */
    private Long upRegionId;
    /**
     * 上级区域名称
     */
    private String upRegionName;
    /**
     * 创建时间
     */
    private Date createTime;
    /**
     * 创建人
     */
    private String createPerson;
    /**
     *
     */
    private Date updateTime;
    /**
     *
     */
    private String updatePerson;
    /**
     * 区域编码
     */
    private String edgeCode;
    /**
     * 系数
     */
    private Integer magnificationCoefficient;
    /**
     * 采集频率 单位:分钟
     */
    private Integer collectionFrequency;
    /**
     * 单位
     */
    private String unit;
}
