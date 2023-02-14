package com.yjh.accessrobot.module.command.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.stream.Stream;

/**
 * 标准化设备表
 * @TableName t_std_device
 */
@Data
public class TStdDevice implements Serializable {
    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 别名
     */
    private String aliasName;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 点号位置，inside-内部设备，outside-外部设备
     */
    private String positionType;

    /**
     * 模版ID
     */
    private Long modelId;

    /**
     * 路径
     */
    private String regionPath;

    /**
     * 上级区域id
     */
    private Long upRegionId;

    /**
     * 上级区域名称
     */
    private String upRegionName;

    /**
     * 部位类型
     */
    private Integer customType;

    /**
     * 设备状态(0：新建，1：在线，2：离线)
     */
    private Integer status;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 预置位id
     */
    private Long presetId;

    /**
     * 摄像机id
     */
    private Long cameraId;

    /**
     * 原始id(下级同步的id)
     */
    private String originId;

    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     *
     */
    private String realCode;

    private static final long serialVersionUID = 1L;

}