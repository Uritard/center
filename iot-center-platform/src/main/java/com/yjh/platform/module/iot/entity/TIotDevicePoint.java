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
    @Length(max = 128)
    @Pattern(regexp= ValidateConstant.REG_RICH_NAME, message = ValidateConstant.MSG_RICH_NAME)
    private String iotDeviceName;

    /**
     * 通道号
     */
    private String channelNum;

    /**
     * 测点名称
     */
    @Length(max = 128)
    @Pattern(regexp= ValidateConstant.REG_RICH_NAME, message = ValidateConstant.MSG_RICH_NAME)
    private String pointName;

    /**
     * 单位
     */
    @Length(max = 10)
    private String unit;

    /**
     * 额外参数配置
     */
    @Length(max = 256)
    @Pattern(regexp= ValidateConstant.REG_DEVICE_PROPERTIES, message = ValidateConstant.MSG_DEVICE_PROPERTIES)
    private String extend;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
