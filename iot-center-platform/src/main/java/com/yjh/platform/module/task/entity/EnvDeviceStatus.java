package com.yjh.platform.module.task.entity;

import lombok.Data;

@Data
public class EnvDeviceStatus {
    /**
     * 厂商的环境设备编码
     */
    private String deviceId;
    /**
     * 环境设备状态, 0 正常 1 异常  0开 1 关
     */
    private Integer status;
    /**
     * 环境设备单位
     */
    private String unit;
    /**
     * 环境设备值
     */
    private String deviceValue;
    /**
     * 环境设备名称
     */
    private String deviceName;
    /**
     * 环境设备类型 1状态型 0 正常 1 异常  2数值型 3控制型 0开 1 关
     */
    private String showType;
    /**
     * 环境设备类型code码
     */
    private String code;
    /**
     * 环境设备类型
     */
    private String type;

    private String robotCode;
}
