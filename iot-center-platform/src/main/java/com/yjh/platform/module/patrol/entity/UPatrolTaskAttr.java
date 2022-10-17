package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author lqh
 * @since 2022-10-12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "UPatrolTaskAttr对象", description = "任务关联表")
public class UPatrolTaskAttr implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "关联任务表id")
    private String taskId;

    @ApiModelProperty(value = "巡检点实例ID")
    private Long instanceId;

    @ApiModelProperty(value = "测点实例ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "关联设备id")
    private Long deviceId;

    @ApiModelProperty(value = "关联部位表id")
    private String customId;

    @ApiModelProperty(value = "关联巡视点表id")
    private String pointTaskId;

    @ApiModelProperty(value = "是否支持机器人巡视（暂时没用）")
    private Integer ifRobot;

    @ApiModelProperty(value = "是否支持视频巡视（暂时没用）")
    private Integer ifVideo;

    @ApiModelProperty(value = "是否支持红外巡视（暂时没用）")
    private Integer ifInferad;

    @ApiModelProperty(value = "是否支持人工巡视（暂时没用）")
    private Integer ifArtificial;


}
