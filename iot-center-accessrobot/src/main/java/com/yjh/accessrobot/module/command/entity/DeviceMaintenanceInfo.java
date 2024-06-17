package com.yjh.accessrobot.module.command.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * 设备维护信息表
 * @TableName device_maintenance_info
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(exclude={"id", "deviceId", "patroldeviceTypeName", "maintenanceTypeName", "updateTime"})
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
     * 设备编码
     */
    private String patroldeviceCode;

    /**
     * 设备名称
     */
    private String patroldeviceName;

    /**
     * 设备类型
     */
    private Integer patroldeviceType;

    /**
     * 设备类型名称
     */
    @TableField(exist = false)
    private String patroldeviceTypeName;

    /**
     * 维护记录编码
     */
    private String recordCode;

    /**
     * 维护信息类型
     */
    private Integer maintenanceType;

    /**
     * 维护信息类型名称
     */
    @TableField(exist = false)
    private String maintenanceTypeName;

    /**
     * 故障级别 1:一般 2:严重
     */
    private Integer faultLevel;

    /**
     * 维护信息描述
     */
    private String faultDesc;

    /**
     * 维护开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 维护结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 维护单位
     */
    private String maintenanceUnit;

    /**
     * 维护人员
     */
    private String maintenancePerson;

    /**
     * 消缺状态 1-已消缺 2-未消缺 3-消缺中
     */
    private Integer maintenanceStatus;

    /**
     * 备注
     */
    private String remark;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
