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
 * @date 2021/2/18 13:47
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "测点巡检时间记录表类", description = "测点巡检时间记录表")
public class TStdDeviceMeteUpdate implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "巡检时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private String updateTime;
    @ApiModelProperty(value = "测点id")
    private Long deviceMeteId;
    @ApiModelProperty(value = "巡视执行结果")
    private Integer cruiseResult;
    @ApiModelProperty(value = "实际结果")
    private Integer identifyResult;
    @ApiModelProperty(value = "巡检图片")
    private String picPath;
}
