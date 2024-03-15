package com.yjh.platform.module.iot.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @Author: lqh
 * @Date: 2023/12/19
 */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Data
public class IotDeviceDataEx extends TIotDeviceData{

    private String ip;

    private Integer port;

    private String address;

    private Long deviceId;

    private String upRegionName;

    private String channelNum;

    private Integer controllable;

    private String state;

    private String type;

    private String robotCode;

    private String extend;
}
