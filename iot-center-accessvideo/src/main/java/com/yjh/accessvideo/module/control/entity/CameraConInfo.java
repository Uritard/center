package com.yjh.accessvideo.module.control.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/8/12 - 21:39
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CameraConInfo对象", description = "摄像机控制信息")
public class CameraConInfo implements Serializable {

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

    @ApiModelProperty(value = "录像机ID")
    private Long recordId;

    @ApiModelProperty(value = "服务器地址")
    private String recordIp;

    @ApiModelProperty(value = "NVR用户名")
    private String identityManager;

    @ApiModelProperty(value = "NVR密码")
    private String identityCode;

    @ApiModelProperty(value = "控制端口")
    private Integer rtspPort;

    @ApiModelProperty(value = "相机用户名")
    private String cameraManager;

    @ApiModelProperty(value = "相机密码")
    private String cameraCode;

    @ApiModelProperty(value = "相机IP")
    private String cameraIp;

    @ApiModelProperty(value = "相机本身通道号")
    private Integer cameraNum;

    @ApiModelProperty(value = "相机控制端口")
    private Integer port;

}
