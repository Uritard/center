package com.yjh.accessrobot.module.command.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * 声纹设备表
 * @TableName t_voice_device
 */
@Data
public class TVoiceDevice implements Serializable {
    /**
     * 声纹监控设备Id
     */
    private Long voiceDeviceId;

    /**
     * 声纹监控设备名称
     */
    private String voiceDeviceName;

    /**
     * 变压器下面换流变的设备Id变压器下面换流变的设备Id
     */
    private Long stdDeviceId;

    /**
     * 被监测的设备类型
     */
    private String deviceType;

    /**
     * 
     */
    private Long configId;

    /**
     * 上级区域id
     */
    private Long upRegionId;

    /**
     * 在线状态
     */
    private String state;

    /**
     * 声纹设备编码
     */
    private String voiceCode;

    /**
     * 设备类型
     */
    private String voiceType;

    /**
     * 设备型号
     */
    private String voiceModel;

    /**
     * 生产厂家
     */
    private String voiceFactory;

    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     * 原始id(下级同步的id)
     */
    private String originId;

    private static final long serialVersionUID = 1L;
}