package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020/10/19
 */@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "cameraInfo", description = "摄像头基本信息")

public class Camera {
    @ApiModelProperty(value = "摄像头ID")
    private String cameraId;

    @ApiModelProperty(value = "摄像头名称")
    private String cameraName;

    @ApiModelProperty(value = "摄像头类型，1：普通相机，2：机器人可见光 3：机器人红外")
    private int cameraType;
}
