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
@ApiModel(value = "任务运行时间对象", description = "运行时间格式")
public class CruiseTaskRunningTime implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "天")
    private Long Day;

    @ApiModelProperty(value = "时")
    private Long Hour;

    @ApiModelProperty(value = "分")
    private Long Minute;

    @ApiModelProperty(value = "秒")
    private Long Seconds;
}