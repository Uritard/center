package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "获取巡检类型&巡检设备绑定ID", description = "巡视点相关数据获取类")
public class CruiseTypeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡检类型")
    private Integer cruiseType;
    @ApiModelProperty(value = "巡检类型名称")
    private String cruiseTypeName;
    @ApiModelProperty(value = "巡检点与摄像头/机器人绑定位ID")
    private Long cruiseId;
}