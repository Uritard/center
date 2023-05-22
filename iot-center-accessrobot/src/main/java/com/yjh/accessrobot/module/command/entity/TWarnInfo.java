package com.yjh.accessrobot.module.command.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警信息表
 * @TableName t_warn_info
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TWarnInfo implements Serializable {
    /**
     * 告警ID
     */
    private Long warnId;

    /**
     * 告警等级
     */
    private Integer warnLevel;

    /**
     * 告警时间
     */
    private Date warnTime;

    /**
     * 告警类型
     */
    private Integer warnType;

    /**
     * 告警名称
     */
    private String warnName;

    /**
     * 告警内容
     */
    private String warnContent;

    /**
     * 设备Id
     */
    private Long deviceId;
    /**
     * 设备Id
     */
    private String deviceName;

    /**
     * 部位ID
     */
    private String cunstomId;

    /**
     * 巡检点ID
     */
    private Long instanceId;

    /**
     * 标准测点ID
     */
    private Long stdMeteId;
    /**
     * 标准测点ID
     */
    private String deviceMeteName;

    /**
     * 处理状态1.已核查2.未核查
     */
    private Integer confMode;

    /**
     * 是否告警
     */
    private Integer isWarn;

    /**
     * 是否属实1.属实2.不属实
     */
    private Integer dealType;

    /**
     * 处理意见
     */
    private String dealInfo;

    /**
     * 确认人ID
     */
    private String dealPersonId;

    /**
     * 确认时间
     */
    private Date dealTime;

    /**
     * 缺陷类型
     */
    private Integer defectModel;

    /**
     * 告警来源
     */
    private Integer alarmSource;

    /**
     * 告警子类型
     */
    private Integer warnSubtype;

    /**
     * 设备编码
     */
    private String deviceCode;

    /**
     * 图片地址
     */
    private String imagePath;

    /**
     * 视频地址
     */
    private String videoPath;

    /**
     * 
     */
    private String value;

    /**
     * 
     */
    private String outRange;

    /**
     * 任务ID
     */
    private String taskId;

    private static final long serialVersionUID = 1L;
}