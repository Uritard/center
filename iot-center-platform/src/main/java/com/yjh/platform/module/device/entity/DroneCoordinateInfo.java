package com.yjh.platform.module.device.entity;

import lombok.Data;

/**
 * @Author jinyujiang
 * @Description 无人机上传的坐标信息
 * @Date create in 2023/6/7 19:01
 */
@Data
public class DroneCoordinateInfo {
    /**
     * 巡视设备名称
     */
    private String patrolDeviceName;

    /**
     * 巡视设备编码
     */
    private String patrolDeviceCode;

    /**
     * 机器人/无人机code
     */
    private String robotCode;

    /**
     * 时间
     */
    private String time;

    /**
     * 坐标, 格式：”x,y,z,a”，x、y、z为地图文件的坐标，a为巡视设备航向角
     */
    private String coordinatePixel;

    /**
     * 经纬度， 格式：”x，y”
     */
    private String coordinateGeography;
}
