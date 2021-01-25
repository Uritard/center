package com.yjh.platform.module.task.entity;

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
@ApiModel(value = "任务基本信息表", description = "任务基本信息")
public class TaskSimpleInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务ID")
    private String taskId;
    @ApiModelProperty(value = "任务名称")
    private String taskName;
    @ApiModelProperty(value = "任务类型")
    private Integer Type;
    @ApiModelProperty(value = "任务状态")
    private Integer taskState;
    @ApiModelProperty(value = "任务类型名称")
    private String TypeName;
    @ApiModelProperty(value = "任务状态名称")
    private String taskStateName;
    @ApiModelProperty(value = "测点数量")
    private Long deviceMeteCount;
    @ApiModelProperty(value = "视频设备个数")
    private Long cameraCount;
    @ApiModelProperty(value = "机器人点位")
    private Long robotPointsCount;


}
