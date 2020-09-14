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
@ApiModel(value = "任务基本信息表", description = "任务基本信息")
public class TaskSimpleInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务ID")
    private Long taskId;
    @ApiModelProperty(value = "任务名称")
    private String taskName;
    @ApiModelProperty(value = "任务类型")
    private Integer Type;
    @ApiModelProperty(value = "任务类型名称")
    private String TypeName;
    @ApiModelProperty(value = "任务来源")
    private Integer taskType;
    @ApiModelProperty(value = "任务来源名称")
    private String taskTypeName;
}
