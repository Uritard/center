package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

/**
 * @author tt
 * @since 2020-07-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDevice对象", description = "标准化设备表")
public class TStdDevice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "设备ID")
    @TableField(value = "device_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceId;

    @Length(max = 32, message = "customId长度必须小于等于32")
    @ApiModelProperty(value = "部位ID")
    @TableField(value = "custom_id",updateStrategy = FieldStrategy.IGNORED)
    private String customId;

    @Length(max = 40, message = "deviceCode长度必须小于等于40")
    @ApiModelProperty(value = "设备编码")
    @TableField(value = "device_code",updateStrategy = FieldStrategy.IGNORED)
    private String deviceCode;

    @Length(max = 128, message = "deviceName长度必须小于等于128")
    @ApiModelProperty(value = "设备名称")
    @TableField(value = "device_name",updateStrategy = FieldStrategy.IGNORED)
    private String deviceName;

    @Length(max = 128, message = "aliasName长度必须小于等于128")
    @ApiModelProperty(value = "别名")
     @TableField(value = "alias_name",updateStrategy = FieldStrategy.IGNORED)
    private String aliasName;

    @Max(value = 99999999)
    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @Length(max = 20, message = "positionType长度必须小于等于255")
    @ApiModelProperty(value = "点号位置，inside-内部设备，outside-外部设备")
    @TableField(value = "position_type",updateStrategy = FieldStrategy.IGNORED)
    private String positionType;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "模版ID")
     @TableField(value = "model_id",updateStrategy = FieldStrategy.IGNORED)
    private Long modelId;

    @Length(max = 255, message = "regionPath长度必须小于等于255")
    @ApiModelProperty(value = "路径")
     @TableField(value = "region_path",updateStrategy = FieldStrategy.IGNORED)
    private String regionPath;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "上级区域id")
    @TableField(value = "up_region_id",updateStrategy = FieldStrategy.IGNORED)
    private Long upRegionId;

    @Length(max = 64, message = "upRegionName长度必须小于等于64")
    @ApiModelProperty(value = "上级区域名称")
     @TableField(value = "up_region_name",updateStrategy = FieldStrategy.IGNORED)
    private String upRegionName;

    @Length(max = 32, message = "customName长度必须小于等于32")
    @ApiModelProperty(value = "部位名称")
     @TableField(value = "custom_name",updateStrategy = FieldStrategy.IGNORED)
    private String customName;

    @Max(value = 99999999)
    @ApiModelProperty(value = "部位类型")
    private Integer customType;

    @Max(value = 9)
    @ApiModelProperty(value = "设备状态(0：新建，1：在线，2：离线)")
    private Integer status;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
