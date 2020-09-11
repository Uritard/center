package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/9/10 - 15:01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CruiseAudit对象", description = "巡视审核表")
public class CruiseAudit implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡检任务id")
    private String taskId;

    @ApiModelProperty(value = "巡检点id")
    private Long instanceId;

    @ApiModelProperty(value = "巡检点名称")
    private String instanceName;

    @ApiModelProperty(value = "巡检分析图片")
    private String picpath;

    @ApiModelProperty(value = "巡检结果数值")
    private String resultNum;

    @ApiModelProperty(value = "实际结果")
    private Integer identifyResult;

    @ApiModelProperty(value = "实际结果-字典表")
    private String identifyResult1;

    @ApiModelProperty(value = "评价状态")
    private Integer evaluationState;

    @ApiModelProperty(value = "评价状态-字典表")
    private String evaluationState1;

    @ApiModelProperty(value = "识别状态")
    private Integer identifyState;

    @ApiModelProperty(value = "识别状态-字典表")
    private String identifyState1;

    @ApiModelProperty(value = "人工校核结果")
    private String personcheck;


}
