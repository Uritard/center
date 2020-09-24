package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "cameraInfo", description = "摄像头基本信息")
public class CameraOfRobotInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    @ApiModelProperty(value = "机器人ID")
    private Long robotId;
    @ApiModelProperty(value = "可见光IP")
    private String lightIp;
    @ApiModelProperty(value = "可见光Port")
    private String lightPort;
    @ApiModelProperty(value = "可见光用户名")
    private String lightUserName;
    @ApiModelProperty(value = "可见光密码")
    private String lightPassword;
    @ApiModelProperty(value = "红外IP")
    private String inferadIp;
    @ApiModelProperty(value = "红外Port")
    private String inferadPort;
    @ApiModelProperty(value = "红外用户名")
    private String inferadUserName;
    @ApiModelProperty(value = "红外密码")
    private String inferadPassword;
}