package com.yjh.platform.module.task.entity;


import lombok.Data;

import java.util.Date;

@Data
public class TEnvDeviceType {

    //id
    private String typeId;

    //环境设备类型名称
    private String typeName;

    //创建时间
    private Date createTime;

    //环境设备类型
    private String type;

    //单位
    private String unit;

}
