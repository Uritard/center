package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author YC
 * @date 2020/9/9 - 9:36
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CruiseManualReview对象", description = "巡视点人工复核表")
public class CruiseManualReview {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡检任务id")
    private Integer taskId;
    @ApiModelProperty(value = "巡检点实例id")
    private Integer instanceId;
    @ApiModelProperty(value = "人工校核结果")
    private String personcheck;
    @ApiModelProperty(value = "评价状态")
    private Integer evaluationState;
    @ApiModelProperty(value = "评价状态-字典表")
    private String evaluationState1;
    @ApiModelProperty(value = "实际结果")
    private Integer identifyResult;
    @ApiModelProperty(value = "实际结果-字典表")
    private String identifyResult1;
    @ApiModelProperty(value = "识别状态")
    private Integer identifyState;
    @ApiModelProperty(value = "识别状态-字典表")
    private String identifyState1;

}
