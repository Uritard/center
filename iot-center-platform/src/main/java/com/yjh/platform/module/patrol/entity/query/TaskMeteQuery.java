package com.yjh.platform.module.patrol.entity.query;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@ApiModel(value = "任务测点查询", description = "任务测点查询")
public class TaskMeteQuery implements Serializable {

    @ApiModelProperty(required = true, value = "任务id")
    private String taskId;

    @ApiModelProperty(required = false, value = "测点名称")
    private String meteName;

    @ApiModelProperty(required = false, value = "巡视设备Id")
    private List<Long> cruiseDeviceList;

    @ApiModelProperty(required = false, value = "数据来源")
    private List<Integer> cruiseTypeList;

    @ApiModelProperty(required = false, value = "测点类型")
    private List<Integer> meteTypeList;

    private Integer pageNum = 1;

    private Integer pageSize = 15;
}
