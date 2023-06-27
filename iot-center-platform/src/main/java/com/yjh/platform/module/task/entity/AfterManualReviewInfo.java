package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author YC
 * @date 2021/2/19 13:32
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AfterManualReviewInfo对象", description = "巡视点人工复核后详情表")
public class AfterManualReviewInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "人工校验结果")
    private String personCheck;
    @ApiModelProperty(value = "该巡视点是否产生告警，0-否，1-是")
    private Integer isWarn;
    @ApiModelProperty(value = "巡检任务id")
    private String taskId;
    @ApiModelProperty(value = "巡检任务结果id")
    private String taskResultId;
    @ApiModelProperty(value = "巡检图片")
    private String picPath;
    @ApiModelProperty(value = "巡检点实例id")
    private Long instanceId;
    @ApiModelProperty(value = "人工审核值")
    private Integer identifyResult;
    @ApiModelProperty(value = "审核后的值")
    private String modifyNum;
    @ApiModelProperty(value = "巡视类型")
    private Integer cruiseType;
    @ApiModelProperty(value = "单位")
    private String unit;
    @ApiModelProperty(value = "测点ID")
    private Long deviceMeteId;
    @ApiModelProperty(value = "巡视时间")
    private Date cruiseTime;
}
