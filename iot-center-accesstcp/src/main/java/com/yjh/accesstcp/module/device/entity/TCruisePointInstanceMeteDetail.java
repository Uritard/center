package com.yjh.accesstcp.module.device.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/5/27
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TCruisePointInstanceMeteDetail extends TCruisePointInstance{

    @ApiModelProperty(value = "标注点位Id")
    private String devicePointId;

    @ApiModelProperty(value = "测点类型:0-遥信，1-遥测")
    private Integer meteKind;

    @ApiModelProperty(value = "表计类型")
    private Integer meterType;

    @ApiModelProperty(value = "设备名称")
    private String meteName;

    @ApiModelProperty(value = "外观类型")
    private Integer appearanceType;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "算法类型")
    private Integer analyseType;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "告警分类")
    private String alarmType;

    @ApiModelProperty(value = "有效上限")
    private Float upEffect;

    @ApiModelProperty(value = "有效下限")
    private Float downEffect;

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


    @ApiModelProperty(value = "绝对阀值")
    private BigDecimal thresholdAbs;

    @ApiModelProperty(value = "百分比阀值")
    private BigDecimal thresholdPer;

    @ApiModelProperty(value = "巡检类型")
    private String meteType;

    @ApiModelProperty(value = "状态一描述")
    private String stateZero;

    @ApiModelProperty(value = "状态二描述")
    private String stateOne;

    @ApiModelProperty(value = "告警级别")
    private Integer alarmState;

    @ApiModelProperty(value = "是否温差任务（0-否；1-是）")
    private Integer isTemdif;

    @ApiModelProperty(value = "识别类型")
    private String recognitionType;
}
