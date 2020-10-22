package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author lqh
 * @since 2020/10/22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgAlarmHistory对象联动结果", description = "历史告警表联动结果")
public class TCfgAlarmHistoryResultInfo extends TCruiseDataResult {

    private String taskId;

    private Date cruiseTime;

    private Date endTime;

    private Long deviceId;

    private String deviceName;

    private String stateName;


}
