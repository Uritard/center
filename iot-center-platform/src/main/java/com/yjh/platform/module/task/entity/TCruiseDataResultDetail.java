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
 * @date 2020/10/29 - 14:46
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseDataResultDetail对象", description = "巡检记录报表-明细")
public class TCruiseDataResultDetail implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡视设备")
    private String deviceName;
    @ApiModelProperty(value = "巡视点")
    private String instanceName;
    @ApiModelProperty(value = "巡视值")
    private String resultNum;
    @ApiModelProperty(value = "图片")
    private String picPath;
    @ApiModelProperty(value = "识别状态")
    private String cruiseResultName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "巡视时间")
    private Date cruiseTime;
    @ApiModelProperty(value = "序号")
    private int px;
    @ApiModelProperty(value = "实物编码")
    private String realCode;
    @ApiModelProperty(value = "审核值")
    private String personCheck;
    @ApiModelProperty(value = "审核结果")
    private String identifyResultName;
    @ApiModelProperty(value = "审核状态")
    private String evaluationStateName;
}
