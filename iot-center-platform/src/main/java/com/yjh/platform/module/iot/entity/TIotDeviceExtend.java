package com.yjh.platform.module.iot.entity;

import lombok.Data;

import java.util.List;

/**
 * 物联设备表
 * @TableName t_iot_device
 */
@Data
public class TIotDeviceExtend extends TIotDevice {

    /**
     * 设备通道
     */
    List<TIotDevicePoint> pointList;

}
