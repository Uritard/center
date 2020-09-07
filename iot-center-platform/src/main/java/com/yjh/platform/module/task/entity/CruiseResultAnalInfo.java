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
@ApiModel(value = "巡视结果分析-获取测点下的巡检数据结果", description = "巡检点数据结果")
public class CruiseResultAnalInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "识别时间")
    private Date endTime;
    @ApiModelProperty(value = "点位名称")
    private String instanceName;
    @ApiModelProperty(value = "巡检类型")
    private int cruiseType;
    @ApiModelProperty(value = "巡检类型名")
    private String cruiseTypeName;
    @ApiModelProperty(value = "采集方式")
    private int cType;
    @ApiModelProperty(value = "采集方式名称")
    private String cTypeName;
    @ApiModelProperty(value = "识别结果")
    private int identifyState;
    @ApiModelProperty(value = "识别结果名称")
    private String identifyStateName;
    @ApiModelProperty(value = "采集信息")
    private String resultNum;
    @ApiModelProperty(value = "识别图片")
    private String picPath;
}