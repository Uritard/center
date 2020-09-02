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
public class CameraInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "摄像头ID")
    private Long cameraId;

    @ApiModelProperty(value = "摄像头名称")
    private String cameraName;

    @ApiModelProperty(value = "摄像头码流地址")
    private String URL;
}
