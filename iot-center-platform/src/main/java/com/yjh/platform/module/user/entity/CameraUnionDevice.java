package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YChen
 * @date 2021/8/13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CameraUnionDevice",description = "摄像机关联设备实体类")
public class CameraUnionDevice implements Serializable {

    @ApiModelProperty(value = "设备id")
    private Long deviceId;
    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "测点id")
    private Long deviceMeteId;
    @ApiModelProperty(value = "巡检点id")
    private Long instanceId;
    @ApiModelProperty(value = "预置位id/机器人测点id")
    private Long cruiseId;
    @ApiModelProperty(value = "相机id")
    private Long cameraId;
    @ApiModelProperty(value = "nvrId")
    private Long recordId;
    @ApiModelProperty(value = "相机名称")
    private String cameraName;
}
