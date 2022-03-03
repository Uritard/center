package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.util.Date;

/**
* 环境设备告警实体类
*/


@Data
public class TEnvWarning {


    //环境告警ID
    private String envWarnId;

    private String robotName;

    //机器人id
    private String robotCode;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date time;

    //环境设备类型
    private String type;

    //值
    private String value;

    //单位
    private String unit;

    private String valueUnit;

    //环境设备SN编码
    private String sn;

    //取值类型
    private String valueType;

    //环境设备告警时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date alarmTime;

    //环境设备名称
    private String huan ;


}

