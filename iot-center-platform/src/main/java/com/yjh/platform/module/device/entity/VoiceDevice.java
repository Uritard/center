package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author lqh
 * @since 2020/12/2
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "VoiceDevice", description = "音频设备树专用")
public class VoiceDevice {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    private String Id;

    @ApiModelProperty(value = "名称")
    private String label;

    @ApiModelProperty(value = "上级区域ID")
    private String upId;

    @ApiModelProperty(value = "上级区域名称")
    private String upName;

    @ApiModelProperty(value = "消息类型")
    private String infoType;

    private String state;

    @ApiModelProperty(value = "子类")
    private List<VoiceDevice> children;

    private String filePath;
}
