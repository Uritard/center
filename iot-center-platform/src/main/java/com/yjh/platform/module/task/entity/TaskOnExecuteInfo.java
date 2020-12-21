package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author lqh
 * @since 2020/12/15
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "正在执行的任务", description = "正在执行的任务数据")
public class TaskOnExecuteInfo {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务id" )
    private String taskId;

    @ApiModelProperty(value = "任务名称" )
    private String taskName;


    @ApiModelProperty(value = "任务开始时间" )
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty(value = "任务巡视类型" )
    private String cruiseTypeName;

    @ApiModelProperty(value = "任务进度" )
    private Integer taskProgress;

    @ApiModelProperty(value = "异常结果个数" )
    private Integer alarmCount;

    @ApiModelProperty(value = "已巡视结果个数")
    private Integer cruisedCount;

    @ApiModelProperty(value = "未巡视结果个数")
    private Integer cruiseNotCount;

    @ApiModelProperty(value = "运行时间")
    private Long runningTime;
}
