package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author YC
 * @date 2020/9/7 - 21:19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CruiseResultDetail对象", description = "巡视点结果详情")
public class CruiseResultDetail {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "实物编码")
    private String realCode;
    @ApiModelProperty(value = "巡检任务id")
    private Integer taskId;

    @ApiModelProperty(value = "设备id")
    private Integer deviceId;
    @ApiModelProperty(value = "巡视设备")
    private String deviceName;

    @ApiModelProperty(value = "巡视点id")
    private Integer instanceId;
    @ApiModelProperty(value = "巡视点名称")
    private String instanceName;

    @ApiModelProperty(value = "巡视点类型")
    private Integer pointType;
    @ApiModelProperty(value = "巡检方式")
    private String cruiseType;

    @ApiModelProperty(value = "巡视值")
    private String resultNum;

    @ApiModelProperty(value = "巡视结果")
    private Integer state;
    @ApiModelProperty(value = "巡检数据状态")
    private String dataState;

    @ApiModelProperty(value = "巡视时间")
    private Date cruiseTime;

    @ApiModelProperty(value = "状态评价")
    private Integer identifyState;
    @ApiModelProperty(value = "识别状态")
    private String identifyState1;


}

