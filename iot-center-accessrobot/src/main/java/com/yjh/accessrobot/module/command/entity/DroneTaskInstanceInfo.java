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
@ApiModel(value = "DroneTaskInstanceInfo对象", description = "无人机巡检点数据表")
public class DroneTaskInstanceInfo implements Serializable {
    @ApiModelProperty(value = "巡视类型")
    private Integer cruiseType;

    @ApiModelProperty(value = "任务编码")
    private String taskId;

    @ApiModelProperty(value = "预案编码")
    private String PlanCode;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "优先级")
    private Integer priority;

    @ApiModelProperty(value = "设备层级")
    private Integer deviceLevel;

    @ApiModelProperty(value = "是否ORC识别 (0:是 1:否)")
    private String isOrc;

    @ApiModelProperty(value = "无人机唯一标识")
    private String droneCode;

    @ApiModelProperty(value = "无人机巡检点list")
    private List<Long> instanceList;
    @ApiModelProperty(value = "执行方式")
    private String ifRun;
    @ApiModelProperty(value = "定期开始时间")
    private String fixedStartTime;
    @ApiModelProperty(value = "周期（月）")
    private String cycleMonth;
    @ApiModelProperty(value = "周期（周）")
    private String cycleWeek;
    @ApiModelProperty(value = "周期（执行时间）")
    private String cycleExecuteTime;
    @ApiModelProperty(value = "周期开始时间")
    private String cycleStartTime;
    @ApiModelProperty(value = "周期结束时间")
    private String cycleEndTime;
    @ApiModelProperty(value = "间隔（数量）")
    private String intervalNumber;
    @ApiModelProperty(value = "间隔（类型）")
    private String intervalType;
    @ApiModelProperty(value = "间隔（执行时间）")
    private String intervalExecuteTime;
    @ApiModelProperty(value = "间隔开始时间")
    private String intervalStartTime;
    @ApiModelProperty(value = "间隔结束时间")
    private String intervalEndTime;
    @ApiModelProperty(value = "是否为联动任务（1-是0否）")
    private String unionTaskStatus;

}
