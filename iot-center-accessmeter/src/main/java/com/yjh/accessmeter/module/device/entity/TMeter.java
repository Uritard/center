package com.yjh.accessmeter.module.device.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * @TableName t_meter
 */
@Data
public class TMeter implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * IP
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 电表地址(10位字符串)
     */
    private String address;

    /**
     * 正向有功总电量
     */
    private String totalPositivePower;
    /**
     * 正向无功总电量
     */
    private String totalPositiveReactivePower;
    /**
     * 反向无功总电量
     */
    private String totalNegativeReactivePower;

    /**
     * 电量采集时间
     */
    private Date collectPowerTime;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 上级区域id*
     */
    private Long upRegionId;
}
