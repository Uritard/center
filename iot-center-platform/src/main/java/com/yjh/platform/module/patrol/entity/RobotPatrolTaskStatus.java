package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RobotPatrolTaskStatus对象", description = "机器人/无人机任务状态")
public class RobotPatrolTaskStatus implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "所属机器人/无人机")
    private String robotCode;

    @ApiModelProperty(value = "巡视任务执行ID")
    private String taskPatrolledId;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务编码")
    private String taskCode;

    @ApiModelProperty(value = "任务状态(1:已执行 2:正在执行 3:暂停 4:终止 5:未执行 6:超期)")
    private String taskState;

    @ApiModelProperty(value = "计划开始时间")
    private String planStartTime;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "任务进度")
    private String taskProgress;

    @ApiModelProperty(value = "任务预计剩余时间")
    private String taskEstimatedTime;

    @ApiModelProperty(value = "描述")
    private String description;
}
