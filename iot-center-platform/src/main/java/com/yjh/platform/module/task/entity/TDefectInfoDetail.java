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
 * @date 2020/10/14 - 21:13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDefectInfoDetail对象", description = "缺陷信息扩展")
public class TDefectInfoDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "缺陷id")
    private Long defectId;

    @ApiModelProperty(value = "缺陷时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date defectTime;

    @ApiModelProperty(value = "缺陷类型")
    private Integer defectType;
    @ApiModelProperty(value = "缺陷类型-字典表")
    private String defectTypeName;

    @ApiModelProperty(value = "处理状态")
    private Integer confMode;
    @ApiModelProperty(value = "处理状态-字典表")
    private String confModeName;

    @ApiModelProperty(value = "设备id")
    private Integer deviceId;
    @ApiModelProperty(value = "缺陷主设备")
    private String deviceName;

    @ApiModelProperty(value = "缺陷点位id")
    private Integer stdMeteId;
    @ApiModelProperty(value = "缺陷点位名称")
    private String meteName;

    @ApiModelProperty(value = "处理意见")
    private String dealInfo;
    @ApiModelProperty(value = "确认时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date dealTime;

    @ApiModelProperty(value = "缺陷状态源图片")
    private String imagePath;

    @ApiModelProperty(value = "处理方法")
    private Integer dealType;
    @ApiModelProperty(value = "处理方法-字典表")
    private String dealTypeName;

}
