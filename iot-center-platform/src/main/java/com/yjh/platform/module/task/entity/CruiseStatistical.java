package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author YC
 * @date 2020/9/7 - 11:41
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CruiseStatistical对象", description = "巡视点结果统计")
public class CruiseStatistical {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "未完成")
    private Integer dataState1;
    @ApiModelProperty(value = "正常")
    private Integer dataState2;
    @ApiModelProperty(value = "异常")
    private Integer dataState3;
    @ApiModelProperty(value = "算法超时")
    private Integer dataState4;
    @ApiModelProperty(value = "抓图失败")
    private Integer dataState5;
    @ApiModelProperty(value = "未识别")
    private Integer dataState6;
    @ApiModelProperty(value = "数据异常")
    private Integer dataState7;
}
