package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author tt
 * @since 2020-08-27
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseTaskCron对象", description = "巡检任务表")
public class TCruiseTaskCron implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 所选作业类型:
     * 1  -> 每天
     * 2  -> 每月
     * 3  -> 每周
     * 4  -> 每时
     * 5  -> 每分
     * 6  -> 每年
     */
    @ApiModelProperty(value = "作业类型")
    private String jobType;

    /**一周的哪几天*/
    @ApiModelProperty(value = "每周的几天")
    private String[] dayOfWeeks;

    /**一个月的哪几天*/
    @ApiModelProperty(value = "每月的几天")
    private String[] dayOfMonths;

    /**秒  */
    @ApiModelProperty(value = "几秒")
    private String second;

    /**分  */
    @ApiModelProperty(value = "几分")
    private String minute;

    /**时  */
    @ApiModelProperty(value = "几时")
    private String hour;

    /**年  */
    @ApiModelProperty(value = "几年")
    private String year;
}
