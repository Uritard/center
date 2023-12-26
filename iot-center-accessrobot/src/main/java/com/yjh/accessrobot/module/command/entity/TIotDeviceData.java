package com.yjh.accessrobot.module.command.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 物联设备结果表
 * @TableName t_iot_device_data
 */
@Data
public class TIotDeviceData implements Serializable {
    /**
     * Id
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 系数
     */
    private Integer magnificationCoefficient;

    private Integer channelNum;

    private Integer iotDeviceType;

    private String ip;

    private Integer port;

    private String address;

    private Long deviceId;

    private String upRegionName;

    private Long originId;

    private String edgeCode;

    private String createPerson;
    private String updatePerson;

    private Integer controllable;
}
