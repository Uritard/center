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
    private long warnId;

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

    @ApiModelProperty(value = "处理方法")
    private String dealInfo;

    @ApiModelProperty(value = "告警点位")
    private Long stdMeteId;
    @ApiModelProperty(value = "告警点位名称")
    private String stdMeteName;


}
