package com.yjh.accessvideo.module.device.entity;

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
 * @since 2020-10-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdDevicemete对象", description = "标准设备测点表")
public class TStdDevicemete implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备测点实例ID")
    @TableId(value = "device_mete_id", type = IdType.AUTO)
    private Long deviceMeteId;

    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @ApiModelProperty(value = "部位ID")
    private String customId;

    @ApiModelProperty(value = "标准测点ID")
    private Long meteId;

    @ApiModelProperty(value = "测点类型:0-遥信，1-遥测")
    private String meteKind;

    @ApiModelProperty(value = "巡检类型")
    private String meteType;

    @ApiModelProperty(value = "测点名称")
    private String meteName;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "点号位置，inside-内部设备，outside-外部设备")
    private String positionType;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "是否生成告警 0-生成 1-不生成")
    private String alarmNote;

    @ApiModelProperty(value = "告警分类")
    private String alarmType;

    @ApiModelProperty(value = "有效上限")
    private Float upEffect;

    @ApiModelProperty(value = "有效下限")
    private Float downEffect;

    @ApiModelProperty(value = "状态一描述")
    private String stateZero;

    @ApiModelProperty(value = "状态二描述")
    private String stateOne;

    @ApiModelProperty(value = "告警关联信号")
    private Integer alarmState;

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

    private Float highLimit3;

    private Float lowLimit3;

    private Float highLimit4;

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

    @ApiModelProperty(value = "信号说明")
    private String remark;


}
