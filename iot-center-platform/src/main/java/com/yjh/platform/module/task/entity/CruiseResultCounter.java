package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseTask对象", description = "巡检任务表")
public class CruiseResultCounter implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "异常结果个数" )
    private Integer abnormalCount;

    @ApiModelProperty(value = "已巡视结果个数")
    private Integer cruisedCount;

    @ApiModelProperty(value = "未巡视结果个数")
    private Integer cruiseNotCount;
}

