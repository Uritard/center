package com.yjh.platform.module.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

/**
 * 物联设备告警表
 * @TableName t_iot_device_warn
 */
@TableName(value ="t_iot_device_warn")
@Data
public class TIotDeviceWarn implements Serializable {
    /**
     * Id
     */
    @TableId(type = IdType.AUTO)
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
     * 物联设备类型
     */
    private Integer iotDeviceType;

    /**
     * 物联设备类型名称
     */
    @TableField(exist = false)
    private String iotDeviceTypeName;

    /**
     * 告警信息
     */
    private String alarmContent;

    /**
     * 机器人code
     */
    private String robotCode;
    /**
     * 通道号
     */
    private String channelNum;

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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 系数
     */
    private Integer magnificationCoefficient;

    /**
     * 告警时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date alarmTime;

    /**
     * 告警消除标志 1未消除 2 已消除
     */
    private Long deleteFlag;

    /**
     * 消除时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date deleteTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
