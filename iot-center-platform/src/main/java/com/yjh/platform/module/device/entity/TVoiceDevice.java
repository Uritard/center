package com.yjh.platform.module.device.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;

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

    @Length(max = 64,message = "voiceDeviceId长度必须小于等于64")
    @ApiModelProperty(value = "声纹监控设备Id（默认为Ip地址）")
    private String voiceDeviceId;

    @Length(max = 255,message = "voiceDeviceName长度必须小于等于255")
    @ApiModelProperty(value = "声纹监控设备名称（）")
    private String voiceDeviceName;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "变压器下面换流变的设备Id")
    private Long stdDeviceId;

    @Length(max = 64,message = "deviceType长度必须小于等于64")
    @ApiModelProperty(value = "被监测的设备类型")
    private String deviceType;

    @Length(max = 32,message = "configId长度必须小于等于32")
    private String configId;


}
