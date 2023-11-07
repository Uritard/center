package com.yjh.platform.module.patrol.event;

import lombok.Data;

/**
 * @Author: lqh
 * @Date: 2023/11/07
 */
@Data
public class InspectionResultEvent {

    private String taskId;

    private String deviceId;

    private String result;
}
