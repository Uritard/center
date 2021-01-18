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
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

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

    @Length(max = 50,message = "unionId长度必须小于等于50")
    @ApiModelProperty(value = "巡检任务")
    private String unionId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "规则id")
    private Long ruleId;

    @Length(max = 128,message = "unionName长度必须小于等于128")
    @ApiModelProperty(value = "任务名称")
    private String unionName;

    @Max(value=999999999)
    @ApiModelProperty(value = "延迟时间")
    private Integer ruleDelay;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "机器人id")
    private Long robotId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "备注1")
    private Long meteId;

    @Max(value=999999999)
    @ApiModelProperty(value = "备注2")
    private Integer remark2;

    @Length(max = 256,message = "remark3长度必须小于等于256")
    @ApiModelProperty(value = "备注3")
    private String remark3;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "巡视时间")
    private Date startTime;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "创建时间")
    private Date createTime;

    @Length(max = 2000,message = "remark3长度必须小于等于2000")
    @ApiModelProperty(value = "断面数据")
    private String paramValues;

    @Max(value=999999999)
    @ApiModelProperty(value = "联动结果 0-失败 1-成功")
    private Integer isFinish;


}
