package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * @author lqh
 * @since 2020/11/20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RobotTaskInstanceInfo implements Serializable {

    private Integer cruiseType;

    private String taskId;

    @ApiModelProperty(value = "预案编码")
    private String planCode;

    private String taskName;

    private Integer priority;

    private Integer deviceLevel = 3;

    private List<Long> instanceList;

    private String robotCode;
    @ApiModelProperty(value = "是否ORC识别 (0:是 1:否)")
    private String isOcr;
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
    @ApiModelProperty(value = "边缘节点ID")
    private String edgeCode;

}
