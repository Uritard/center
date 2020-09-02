package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdMetemodelDetail对象", description = "系统测点模版详细表")
public class MeteModelDetail implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "监控量模板ID")
    private Long modelId;

    @ApiModelProperty(value = "监控量ID")
    private Long meteId;

    @ApiModelProperty(value = "部位类型，默认为0-本体，")
    private Integer customType;

    @ApiModelProperty(value = "部位名称")
    private String customTypeName;

    @ApiModelProperty(value = "信号标准化编码")
    private String meteCode;

    @ApiModelProperty(value = "测点标准名")
    private String meteName;

    @ApiModelProperty(value = "测点类型")
    private String meteType;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "信号说明")
    private String alarmNote;

    @ApiModelProperty(value = "信号解释")
    private String alarmExplain;

    @ApiModelProperty(value = "告警分类")
    private String alarmType;

    @ApiModelProperty(value = "有效上限")
    private Float upEffect;

    @ApiModelProperty(value = "有效下限")
    private Float lowEffect;

    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;

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

    @ApiModelProperty(value = "告警延时")
    private Integer alarmDelay;

    @ApiModelProperty(value = "告警次数")
    private Integer alarmCnt;

    @ApiModelProperty(value = "绝对阀值")
    private BigDecimal thresholdAbs;

    @ApiModelProperty(value = "百分比阀值")
    private BigDecimal thresholdPer;

    @ApiModelProperty(value = "系数")
    private Integer modulus;


}
