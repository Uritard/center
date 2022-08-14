package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import java.math.BigDecimal;

/**
 * @author hyh
 * @since 2022/4/15
 **/

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdMeterTypeModel", description = "表计类型模板表")
public class TStdMeterTypeModel {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID")
    @TableId(value = "id")
    private Long id;


    @ApiModelProperty(value = "表计类型")
    @TableId(value = "meter_type")
    private Long meterType;

    @ApiModelProperty(value = "表计类型ID")
    @TableId(value = "device_type")
    private Long deviceType;

    @ApiModelProperty(value = "测点类型:0-遥信，1-遥测")
    @TableField(value = "mete_kind",updateStrategy = FieldStrategy.IGNORED)
    private Integer meteKind;

    @Length(max = 50, message = "meteKindName长度必须小于等于50")
    private String meteKindName;

    @Length(max = 10, message = "unit长度必须小于等于10")
    @ApiModelProperty(value = "单位")
    @TableField(value = "unit",updateStrategy = FieldStrategy.IGNORED)
    private String unit;

    @Length(max = 50, message = "alarmNote长度必须小于等于50")
    @ApiModelProperty(value = "是否生成告警提示 0-生成 1-不生成")
    @TableField(value = "alarm_note",updateStrategy = FieldStrategy.IGNORED)
    private String alarmNote;

    @Length(max = 50, message = "alarmType长度必须小于等于50")
    @ApiModelProperty(value = "告警分类")
    @TableField(value = "alarm_type",updateStrategy = FieldStrategy.IGNORED)
    private String alarmType;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "有效上限")
    private Float upEffect;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "有效下限")
    private Float downEffect;

    @Max(value = 9999)
    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "告警上限1")
    private Float highLimit1;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "告警下限1")
    private Float lowLimit1;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "告警上限2")
    private Float highLimit2;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "告警下限2")
    private Float lowLimit2;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "告警上限3")
    private Float highLimit3;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "告警下限3")
    private Float lowLimit3;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "告警上限4")
    private Float highLimit4;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "告警下限4")
    private Float lowLimit4;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警延时")
    private Integer alarmDelay;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警次数")
    private Integer alarmCnt;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "绝对阀值")
    private BigDecimal thresholdAbs;

    @Digits(integer = 4, fraction = 3)
    @ApiModelProperty(value = "百分比阀值")
    private BigDecimal thresholdPer;

    @Max(value = 999999999)
    @ApiModelProperty(value = "系数")
    private Integer modulus;

    @Length(max = 128, message = "remark长度必须小于等于128")
    @ApiModelProperty(value = "信号说明")
    @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    @Length(max = 20, message = "stateZero长度必须小于等于20")
    @ApiModelProperty(value = "状态一描述")
    @TableField(value = "state_zero",updateStrategy = FieldStrategy.IGNORED)
    private String stateZero;

    @Length(max = 20, message = "stateOne长度必须小于等于20")
    @ApiModelProperty(value = "状态二描述")
    @TableField(value = "state_one",updateStrategy = FieldStrategy.IGNORED)
    private String stateOne;

    private Integer alarmState;
}
