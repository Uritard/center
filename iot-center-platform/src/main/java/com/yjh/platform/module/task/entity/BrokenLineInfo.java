package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date cruiseTime;

    @ApiModelProperty(value = "巡视点名称")
    private String instanceName;

    @ApiModelProperty(value = "巡视数据")
    private String resultNum;

    @ApiModelProperty(value = "巡视结果描述")
    private String resultDesc;

    @ApiModelProperty(value = "巡视类型")
    private int cruiseType;
    @ApiModelProperty(value = "巡视类型名称")
    private String cruiseTypeName;

    @ApiModelProperty(value = "巡视设备ID")
    private String cruiseDeviceId;

    @ApiModelProperty(value = "巡视设备名称")
    private String cruiseDeviceName;

    @ApiModelProperty(value = "任务类型")
    private Integer cType;

    @ApiModelProperty(value = "测点名称")
    private String meteName;

    @ApiModelProperty(value = "任务类型--字典表")
    private String cTypeName;

    @ApiModelProperty(value = "识别类型")
    private String meteType;

    @ApiModelProperty(value = "识别类型--字典表")
    private String meteTypeName;

    @ApiModelProperty(value = "表计类型")
    private Integer meterType;

    @ApiModelProperty(value = "表计类型--字典表")
    private String meterTypeName;
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

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

}
