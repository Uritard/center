package com.yjh.platform.module.task.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-09-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TUnionTaskAttr对象", description = "联合巡视预案属性表")
public class TUnionTaskAttr implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "关联任务表UUID")
    private String unionId;

    private Long instanceId;

    @ApiModelProperty(value = "测点实例ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "关联巡视设备部位表id")
    private String deviceCustomId;

    @ApiModelProperty(value = "关联巡视点表id")
    private Long pointTaskId;

    @ApiModelProperty(value = "是否支持机器人巡视")
    private Integer ifRobot;

    @ApiModelProperty(value = "是否支持视频巡视")
    private Integer ifVideo;

    @ApiModelProperty(value = "是否支持红外巡视")
    private Integer ifInferad;

    @ApiModelProperty(value = "是否支持人工巡视")
    private Integer ifArtificial;


}
