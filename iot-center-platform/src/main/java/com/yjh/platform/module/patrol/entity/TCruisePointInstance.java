package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "巡视点所需信息对象", description = "巡视点实例表")
public class TCruisePointInstance implements Serializable {

    @ApiModelProperty(value = "巡视点实例Id")
    private Long instanceId;
    @ApiModelProperty(value = "巡视点Id")
    private Long cruiseid;
    @ApiModelProperty(value = "设备ID")
    private Long deviceId;
    @ApiModelProperty(value = "设备编码")
    private String deviceCode;
    @ApiModelProperty(value = "部位ID")
    private String customId;
    @ApiModelProperty(value = "标准测点ID")
    private Long deviceMeteId;
    @ApiModelProperty(value = "巡视类型")
    private Integer cruiseType;
    @ApiModelProperty(value = "巡视类型名称")
    private String cruiseTypeName;



}
