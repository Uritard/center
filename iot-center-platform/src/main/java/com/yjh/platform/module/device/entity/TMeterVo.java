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
     * 正向无功总电量
     */
    private String totalPositiveReactivePower;
    /**
     * 反向无功总电量
     */
    private String totalNegativePositivePower;

    /**
     * 创建时间
     */
    private String createTime;

    private String startTime;

    private String endTime;

}
