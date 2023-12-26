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
 * 物联设备结果表
 * @TableName t_iot_device_data
 */
@TableName(value ="t_iot_device_data")
@Data
public class TIotDeviceData implements Serializable {
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 系数
     */
    private Float magnificationCoefficient;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;

    private Integer iotDeviceType;
}
