package com.yjh.platform.module.device.entity;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author tt
 * @since 2020-08-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDevicemete对象", description = "标准设备测点表")
public class TStdDeviceMete implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备测点实例ID")
    @TableId(value = "device_mete_id", type = IdType.AUTO)
    private Long deviceMeteId;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "标准测点ID")
    private Long meteId;

    @ApiModelProperty(value = "测点类型:0-遥信，1-遥测")
    private Integer meteKind;

    @ApiModelProperty(value = "设备名称")
    private String meteName;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "部位类型")
    private String customType;

    @ApiModelProperty(value = "点号位置，inside-内部设备，outside-外部设备")
    private String positionType;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "信号说明")
    private String alarmNote;

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

    @ApiModelProperty(value = "告警延时")
    private Integer alarmDelay;

    @ApiModelProperty(value = "告警次数")
    private Integer alarmCnt;

    @ApiModelProperty(value = "绝对阀值")
    private BigDecimal thresholdAbs;

    @ApiModelProperty(value = "百分比阀值")
    private BigDecimal thresholdPer;

    private String meteType;

    @ApiModelProperty(value = "系数")
    private Integer modulus;

    @ApiModelProperty(value = "是否生成告警 0-生成 1-不生成")
    private String remark;

    private String stateZero;

    private String stateOne;

    private Integer alarmState;

}
