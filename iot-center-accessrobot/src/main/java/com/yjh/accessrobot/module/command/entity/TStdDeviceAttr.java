package com.yjh.accessrobot.module.command.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 标准化设备参数表
 * @TableName t_std_device_attr
 */
@Data
public class TStdDeviceAttr implements Serializable {
    /**
     * 
     */
    private Long deviceId;

    /**
     * 节点编码
     */
    private String edgeCode;

    /**
     * 原始id(下级同步的id)
     */
    private String originId;

    /**
     * 设备型号
     */
    private Integer deviceModel;

    /**
     * PMS类型
     */
    private String pmsType;

    /**
     * PMS ID
     */
    private String pmsId;

    /**
     * 生产厂家
     */
    private String deviceVendor;

    /**
     * 
     */
    private Date productionDate;

    /**
     * 投运时间
     */
    private Date usedTime;

    /**
     * 
     */
    private Date disableDate;

    /**
     * 
     */
    private Date lastMaintenance;

    /**
     * 维修次数
     */
    private String maintenanceCount;

    /**
     * 所属单位
     */
    private String organization;

    /**
     * 管理部门
     */
    private String department;

    /**
     * 责任人
     */
    private String responsiblePerson;

    /**
     * 纬度
     */
    private String latitude;

    /**
     * 经度
     */
    private String longitude;

    /**
     * 设备IP地址
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 电压等级
     */
    private String voltageLevel;

    /**
     * 顺控点号
     */
    private String sequencePoint;

    /**
     * 实物编码
     */
    private String realCode;

    /**
     * 安装地址
     */
    private String address;

    private static final long serialVersionUID = 1L;

}