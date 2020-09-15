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
@ApiModel(value = "获取当前任务下各种巡视方式下的巡检点个数", description = "不同巡视方式下的巡检点个数")
public class CruiseCountOfType implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡检方式ID")
    private String cruiseType;
    @ApiModelProperty(value = "巡检方式名称")
    private String cruiseTypeName;
    @ApiModelProperty(value = "巡检点数量")
    private Integer Count;
}
