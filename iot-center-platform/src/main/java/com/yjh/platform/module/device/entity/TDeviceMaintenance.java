package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;

/**
 * @author lqh
 * @since 2021-01-11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDeviceMaintenance对象", description = "设备区域检修表")
public class TDeviceMaintenance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "检修ID")
    @TableId(value = "maintenance_id", type = IdType.AUTO)
    @TableField(value = "maintenance_id",updateStrategy = FieldStrategy.IGNORED)
    private Long maintenanceId;

    @Length(max = 256,message = "maintenanceName长度必须小于等于256")
    @ApiModelProperty(value = "检修名称")
    @TableField(value = "maintenance_name",updateStrategy = FieldStrategy.IGNORED)
    private String maintenanceName;

    @Length(max = 256,message = "deviceId长度必须小于等于256")
    @TableField(value = "device_ids",updateStrategy = FieldStrategy.IGNORED)
    private String deviceIds;

    @Max(value=99)
    @ApiModelProperty(value = "是否使用，0-不使用，1-使用")
    private Integer isValid;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "开始检修时间")
    private Date maintenanceStart;


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "结束检修时间")
    private Date maintenanceStop;

    @Max(value=999999999)
    private Integer effectiveState;

    @Length(max = 256,message = "effectiveStateName长度必须小于等于256")
    private String effectiveStateName;

    private List<Long> deviceIdList;

    private List<DeviceAndInstance> deviceAndInstanceList;

    @Length(max = 256, message = "coordinatePixel长度必须小于等于256")
    private String coordinatePixel;

    private String deviceLevel;


    @TableField(value = "instance_ids",updateStrategy = FieldStrategy.IGNORED)
    private String instanceIds;


}
