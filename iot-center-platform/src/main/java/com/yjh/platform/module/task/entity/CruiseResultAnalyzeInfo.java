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
 * @author YC
 * @date 2021/1/12 17:18
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "巡视结果分析-获取测点下的巡检数据结果", description = "巡检点数据结果")
public class CruiseResultAnalyzeInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "测点id")
    private Long deviceMeteId;

    @ApiModelProperty(value = "巡检时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cruiseTime;

    @ApiModelProperty(value = "数据来源")
    private Integer cruiseType;

    @ApiModelProperty(value = "数据来源--字典表")
    private String cruiseTypeName;

    @ApiModelProperty(value = "巡视点名称")
    private String instanceName;

    @ApiModelProperty(value = "巡检类型")
    private Integer cType;

    @ApiModelProperty(value = "巡检类型--字典表")
    private String cTypeName;

    @ApiModelProperty(value = "巡视结果值")
    private String resultNum;

    @ApiModelProperty(value = "人工审核值")
    private String personCheck;

    @ApiModelProperty(value = "实际结果")
    private Integer identifyResult;

    @ApiModelProperty(value = "实际结果--字典表")
    private String identifyResultName;

    @ApiModelProperty(value = "巡视执行结果")
    private Integer cruiseResult;

    @ApiModelProperty(value = "巡视执行结果--字典表")
    private String cruiseResultName;

    @ApiModelProperty(value = "巡检分析图片")
    private String picPath;

    @ApiModelProperty(value = "识别类型")
    private String meteType;

    @ApiModelProperty(value = "识别类型--字典表")
    private String meteTypeName;

    @ApiModelProperty(value = "表计类型")
    private Integer meterType;

    @ApiModelProperty(value = "表计类型--字典表")
    private String meterTypeName;

    @ApiModelProperty(value = "巡检点结果id")
    private String cruiseResultId;

    @ApiModelProperty(value = "巡视点数据id")
    private Long cruiseDataId;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "设备类型--字典表")
    private String deviceTypeName;

    @ApiModelProperty(value = "区域名称")
    private String regionName;

    @ApiModelProperty(value = "审核状态")
    private String evaluationState;

    @ApiModelProperty(value = "间隔名称")
    private String upRegionName;

    @ApiModelProperty(value = "变电站id")
    private String stationId;

    @ApiModelProperty(value = "变电站名称")
    private String stationName;

    @ApiModelProperty(value = "告警级别")
    private String alarmLevel;

    @ApiModelProperty(value = "区域id")
    private Long regionId;

    @ApiModelProperty(value = "告警级别Desc")
    private String alarmLevelName;

}
