package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "折线图类", description = "巡视结果分析折线图信息")
public class BrokenLineInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long deviceMeteId;
    @ApiModelProperty(value = "采集时间")
    private Date endTime;
    @ApiModelProperty(value = "巡视数据")
    private String ResultNum;
    @ApiModelProperty(value = "巡视类型")
    private int cruiseType;

    @ApiModelProperty(value = "查询开始与结束时间")
    private Date startDate;
    private Date endDate;
    @ApiModelProperty(value = "查询条件-巡检方式")
    private Integer cType;

    @ApiModelProperty(value = "巡视类型名称")
    private String cruiseTypeName;
}
