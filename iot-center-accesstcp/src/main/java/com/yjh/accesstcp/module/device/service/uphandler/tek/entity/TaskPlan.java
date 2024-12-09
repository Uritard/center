/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler.tek.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2024/12/6
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode
@ApiModel(value = "科大下发任务对象", description = "科大下发任务对象")
public class TaskPlan {

    @ApiModelProperty(value = "下发计划编码")
    private String planNo;

    @ApiModelProperty(value = "下发计划名称")
    private String planName;

    @ApiModelProperty(value = "下发任务编码")
    private String taskNo;

    @ApiModelProperty(value = "任务类型：1-定时下发 2-立即下发")
    private int taskType = 1;

    @ApiModelProperty(value = "任务巡检开始时间")
    private String taskTime;

    @ApiModelProperty(value = "下发任务名称")
    private String taskName;

    @ApiModelProperty(value = "下发的点位唯一编码集")
    private String[] infoCodeList;
}
