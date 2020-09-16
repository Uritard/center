package com.yjh.platform.module.task.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author tt
 * @since 2020-08-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "InstanceTree", description = "巡检点树详情")
public class InstanceTree implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "巡检点实例ID")
    @TableId(value = "instance_id", type = IdType.AUTO)
    private Long instanceId;

    @ApiModelProperty(value = "测点实例ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "变电站id")
    private String stationId;

    @ApiModelProperty(value = "变电站名称")
    private String stationName;

    @ApiModelProperty(value = "关联设备id")
    private Long deviceId;

    @ApiModelProperty(value = "关联部位表id")
    private String customId;

    @ApiModelProperty(value = "点位识别类型 1. 表计读数，2位置状态识别，3外观缺陷识别，4红外测温，5声音检测")
    private Integer identifyType;

    @ApiModelProperty(value = "巡检点类型 4001：摄像头 4002：机器人")
    private Integer cruiseType;

    @ApiModelProperty(value = "巡检点类型 4001：摄像头 4002：机器人")
    private String cruiseTypeName;

    @ApiModelProperty(value = "巡检点ID")
    private Long cruiseId;

    @ApiModelProperty(value = "巡检点名称")
    private String cruiseName;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "设备部位名称")
    private String customName;

    @ApiModelProperty(value = "巡检点名称")
    private String instanceName;

    @ApiModelProperty(value = "机器人名称")
    private String robotName;

    @ApiModelProperty(value = "相机名称")
    private String cameraName;

    @ApiModelProperty(value = "红外相机名称")
    private String inferadName;

}