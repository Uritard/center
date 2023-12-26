package com.yjh.accessmeter.module.device.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
     * 电表协议 DL/T645协议 1997/2007
     */
    private String protocol;

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
    private String totalNegativePositivePower;

    /**
     * 电量采集时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date collectPowerTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * 上级区域id*
     */
    private Long upRegionId;

    /**
     * 电表耗电量计算系数
     */
    private String magnificationCoefficient;
    /**
     * 正向有功总电量与上一次的差值
     */
    private String totalPositivePowerDifferenceValue;
}
