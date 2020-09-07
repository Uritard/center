package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author YC
 * @date 2020/9/5 - 11:21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseResultExpand对象", description = "巡检任务结果表扩充")
public class TCruiseResultExpand extends TCruiseResult {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "异常数")
    private String taskAbnormal;

    @ApiModelProperty(value = "当前状态")
    private String cruiseState;

    @ApiModelProperty(value = "任务状态")
    private Integer cruiseStatus;

    @ApiModelProperty(value = "巡检类型")
    private String cruiseType;
}
