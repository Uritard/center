package com.yjh.accesstcp.module.device.entity;

import lombok.Data;

/**
 * @Author: lqh
 * @Date: 2022/04/19
 */
@Data
public class DeviceStatisticsInfo {

    private Long id;
    private String patrolDeviceName;//巡视设备名称
    private String patrolDeviceCode;//巡视设备编码
    private String commissionTime;//投运时间
    private String reportTime;//上报时间
    private String type;//类型 1-累计在线 2-累计离线 3-累计连续正常 4-正常巡检 5-巡检出勤 6-录像完整
    private String value;//
    private String valueUnit;//
    private String unit;//
}
