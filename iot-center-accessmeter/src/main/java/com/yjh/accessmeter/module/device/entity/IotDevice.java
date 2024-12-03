/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.device.entity;

import lombok.*;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@Getter
@Setter
@ToString
@Accessors(chain = true)
public class IotDevice {
    /**
     *
     */
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
     * 设备类型
     */
    private Integer iotDeviceType;
    /**
     * 协议类型，MODBUS, RS485, DLT645
     */
    private String protocolModel;
    /**
     * 电表类型  子表-0  总表-1
     */
    private Integer meterType;
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
    private Float magnificationCoefficient;
    /**
     * 采集频率 单位:分钟
     */
    private int collectionFrequency;
    /**
     * 单位
     */
    private String unit;

    /**
     * 是否可以控制 0-不可控制 1-可以控制
     */
    private Integer controllable;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IotDevice device = (IotDevice)o;

        return new EqualsBuilder().append(id, device.id).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).toHashCode();
    }

    public String getInetAddr() {
        return this.ip + ":" + this.port;
    }
}
