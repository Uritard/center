package com.yjh.platform.module.task.dal;

import lombok.Data;

import java.util.Date;

/**
 * @author zhangyuyi
 * @create 2023-07-19
 */
@Data
public class TStdWeatherLogDO {

    private Long id;

    /**
     * 设备id*
     */
    private String deviceId;
    /**
     * 设备名称*
     */
    private String deviceName;
    /**
     * 值*
     */
    private String deviceValue;
    /**
     * 单位*
     */
    private String unit;
    /**
     * 类型*
     */
    private String type;
    /**
     * 环境设备类型 1状态型 0 正常 1 异常  2数值型 3控制型 0开 1 关*
     */
    private String showType;
    /**
     * 机器人编号*
     */
    private String robotCode;
    /**
     * 状态*
     */
    private String status;
    /**
     * 创建时间*
     */
    private Date createTime;
    /**
     * 更新时间*
     */
    private Date updateTime;
    /**
     * 创建人*
     */
    private String createPerson;
    /**
     * 更新人*
     */
    private String updatePerson;

    private String startTime;

    private String endTime;
}
