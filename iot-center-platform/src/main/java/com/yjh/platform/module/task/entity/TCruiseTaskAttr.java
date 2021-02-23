package com.yjh.platform.module.task.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;

/**
 * @author tt
 * @since 2020-09-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseTaskAttr对象", description = "任务关联表")
public class TCruiseTaskAttr implements Serializable {

    private static final long serialVersionUID = 1L;

    @Length(max = 50,message = "taskId长度必须小于等于50")
    @ApiModelProperty(value = "关联任务表id")
    @TableField(value = "task_id",updateStrategy = FieldStrategy.IGNORED)
    private String taskId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "巡检点实例ID")
    @TableField(value = "instance_id",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "测点实例ID")
    @TableField(value = "device_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceMeteId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "关联设备id")
    @TableField(value = "device_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceId;

    @Length(max = 32,message = "customId长度必须小于等于32")
    @ApiModelProperty(value = "关联部位表id")
    @TableField(value = "custom_id",updateStrategy = FieldStrategy.IGNORED)
    private String customId;

    @Length(max = 32,message = "pointTaskId长度必须小于等于32")
    @ApiModelProperty(value = "关联巡视点表id")
    @TableField(value = "point_task_id",updateStrategy = FieldStrategy.IGNORED)
    private String pointTaskId;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否支持机器人巡视")
    private Integer ifRobot;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否支持视频巡视")
    private Integer ifVideo;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否支持红外巡视")
    private Integer ifInferad;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否支持人工巡视")
    private Integer ifArtificial;
    private Integer pageNum = 1;

    private Integer pageSize = 0;

}
