package com.yjh.platform.module.patrol.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2020-10-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseTaskResult对象", description = "任务点状态表")
public class TCruiseTaskResult implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "任务结果UUID")
    @TableField(value = "task_result_id",updateStrategy = FieldStrategy.IGNORED)
    private String taskResultId;

    @ApiModelProperty(value = "巡检任务ID")
    @TableField(value = "task_id",updateStrategy = FieldStrategy.IGNORED)
    private String taskId;

    private String taskName;

    @ApiModelProperty(value = "异常数量")
    private Integer taskAbnormal;

    private Integer taskAlarm;

    @ApiModelProperty(value = "执行类型")
    @TableField(value = "run_execute",updateStrategy = FieldStrategy.IGNORED)
    private String runExecute;

    @ApiModelProperty(value = "巡检时间")
    private Date cruiseTaskTime;

    @ApiModelProperty(value = "状态:0-进行中 1-已完成 2-未完成 3-任务中断")
    private Integer taskStatus;

    @ApiModelProperty(value = "巡视结果：0-正常 1-异常")
    private Integer cruiseResult;

    @ApiModelProperty(value = "备注")
     @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;


}
