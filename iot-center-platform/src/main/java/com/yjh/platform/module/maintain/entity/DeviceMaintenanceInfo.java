package com.yjh.platform.module.maintain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 设备维护信息表
 * @TableName device_maintenance_info
 */
@TableName(value ="device_maintenance_info")
@Data
public class DeviceMaintenanceInfo implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 设备id
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 设备类型
     */
    private Integer deviceType;

    /**
     * 设备类型名称
     */
    @TableField(exist = false)
    private String deviceTypeName;

    /**
     * 维护信息类型
     */
    private Integer maintenanceType;

    /**
     * 物联设备类型名称
     */
    @TableField(exist = false)
    private String maintenanceTypeName;

    /**
     * 维护信息描述
     */
    private String maintenanceDesc;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
