package com.yjh.platform.module.device.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author: lqh
 * @Date: 2022/08/25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DeviceAndInstance {

    private Long deviceId;
    private String deviceName;
    private Long instanceId;
    private String instanceName;
}
