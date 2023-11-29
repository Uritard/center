package com.yjh.platform.module.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 物联设备测点表
 * @TableName t_iot_device_point
 */
@TableName(value ="t_iot_device_point")
@Data
public class TIotDevicePoint implements Serializable {
    /**
     * Id
     */
    @TableId(type = IdType.AUTO)
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

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
