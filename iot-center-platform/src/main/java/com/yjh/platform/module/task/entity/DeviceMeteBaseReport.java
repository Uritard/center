package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "巡视测点信息", description = "巡视结果分析报表-element")
public class DeviceMeteBaseReport implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "测点名称")
    private String deviceMeteName;
    @ApiModelProperty(value = "测点类型")
    private String meteTypeName;
    @ApiModelProperty(value = "巡视点数量")
    private Integer cruiseCounts;

}
