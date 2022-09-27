package com.yjh.platform.module.user.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;


import java.io.Serializable;
import java.util.Date;

/**
 * @author YChen
 * @date 2021/8/11
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "AlarmAndMeteInfo对象", description = "三维告警测点信息实体类")
public class AlarmAndMeteInfo implements Serializable {

    @ApiModelProperty(value = "告警Id")
    private Long warnId;
    @ApiModelProperty(value = "设备Id")
    private Long deviceId;
    @ApiModelProperty(value = "测点Id")
    private Long deviceMeteId;
    @ApiModelProperty(value = "巡检点Id")
    private Long instanceId;
    @ApiModelProperty(value = "任务Id")
    private String taskId;
    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "巡检点名称")
    private String instanceName;
    @ApiModelProperty(value = "巡检结果")
    private String resultNum;
    @ApiModelProperty(value = "告警等级")
    private Integer alarmLevel;
    @ApiModelProperty(value = "告警等级-字典表")
    private String alarmLevelName;
    @ApiModelProperty(value = "数据来源")
    private Long cruiseType;
    @ApiModelProperty(value = "数据来源-字典表")
    private String cruiseTypeName;
    @ApiModelProperty(value = "部位名称")
    private String customName;
    @ApiModelProperty(value = "测点名称")
    private String meteName;
    @ApiModelProperty(value = "告警来源")
    private Integer alarmSource;
    @ApiModelProperty(value = "告警来源-字典表")
    private String alarmSourceName;
    @ApiModelProperty(value = "告警内容")
    private String warnContent;
    @ApiModelProperty(value = "告警时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date warnTime;

}
