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

    @ApiModelProperty(value = "设备名称")
    private String deviceName;
    @ApiModelProperty(value = "检测内容")
    private String instanceName;
    @ApiModelProperty(value = "结果")
    private String identifyResult;
    @ApiModelProperty(value = "图片")
    private String picpath;
    @ApiModelProperty(value = "巡检结果")
    private String cruiseResult;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "巡检时间")
    private Date cruiseTime;
    @ApiModelProperty(value = "序号")
    private int px;
}
