package com.yjh.platform.module.task.entity;

import java.awt.*;
import java.util.Date;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @author tt
 * @since 2020-09-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TUnionTask对象", description = "巡检任务表")
public class TUnionTask implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "巡检任务")
    private String unionId;

    @ApiModelProperty(value = "规则id")
    private Long ruleId;

    @ApiModelProperty(value = "任务名称")
    private String unionName;

    @ApiModelProperty(value = "延迟时间")
    private Integer ruleDelay;

    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @ApiModelProperty(value = "备注1")
    private Long meteId;

    @ApiModelProperty(value = "备注2")
    private Integer remark2;

    @ApiModelProperty(value = "备注3")
    private String remark3;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "巡视时间")
    private Date startTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @ApiModelProperty(value = "断面数据")
    private String paramValues;

    @ApiModelProperty(value = "联动结果 0-失败 1-成功")
    private Integer isFinish;


}
