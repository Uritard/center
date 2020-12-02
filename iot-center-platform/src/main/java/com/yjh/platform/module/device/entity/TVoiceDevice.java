package com.yjh.platform.module.device.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020-12-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TVoiceDevice对象", description = "声纹设备表")
public class TVoiceDevice implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "声纹监控设备Id（默认为Ip地址）")
    private String voiceDeviceId;

    @ApiModelProperty(value = "声纹监控设备名称（）")
    private String voiceDeviceName;

    @ApiModelProperty(value = "变压器下面换流变的设备Id")
    private Long stdDeviceId;

    @ApiModelProperty(value = "被监测的设备类型")
    private String deviceType;

    private String configId;


}
