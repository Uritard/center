package com.yjh.accessrobot.module.command.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备区域检修表
 * @TableName t_device_maintenance
 */
@Data
public class TDeviceMaintenance implements Serializable {
    /**
     * 检修ID
     */
    private Long maintenanceId;

    /**
     * 检修名称
     */
    private String maintenanceName;

    /**
     * 设备ID
     */
    private String deviceIds;

    /**
     * 是否使用，0-不使用，1-使用
     */
    private Integer isValid;

    /**
     * 开始检修时间
     */
    private Date maintenanceStart;

    /**
     * 结束检修时间
     */
    private Date maintenanceStop;

    /**
     * 边缘节点编码（从哪个边缘节点同步上来的）
     */
    private String edgeCode;

    /**
     * 
     */
    private String originId;

    /**
     * 设备层级（1 = 间隔 2 = 主设备 3 = 设备点位 4 = 部件）
     */
    private String coordinatePixel;

    /**
     * 设备层级（1 = 间隔 2 = 主设备 3 = 设备点位 4 = 部件）
     */
    private String deviceLevel;

    /**
     * 巡视点id
     */
    private String instanceIds;

    private static final long serialVersionUID = 1L;

}