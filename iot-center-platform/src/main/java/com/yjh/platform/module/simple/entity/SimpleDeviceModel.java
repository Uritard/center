/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.simple.entity;

import lombok.Data;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2025-06-19
 * @since [产品/模块版本] （可选）
 */
@Data
public class SimpleDeviceModel {
    /**
     * 变电站名称。
     */
    private String stationName;

    /**
     * 变电站编码和中台设备编码保持一致。
     */
    private String stationCode;

    /**
     * 区域ID，自定义的站内区域，可为空。
     */
    private String areaId;

    /**
     * 区域名称。
     */
    private String areaName;

    /**
     * 设备点位ID。
     */
    private String deviceId;

    /**
     * 设备点位名称。
     */
    private String deviceName;

    /**
     * 部件ID，一次设备点位对应所属部件或部位，对应部件时和中台设备编码保持一致，可为空。
     */
    private String componentId;

    /**
     * 部件名称。
     */
    private String componentName;

    /**
     * 间隔ID，一次设备点位对应所在间隔，和中台设备编码保持一致。
     */
    private String bayId;

    /**
     * 间隔名称。
     */
    private String bayName;

    /**
     * 主设备ID，一次设备点位对应点位所属主设备，和中台设备编码保持一致。
     */
    private String mainDeviceId;

    /**
     * 主设备名称。
     */
    private String mainDeviceName;

    /**
     * 主设备类型。
     */
    private String deviceType;

    /**
     * 表计类型。
     */
    private String meterType;

    /**
     * 辅助设施类型。
     */
    private String appearanceType;

    /**
     * 采集/保存文件类型列表。
     */
    private String saveTypeList;

    /**
     * 识别类型列表，格式：多个识别类型，采用“,”分隔。
     */
    private String recognitionTypeList;

    /**
     * 相位<1>:=A相<2>:=B相<3>:=C相，多个相位，采用“,”分隔。
     */
    private String phase;

    /**
     * 备注信息，用于描述设备点位的文字信息。
     */
    private String deviceInfo;

    /**
     * 设备点位支持的数据来源。
     */
    private Integer dataType;

    /**
     * 正常范围下限，区域巡视主机上报上级系统填写（选填）。
     */
    private String lowerValue;

    /**
     * 正常范围上限，区域巡视主机上报上级系统填写（选填）。
     */
    private String upperValue;

    /**
     * 关联视频编码及。
     */
    private String videoPos;

    /**
     * 重要等级<1>:=Ⅰ类<2>:=Ⅱ类<3>:=Ⅲ类。
     */
    private String pointType;

    /**
     * 标签属性，点位标签属性，多个附加属性逗号分隔。
     */
    private String labelAttri;

    /**
     * 柜面坐标，柜面左下角、右下角、右上角、左后下方4点的三维坐标："x1,y1,z1,x2,y2,z2,x3,y3,z3,x4,y4,z4"。
     */
    private String masterCoordinate;

    /**
     * 柜面ID，柜面唯一对应的巡视点位ID；初始时，引用设备ID。
     */
    private String masterPointId;

    /**
     * 照片序号，柜面第几张照片的序号（0表示柜面的整体拼接图片，1、2、3、4、5、6...表示从上往下，从左往右的具体图片序号）。
     */
    private Integer photoNum;

    /**
     * 偏移量，巡视主机在拼接大图上人工选取一个点（该点为人工认为柜子的中心点），图片左上角为（0,0）采用百分比表示（相对左侧及上侧）。
     */
    private String offset;

    /**
     * 自定义参量，简易机器人自定义参数：备用灯光模式：0、1、2、3。
     */
    private String args;
}
