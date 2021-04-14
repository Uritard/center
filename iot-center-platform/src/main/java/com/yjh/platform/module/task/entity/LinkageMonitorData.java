package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @author YC
 * @date 2020/12/17 14:08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "LinkageMonitorData对象", description = "联动弹窗内容-监测数据")
public class LinkageMonitorData {

    @ApiModelProperty(value = "任务id")
    private String taskId;

    @ApiModelProperty(value = "任务名称")
    private String taskName;

    @ApiModelProperty(value = "任务结果id")
    private String taskResultId;

    @ApiModelProperty(value = "巡检点结果id")
    private Long cruiseDataId;

    @ApiModelProperty(value = "巡检点id")
    private Long instanceId;

    @ApiModelProperty(value = "巡检点名称")
    private String instanceName;

    @ApiModelProperty(value = "实物编码")
    private String realCode;

    @ApiModelProperty(value = "巡视设备Id")
    private Long deviceId;

    @ApiModelProperty(value = "巡视设备")
    private String deviceName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "巡检时间")
    private Date cruiseTime;

    @ApiModelProperty(value = "巡视执行结果-正常、异常")
    private Integer cruiseResult;

    @ApiModelProperty(value = "巡视执行结果--字典表")
    private String cruiseResultName;

    @ApiModelProperty(value = "巡检点结果")
    private String resultNum;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "摄像机Id")
    private Long cameraId;

    @ApiModelProperty(value = "预置位Id")
    private Long presetId;

    @ApiModelProperty(value = "巡检结果照片")
    private String picpath;

    @ApiModelProperty(value = "联动设备类型-0机器人,1摄像机")
    private Integer deviceType;

}
