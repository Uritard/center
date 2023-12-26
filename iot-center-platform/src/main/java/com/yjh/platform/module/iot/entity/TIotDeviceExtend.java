package com.yjh.platform.module.iot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.yjh.platform.common.ValidateConstant;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Date;

/**
 * 物联设备表
 * @TableName t_iot_device
 */
@Data
public class TIotDeviceExtend extends TIotDevice {

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

}
