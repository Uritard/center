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

    @ApiModelProperty(value = "巡检方式 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹")
    private Integer pointType;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "识别类型")
    private Integer meteType;

    @ApiModelProperty(value = "设备所属区域")
    private Integer regionId;

    @ApiModelProperty(value = "关联巡检点定义实例表名称")
    private String cruiseName;

    @ApiModelProperty(value = "关联巡检点定义实例表名称")
    private String instanceName;

    @ApiModelProperty(value = "部位名称")
    private String customName;

    // 设备名称
    private String deviceName;

    // 测点类型名称
    private String meteTypeName;

    // 巡视设备名称
    private String cruiseDeviceName;

    //数据来源
    private Integer cruiseType;

    // 数据来源名称
    private String cruiseTypeName;

}
