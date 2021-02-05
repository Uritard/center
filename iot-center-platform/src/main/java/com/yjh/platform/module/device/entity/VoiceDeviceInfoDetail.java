package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2021/2/5
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TVoiceDevice对象扩展", description = "声纹设备表扩展")
public class VoiceDeviceInfoDetail extends TVoiceDevice{
    private String dbValue;
}
