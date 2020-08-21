package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author wf
 * @since 2020-08-19
 */

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TUnionTaskAttr", description = "联合巡视预案属性表")
public class TUnionTaskAttr implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "关联任务表UUID")
    @TableId(value = "union_id", type = IdType.AUTO)
    private String UnionId;

    @ApiModelProperty(value = "关联巡检点定义实例表id")
    @TableId(value = "instance_id", type = IdType.AUTO)
    private Long InstanceId;

    @ApiModelProperty(value = "测点实例ID")
    private Long DeviceMeteId;

    @ApiModelProperty(value = "关联巡视设备部位表id")
    private String DeviceCustomId;

    @ApiModelProperty(value = "关联巡视点表id")
    private Long PointTaskId;

    @ApiModelProperty(value = "是否支持机器人巡视")
    private Integer IfRobot;

    @ApiModelProperty(value = "是否支持视频巡视")
    private Integer IfVideo;

    @ApiModelProperty(value = "是否支持红外巡视")
    private Integer IfInferad;

    @ApiModelProperty(value = "是否支持人工巡视")
    private Integer IfArtificial;

}
