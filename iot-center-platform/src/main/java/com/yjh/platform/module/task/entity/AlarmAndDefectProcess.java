package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2021/1/14 14:21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AlarmAndDefectProcess对象", description = "告警和缺陷核查信息表")
public class AlarmAndDefectProcess implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "告警ID")
    private Long warnId;

    @ApiModelProperty(value = "是否属实1.属实2.不属实")
    private Integer dealType;

    @ApiModelProperty(value = "处理意见")
    private String dealInfo;

    @ApiModelProperty(value = "缺陷类型")
    private Integer defectModel;

}
