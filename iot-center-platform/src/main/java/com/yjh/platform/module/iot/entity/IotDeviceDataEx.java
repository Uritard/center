package com.yjh.platform.module.iot.entity;

import lombok.Data;

/**
 * @Author: lqh
 * @Date: 2023/12/19
 */
@Data
public class IotDeviceDataEx extends TIotDeviceData{

    private String ip;

    private Integer port;

    private String address;

    private Long deviceId;

    private String upRegionName;

    private String channelNum;

    private Integer controllable;

}
