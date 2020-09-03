package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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


    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "部位ID")
    private String customId;

    @ApiModelProperty(value = "设备编码")
    private String deviceCode;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "别名")
    private String aliasName;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "点号位置，inside-内部设备，outside-外部设备")
    private String positionType;

    @ApiModelProperty(value = "模版ID")
    private Long modelId;

    @ApiModelProperty(value = "路径")
    private String regionPath;

    @ApiModelProperty(value = "上级区域id")
    private Long upRegionId;

    @ApiModelProperty(value = "上级区域名称")
    private String upRegionName;

    @ApiModelProperty(value = "部位名称")
    private String customName;

    @ApiModelProperty(value = "部位类型")
    private Integer customType;

    @ApiModelProperty(value = "设备状态(0：新建，1：在线，2：离线)")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;


}
