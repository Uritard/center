package com.yjh.platform.module.device.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhangyuyi
 * @create 2023-07-25
 */
@Data
public class TMeterVo implements Serializable {
    /**
     * id
     */
    private Long id;
    /**
     * 正向有功总电量
     */
    private String totalPositivePower;
    /**
     * 正向有功总电量 x 系数
     */
    private String totalPositivePowerLast;
    /**
     * 正向无功总电量
     */
    private String totalPositiveReactivePower;
    /**
     * 正向无功总电量 x 系数
     */
    private String totalPositiveReactivePowerLast;
    /**
     * 反向无功总电量
     */
    private String totalNegativePositivePower;
    /**
     * 反向无功总电量 x 系数
     */
    private String totalNegativePositivePowerLast;

    /**
     * 创建时间
     */
    private String createTime;

    private String startTime;

    private String endTime;

}
