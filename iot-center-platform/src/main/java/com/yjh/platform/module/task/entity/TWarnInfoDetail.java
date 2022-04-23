package com.yjh.platform.module.task.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import java.io.Serializable;
import java.util.Date;

/**
 * @author lqh
 * @since 2020/9/23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TWarnInfoDetail对象", description = "告警信息表扩展")
public class TWarnInfoDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "告警Id")
    private Long warnId;

    @ApiModelProperty(value = "告警时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date alarmTime;

    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;
    @ApiModelProperty(value = "告警级别-字典表")
    private String alarmLevelName;

    @ApiModelProperty(value = "处理状态")
    private Integer confMode;
    @ApiModelProperty(value = "处理状态-字典表")
    private String confModeName;

    @ApiModelProperty(value = "告警来源")
    private Integer alarmSource;
    @ApiModelProperty(value = "告警来源-字典表")
    private String alarmSourceName;

    @ApiModelProperty(value = "设备id")
    private Long deviceId;
    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "告警内容")
    private String warnContent;

    @ApiModelProperty(value = "告警图片")
    private String imagePath;

    @ApiModelProperty(value = "处理意见")
    private String dealInfo;
    @ApiModelProperty(value = "处理方法")
    private Integer dealType;
    @ApiModelProperty(value = "处理方法-字典表")
    private String dealTypeName;

    @ApiModelProperty(value = "告警点位")
    private Long stdMeteId;
    @ApiModelProperty(value = "告警点位名称")
    private String meteName;

    @ApiModelProperty(value = "处理人id")
    private String dealPersonId;
    @ApiModelProperty(value = "处理人名称")
    private String dealPersonName;

    @ApiModelProperty(value = "处理时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealTime;

    @ApiModelProperty(value = "实物Id")
    private String realCode;

    @ApiModelProperty(value = "预置位id")
    private Long presetId;

    @ApiModelProperty(value = "摄像机id")
    private Long cameraId;

    @ApiModelProperty(value = "巡检点ID")
    private Long instanceId;

    @ApiModelProperty(value = "缺陷类型")
    private Integer defectModel;

    @ApiModelProperty(value = "缺陷类型--字典表")
    private String defectModelName;
    @ApiModelProperty(value = "部位名称")
    private String customName;
    @ApiModelProperty(value = "设备编码")
    private String deviceCode;
    @ApiModelProperty(value = "告警设备类型-0机器人,1摄像机")
    private Integer deviceType;
    @ApiModelProperty(value = "若是机器人，1可见光2红外")
    private String videoCameraType;

    @ApiModelProperty(value = "间隔名称")
    private String regionName;

    @ApiModelProperty(value = "告警阈值")
    private String thresholdValue;

    @ApiModelProperty(value = "告警上限1")
    private Float highLimit1;

    @ApiModelProperty(value = "告警下限1")
    private Float lowLimit1;

    @ApiModelProperty(value = "告警上限2")
    private Float highLimit2;

    @ApiModelProperty(value = "告警下限2")
    private Float lowLimit2;

    @ApiModelProperty(value = "告警上限3")
    private Float highLimit3;

    @ApiModelProperty(value = "告警下限3")
    private Float lowLimit3;

    @ApiModelProperty(value = "告警上限4")
    private Float highLimit4;

    @ApiModelProperty(value = "告警下限4")
    private Float lowLimit4;
}
