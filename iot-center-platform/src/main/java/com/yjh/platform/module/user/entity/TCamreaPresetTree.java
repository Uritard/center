package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author tt
 * @since 2020-07-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCamreaPresetTree", description = "摄像头预置位树")
public class TCamreaPresetTree implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    private Long cameraId;

    @ApiModelProperty(value = "摄像机名称")
    private String cameraName;

    @ApiModelProperty(value = "预置位id")
    private Long presetId;

    @ApiModelProperty(value = "预置位名称")
    private String presetName ;

    @ApiModelProperty(value = "相机类型")
    private Long cameraType;

}
