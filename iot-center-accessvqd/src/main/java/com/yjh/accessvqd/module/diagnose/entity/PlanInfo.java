package com.yjh.accessvqd.module.diagnose.entity;


import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "诊断计划对象-origin", description = "视频诊断-诊断计划")
public class PlanInfo{

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "诊断任务ID")
    private String diagnosePlanId;

    @ApiModelProperty(value = "诊断任务名称")
    private String planName;

    @ApiModelProperty(value = "诊断类型(-1即时计划 0星期计划)")
    private String planType;

    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty(value = "用户名")
    private String userName;

    @ApiModelProperty(value = "任务开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    @ApiModelProperty(value = "任务结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

}
