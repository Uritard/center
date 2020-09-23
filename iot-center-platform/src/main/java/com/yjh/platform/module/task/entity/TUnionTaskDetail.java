package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/9/23 - 11:38
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TUnionTaskDetail对象", description = "联合巡视预案属性数据")
public class TUnionTaskDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "规则id")
    private Long ruleId;
    @ApiModelProperty(value = "预案ID")
    private Long planId;
    @ApiModelProperty(value = "巡检点实例ID")
    private Long instanceId;
    @ApiModelProperty(value = "测点实例ID")
    private Long deviceMeteId;
    @ApiModelProperty(value = "关联部位表id")
    private String deviceCustomId;
    @ApiModelProperty(value = "巡检任务")
    private Integer cruiseType;
}
