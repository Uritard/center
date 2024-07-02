package com.yjh.platform.module.patrol.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
@ApiModel(value = "任务查询", description = "任务查询")
public class TaskQuery implements Serializable {
    @ApiModelProperty(required = false, value = "任务名称")
    private String taskName;
    @ApiModelProperty(required = false, value = "开始时间")
    private String startTime;
    @ApiModelProperty(required = false, value = "结束时间")
    private String endTime;
    @ApiModelProperty(required = false, value = "任务是否有效")
    private Integer isValid;
    private Integer pageNum = 1;
    private Integer pageSize = 15;
}
