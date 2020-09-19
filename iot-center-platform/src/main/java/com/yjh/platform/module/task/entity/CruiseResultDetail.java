package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

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
    private String taskId;

    @ApiModelProperty(value = "设备id")
    private Long deviceId;
    @ApiModelProperty(value = "巡视设备")
    private String deviceName;

    @ApiModelProperty(value = "巡视点id")
    private Long instanceId;
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

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "巡视时间")
    private Date cruiseTime;

    @ApiModelProperty(value = "评价状态")
    private Integer evaluationState;
    @ApiModelProperty(value = "评价状态-字典表")
    private String evaluationStateName;


}

