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
@ApiModel(value = "TCfgAlarmHistory对象扩展", description = "历史告警表扩展")
public class TCfgAlarmHistoryDetail extends TCfgAlarmHistory {

    private static final long serialVersionUID = 1L;

    private String unionId;

    private Integer taskAbnormal;

    private Date cruiseTaskTime;

    private Integer cruiseResult;

    private String alarmLeverName;

    private String confirmStateName;

    private String defectLevelName;

}
