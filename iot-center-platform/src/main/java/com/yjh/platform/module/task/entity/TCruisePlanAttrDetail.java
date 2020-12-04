package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author tt
 * @since 2020-09-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePlanAttr对象", description = "巡检预案属性表")
public class TCruisePlanAttrDetail implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "预案ID")
    private Long planId;

    @ApiModelProperty(value = "关联巡检点定义实例表id")
    private Long instanceId;

    @ApiModelProperty(value = "关联巡检点定义实例表名称")
    private String cruiseName;

    @ApiModelProperty(value = "关联巡检点定义实例表名称")
    private String instanceName;

    @ApiModelProperty(value = "设备id")
    private Long deviceId;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "部位id")
    private Long customId;

    @ApiModelProperty(value = "部位名称")
    private String customName;

    @ApiModelProperty(value = "巡检方式 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹")
    private Integer cruiseType;

    @ApiModelProperty(value = "询价类型名称名称")
    private String cruiseTypeName;

    @ApiModelProperty(value = "巡检方式 1视频 2机器人 3红外 4在线监测 5SCADA 6声纹")
    private Integer pointType;

    @ApiModelProperty(value = "任务子类型")
    private Integer subType;

    @ApiModelProperty(value = "区域ID")
    private String areaId;

    @ApiModelProperty(value = "巡检区域id")
    private String cruiseRegionIds;

    @ApiModelProperty(value = "巡视异常类型:0无，1.外观缺陷异常，2.多源对比异常，3.数值越限异常")
    private Integer exceptionType;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "机器人点位或预置位点位或红外预置位")
    private String position;

    @ApiModelProperty(value = "算法实例ID")
    private Long algorithmId;

    @ApiModelProperty(value = "红外诊断公式id")
    private String inferadAnalyze;

    @ApiModelProperty(value = "红外预置位温度框")
    private String irTempBox;

    @ApiModelProperty(value = "创建时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @ApiModelProperty(value = "更新时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;


}
