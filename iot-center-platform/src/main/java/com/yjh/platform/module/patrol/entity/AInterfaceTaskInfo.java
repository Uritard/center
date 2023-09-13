package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @Author: lqh
 * @Date: 2023/09/12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "A接口任务信息", description = "A接口任务信息")
public class AInterfaceTaskInfo {

    private String type;
    private String taskCode;
    private String taskName;
    private String priority;
    private String deviceLevel;
    private String deviceList;
    private String fixedStartTime;
    private String cycleMonth;
    private String cycleWeek;
    private String cycleExecuteTime;
    private String cycleStartTime;
    private String cycleEndTime;
    private String intervalNumber;
    private String intervalType;
    private String intervalExecuteTime;
    private String intervalStartTime;
    private String intervalEndTime;
    private String invalidStartTime;
    private String invalidEndTime;
    private String isenable;
    private String creator;
    private String createTime;
}
