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
 * @date 2020/12/17 11:24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "linkagePopUpContent对象", description = "联动弹窗内容")
public class LinkageInformation {

    @ApiModelProperty(value = "联动任务Id")
    private String unionId;

    @ApiModelProperty(value = "联动量Id")
    private Long meteId;

    @ApiModelProperty(value = "信号类型")
    private String meteKindName;

    @ApiModelProperty(value = "规则Id")
    private Long ruleId;

    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    @ApiModelProperty(value = "预案Id")
    private Long planId;

    @ApiModelProperty(value = "预案名称")
    private String planName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "触发时间")
    private Date createTime;

    @ApiModelProperty(value = "触发规则")
    private String ruleContent;

    @ApiModelProperty(value = "信号值")
    private String meteValue;

    @ApiModelProperty(value = "信号量名称")
    private String deviceName;

    @ApiModelProperty(value = "全景摄像机Id")
    private Long cameraId;

    @ApiModelProperty(value = "全景摄像机预置位Id")
    private Long presetId;

}
