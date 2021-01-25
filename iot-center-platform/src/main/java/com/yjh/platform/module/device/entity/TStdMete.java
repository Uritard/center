package com.yjh.platform.module.device.entity;

import java.math.BigDecimal;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField(value = "std_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long stdMeteId;

    @Max(value = 99999999)
    @ApiModelProperty(value = "设备类型")

    private Integer deviceType;

    @Length(max = 20, message = "meteType长度必须小于等于20")
    @TableField(value = "mete_type",updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "测点类型")

    private String meteType;

    @Max(value = 999999999)
    @ApiModelProperty(value = "测点种类")
    @TableField(value = "mete_kind",updateStrategy = FieldStrategy.IGNORED)
    private Integer meteKind;

    @Length(max = 128, message = "meteName长度必须小于等于128")
    @ApiModelProperty(value = "测点标准名")
    @TableField(value = "mete_name",updateStrategy = FieldStrategy.IGNORED)
    private String meteName;

    @Length(max = 256, message = "alarmNote长度必须小于等于256")
    @ApiModelProperty(value = "信号说明")
    @TableField(value = "alarm_note",updateStrategy = FieldStrategy.IGNORED)
    private String alarmNote;

    @Length(max = 256, message = "alarmExplain长度必须小于等于256")
    @ApiModelProperty(value = "信号解释")
     @TableField(value = "alarm_explain",updateStrategy = FieldStrategy.IGNORED)
    private String alarmExplain;

    @Length(max = 50, message = "alarmType长度必须小于等于50")
    @ApiModelProperty(value = "告警分类")
    @TableField(value = "alarm_type",updateStrategy = FieldStrategy.IGNORED)
    private String alarmType;

    @Length(max = 50, message = "unit长度必须小于等于50")
    @ApiModelProperty(value = "单位")
    @TableField(value = "unit",updateStrategy = FieldStrategy.IGNORED)
    private String unit;

    @Max(value = 999999999)
    @ApiModelProperty(value = "有效上限")
    @TableField(value = "up_effect",updateStrategy = FieldStrategy.IGNORED)
    private Float upEffect;

    @Max(value = 999999999)
    @ApiModelProperty(value = "有效下限")
    @TableField(value = "down_effect",updateStrategy = FieldStrategy.IGNORED)
    private Float downEffect;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警门限")
    private Integer alarmLimit;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警上限1")
    @TableField(value = "high_limit1",updateStrategy = FieldStrategy.IGNORED)
    private Float highLimit1;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警下限1")
    @TableField(value = "low_limit1",updateStrategy = FieldStrategy.IGNORED)
    private Float lowLimit1;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警上限2")
    @TableField(value = "high_limit2",updateStrategy = FieldStrategy.IGNORED)
    private Float highLimit2;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警下限2")
    @TableField(value = "low_limit2",updateStrategy = FieldStrategy.IGNORED)
    private Float lowLimit2;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警上限3")
     @TableField(value = "high_limit3",updateStrategy = FieldStrategy.IGNORED)
    private Float highLimit3;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警下限3")
     @TableField(value = "low_limit3",updateStrategy = FieldStrategy.IGNORED)
    private Float lowLimit3;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警上限4")
    @TableField(value = "high_limit4",updateStrategy = FieldStrategy.IGNORED)
    private Float highLimit4;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警下限4")
    @TableField(value = "low_limit4",updateStrategy = FieldStrategy.IGNORED)
    private Float lowLimit4;

    @Length(max = 20, message = "stateZero长度必须小于等于20")
    @ApiModelProperty(value = "状态一")
    @TableField(value = "state_zero",updateStrategy = FieldStrategy.IGNORED)
    private String stateZero;

    @Length(max = 20, message = "stateOne长度必须小于等于20")
    @ApiModelProperty(value = "状态二")
     @TableField(value = "state_one",updateStrategy = FieldStrategy.IGNORED)
    private String stateOne;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警延时")
    private Integer alarmDelay;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警次数")
    private Integer alarmCnt;

    @Max(value = 9999999999l)
    @ApiModelProperty(value = "绝对阀值")
    @TableField(value = "threshold_abs",updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal thresholdAbs;

    @Max(value = 99999)
    @ApiModelProperty(value = "百分比阀值")
    @TableField(value = "threshold_per",updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal thresholdPer;

    @Max(value = 999999999)
    @ApiModelProperty(value = "系数")
    private Integer modulus;

    @Length(max = 128, message = "remark长度必须小于等于128")
    @ApiModelProperty(value = "备注")
    @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    @Max(value = 999999999)
    private Integer analyseType;

    @Length(max = 50, message = "analyseTypeName长度必须小于等于50")
    private String analyseTypeName;


}
