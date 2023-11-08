package com.yjh.platform.module.video.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 根据 NVR 和相机信息填充当前类信息
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CameraControl 对象", description = "摄像机控制信息，根据NVR和相机填充")
public class CameraControl implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "摄像头id")
    private Long cameraId;

    @ApiModelProperty(value = "通道号")
    private Integer channelNum;

    @ApiModelProperty(value = "摄像机类型，205-可见光，206-红外")
    private Integer cameraType;

    @ApiModelProperty(value = "预置位id")
    private Long presetId;

    @ApiModelProperty(value = "预置位号")
    private Integer presetNum ;

    @ApiModelProperty(value = "IP地址")
    private String ip;

    @ApiModelProperty(value = "通信端口")
    private int port;

    @ApiModelProperty(value = "视频流端口")
    private int rtspPort;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "密码")
    private String identityCode;

    @ApiModelProperty(value = "厂家")
    private String vendor;

    @ApiModelProperty(value = "相机通道ID")
    private String cameraChannelId;

    @ApiModelProperty(value = "设备ID")
    private String deviceChannel;

    @ApiModelProperty(value = "是否是录像机")
    private boolean isRecord;
}
