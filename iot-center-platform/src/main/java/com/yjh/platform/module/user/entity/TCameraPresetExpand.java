package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author YC
 * @date 2020/9/9 - 16:53
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCameraPresetExpand对象", description = "摄像机预置位表扩展")
public class TCameraPresetExpand extends TCameraPreset{

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "摄像头名称")
    private String cameraName;

    private String presetType;
}
