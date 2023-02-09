package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AnalysePatrolTaskResult对象", description = "算法分析巡视结果")
public class AnalysePatrolTaskResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "任务id")
    private String taskId;

    @ApiModelProperty(value = "巡视点id'")
    private String instanceId;

    @ApiModelProperty(value = "巡视结果值")
    private String resultValue;

    @ApiModelProperty(value = "算法类型")
    private String analyseType;
    @ApiModelProperty(value = "巡视结果图片")
    private String analyseResultImg;

    @ApiModelProperty(value = "红外fir文件")
    private String firDocPath;

    @ApiModelProperty(value = "巡视结果值描述")
    private String resultDesc;

    @ApiModelProperty(value = "置信度")
    private String conf;

}
