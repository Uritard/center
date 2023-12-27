package com.yjh.accessmeter.module.device.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Author: lqh
 * @Date: 2023/12/19
 */
@Data
@Accessors(chain = true)
public class IotDeviceDataEx extends IotDeviceData{

    private String ip;

    private Integer port;

    private String address;

    private Long deviceId;

    private String upRegionName;

    private String channelNum;

    private Integer controllable;

}
