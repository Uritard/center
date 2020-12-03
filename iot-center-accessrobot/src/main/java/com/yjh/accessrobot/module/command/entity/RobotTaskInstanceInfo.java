package com.yjh.accessrobot.module.command.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "RobotTaskInstanceInfo对象", description = "机器人巡检点数据表")
public class RobotTaskInstanceInfo implements Serializable {
    @ApiModelProperty(value = "巡视类型")
    private Integer cruiseType;

    @ApiModelProperty(value = "任务编码")
    private String taskId;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "优先级")
    private Integer priority;

    @ApiModelProperty(value = "设备层级")
    private Integer deviceLevel;

    @ApiModelProperty(value = "机器人唯一标识")
    private String robotCode;

    @ApiModelProperty(value = "机器人巡检点list")
    private List<Long> instanceList;

}
