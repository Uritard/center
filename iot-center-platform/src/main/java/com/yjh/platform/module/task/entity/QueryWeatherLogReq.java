package com.yjh.platform.module.task.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zhangyuyi
 * @create 2023-07-19
 */
@Data
public class QueryWeatherLogReq implements Serializable {

    private String robotCode;

    private String deviceId;

    private String startTime;

    private String endTime;
}
