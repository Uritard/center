package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/12/11 15:21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CruiseStatistical对象", description = "巡视点结果统计")
public class CruiseStatistical implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡视异常原因")
    private Integer cruiseAbnormal;
    @ApiModelProperty(value = "巡视异常原因-字典表")
    private String abnormalType;
    @ApiModelProperty(value = "次数")
    private Integer count;


}
