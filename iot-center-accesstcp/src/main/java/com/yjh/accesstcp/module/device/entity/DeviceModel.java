package com.yjh.accesstcp.module.device.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2021/1/14
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "设备模型", description = "设备模型封装类")
public class DeviceModel {
    private String deviceId;
    private String deviceName;
    private String componentId;
    private String componentName;
    private String bayId;
    private String bayName;
    private String mainDeviceId;
    private String mainDeviceName;
    private String deviceType;
    private String meterType;
    private String appearanceType;
    private String saveTypeLis;
    private String recognitionTypeList;
    private String phase;
    private String deviceInfo;
    private String dataType;
    private String lowerValue;
    private String upperValue;
    private String videoPos;
}
