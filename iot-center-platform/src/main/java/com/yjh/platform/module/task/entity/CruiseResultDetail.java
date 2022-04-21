package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author YC
 * @date 2020/9/7 - 21:19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CruiseResultDetail对象", description = "巡视点结果详情")
public class CruiseResultDetail extends TStdDeviceMete {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "实物编码")
    private String realCode;
    @ApiModelProperty(value = "巡视点数据id")
    private Long cruiseDataId;
    @ApiModelProperty(value = "巡视任务结果id")
    private String cruiseResultId;
    @ApiModelProperty(value = "巡检任务id")
    private String taskResultId;

    @ApiModelProperty(value = "设备id")
    private Long deviceId;
    @ApiModelProperty(value = "巡视设备")
    private String deviceName;

    @ApiModelProperty(value = "巡视点id")
    private Long instanceId;
    @ApiModelProperty(value = "巡视点名称")
    private String instanceName;

    @ApiModelProperty(value = "巡视点类型")
    private Integer cruiseType;
    @ApiModelProperty(value = "巡检方式字典表")
    private String cruiseTypeName;

    @ApiModelProperty(value = "巡视值")
    private String resultNum;

    @ApiModelProperty(value = "巡视执行结果")
    private Integer cruiseResult;
    @ApiModelProperty(value = "巡视执行结果-字典表")
    private String cruiseResultName;

    @ApiModelProperty(value = "巡视异常原因")
    private Integer cruiseAbnormal;
    @ApiModelProperty(value = "巡视异常原因-字典表总")
    private String abnormalType;
    @ApiModelProperty(value = "巡视异常原因-字典表1")
    private String abnormalType1;
    @ApiModelProperty(value = "巡视异常原因-字典表2")
    private String abnormalType2;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "巡视时间")
    private Date cruiseTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "巡视结束时间")
    private Date endTime;

    @ApiModelProperty(value = "评价状态")
    private Integer evaluationState;
    @ApiModelProperty(value = "评价状态-字典表")
    private String evaluationStateName;

    @ApiModelProperty(value = "审核人")
    private String checkUser;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "审核时间")
    private Date checkDate;

    @ApiModelProperty(value = "执行时间")
    private String executeTime;

    @ApiModelProperty(value = "状态评价")
    private Integer identifyState;

    @ApiModelProperty(value = "实际结果")
    private Integer identifyResult;
    @ApiModelProperty(value = "实际结果--字典表")
    private String identifyResultName;

    @ApiModelProperty(value = "人工校核结果")
    private String personCheck;

    @ApiModelProperty(value = "巡检分析图片")
    private String picPath;

    @ApiModelProperty(value = "摄像机id")
    private Long cameraId;

    @ApiModelProperty(value = "预置位id")
    private Long presetId;

    @ApiModelProperty(value = "是否产生告警")
    private Integer isWarn;

    @ApiModelProperty(value = "告警级别--字典表")
    private String alarmLevelName;

    private String voicePath;


}

