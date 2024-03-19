package com.yjh.platform.module.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;

import com.yjh.platform.common.ValidateConstant;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Pattern;

/**
 * 物联设备测点表
 * @TableName t_iot_device_point
 */
@TableName(value ="t_iot_device_point")
@Data
@Accessors(chain = true)
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
    @Length(max = 64)
    @Pattern(regexp= ValidateConstant.REG_RICH_NAME, message = "设备名称" + ValidateConstant.MSG_RICH_NAME)
    private String iotDeviceName;

    /**
     * 通道号
     */
    @Length(max = 32, message = "通道号长度必须小于等于32")
    @Pattern(regexp= ValidateConstant.REG_SPECIAL_CHARACTERS, message = "通道号" + ValidateConstant.MSG_SPECIAL_CHARACTERS)
    private String channelNum;

    /**
     * 测点名称
     */
    @Length(max = 64, message = "测点名称长度必须小于等于64")
    @Pattern(regexp= ValidateConstant.REG_RICH_NAME, message = "测点名称" + ValidateConstant.MSG_RICH_NAME)
    private String pointName;

    /**
     * 单位
     */
    @Length(max = 32, message = "单位长度必须小于等于32")
    private String unit;

    /**
     * 额外参数配置
     */
    @Length(max = 256, message = "额外参数长度必须小于等于256")
    @Pattern(regexp= ValidateConstant.REG_DEVICE_PROPERTIES, message = "额外参数" + ValidateConstant.MSG_DEVICE_PROPERTIES)
    private String extend;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
