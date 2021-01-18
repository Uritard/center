package com.yjh.platform.module.device.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;

/**
 * @author tt
 * @since 2020-08-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdMete对象", description = "系统测点信息表")
public class TStdMete implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "测点ID")
    @TableId(value = "std_mete_id", type = IdType.AUTO)
    private Long stdMeteId;

    @Max(value = 99999999)
    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @Length(max = 20, message = "meteType长度必须小于等于20")
    @ApiModelProperty(value = "测点类型")
    private String meteType;

    @Max(value = 999999999)
    @ApiModelProperty(value = "测点种类")
    private Integer meteKind;

    @Length(max = 128, message = "meteName长度必须小于等于128")
    @ApiModelProperty(value = "测点标准名")
    private String meteName;

    @Length(max = 256, message = "alarmNote长度必须小于等于256")
    @ApiModelProperty(value = "信号说明")
    private String alarmNote;

    @Length(max = 256, message = "alarmExplain长度必须小于等于256")
    @ApiModelProperty(value = "信号解释")
    private String alarmExplain;

    @Length(max = 50, message = "alarmType长度必须小于等于50")
    @ApiModelProperty(value = "告警分类")
    private String alarmType;

    @Length(max = 50, message = "unit长度必须小于等于50")
    @ApiModelProperty(value = "单位")
    private String unit;

    @Max(value = 999999999)
    @ApiModelProperty(value = "有效上限")
    private Float upEffect;

    @Max(value = 999999999)
    @ApiModelProperty(value = "有效下限")
    private Float downEffect;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警门限")
    private Integer alarmLimit;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警上限1")
    private Float highLimit1;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警下限1")
    private Float lowLimit1;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警上限2")
    private Float highLimit2;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警下限2")
    private Float lowLimit2;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警上限3")
    private Float highLimit3;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警下限3")
    private Float lowLimit3;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警上限4")
    private Float highLimit4;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警下限4")
    private Float lowLimit4;

    @Length(max = 20, message = "stateZero长度必须小于等于20")
    @ApiModelProperty(value = "状态一")
    private String stateZero;

    @Length(max = 20, message = "stateOne长度必须小于等于20")
    @ApiModelProperty(value = "状态二")
    private String stateOne;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警延时")
    private Integer alarmDelay;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警次数")
    private Integer alarmCnt;

    @Max(value = 9999999999l)
    @ApiModelProperty(value = "绝对阀值")
    private BigDecimal thresholdAbs;

    @Max(value = 99999)
    @ApiModelProperty(value = "百分比阀值")
    private BigDecimal thresholdPer;

    @Max(value = 999999999)
    @ApiModelProperty(value = "系数")
    private Integer modulus;

    @Length(max = 128, message = "remark长度必须小于等于128")
    @ApiModelProperty(value = "备注")
    private String remark;

    @Max(value = 999999999)
    private Integer analyseType;

    @Length(max = 50, message = "analyseTypeName长度必须小于等于50")
    private String analyseTypeName;


}
