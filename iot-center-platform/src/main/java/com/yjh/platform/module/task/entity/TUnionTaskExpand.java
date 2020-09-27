package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * @author YC
 * @date 2020/9/22 - 16:12
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TUnionTaskExpand对象", description = "巡检任务表拓展")
public class TUnionTaskExpand extends TUnionTask{

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "预案id")
    private Long planId;

    @ApiModelProperty(value = "预案名称")
    private String planName;

    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    @ApiModelProperty(value = "规则类型 multi／single",example = "multi")
    private String ruleType;

    @ApiModelProperty(value = "具体治理规则")
    private String ruleContent;

    @ApiModelProperty(value = "描述")
    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "更新时间")
    private Date updateTime;

    @ApiModelProperty(value = "。。。。")
    private String IsFinishName;


}
