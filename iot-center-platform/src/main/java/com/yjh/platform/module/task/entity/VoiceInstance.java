package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/11/6
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class VoiceInstance {

    @ApiModelProperty(value = "巡视点实例Id")
    private Long instanceId;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "部位ID")
    private String customId;

    @ApiModelProperty(value = "测点ID")
    private Long deviceMeteId;

    @ApiModelProperty(value = "测点名称")
    private String meteName;

    @ApiModelProperty(value = "声纹设备编码")
    private String voiceCode;

    @ApiModelProperty(value = "声纹设备名称")
    private String voiceDeviceName;

    @ApiModelProperty(value = "相对路径 imagePath")
    private String imagePath;

    @ApiModelProperty(value = "绝对路径 filePath")
    private String filePath;
}
