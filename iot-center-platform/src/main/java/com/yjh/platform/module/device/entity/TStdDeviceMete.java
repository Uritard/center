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

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.math.BigDecimal;

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

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "设备测点实例ID")
    @TableId(value = "device_mete_id", type = IdType.AUTO)
    private Long deviceMeteId;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "设备ID")
    private Long deviceId;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "设备点位ID")
    private String devicePointId;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "标准测点ID")
    @TableField(value = "mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long meteId;

    @Length(max = 50, message = "customId长度必须小于等于50")
    private String customId;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "测点类型:0-遥信，1-遥测")
    @TableField(value = "mete_kind",updateStrategy = FieldStrategy.IGNORED)
    private Integer meteKind;

    @Length(max = 50, message = "meteKindName长度必须小于等于50")
    private String meteKindName;

    @Max(value = 99999999)
    @ApiModelProperty(value = "表计类型")
    private Integer meterType;

    @Length(max = 50, message = "meteName长度必须小于等于50")
    @ApiModelProperty(value = "设备名称")
    @TableField(value = "mete_name",updateStrategy = FieldStrategy.IGNORED)
    private String meteName;

    @Max(value = 9999)
    @ApiModelProperty(value = "外观类型")
    private Integer appearanceType;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @TableField(value = "inspection_type",updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "测点类型, 1.巡检点 2.操作点")
    private String inspectionType;

    @Length(max = 20, message = "positionType长度必须小于等于20")
    @ApiModelProperty(value = "点号位置，inside-内部设备，outside-外部设备")
    @TableField(value = "position_type",updateStrategy = FieldStrategy.IGNORED)
    private String positionType;

    private Integer analyseType;

    private String isAi;

    @Length(max = 50, message = "unit长度必须小于等于50")
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

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警延时")
    private Integer alarmDelay;

    @Max(value = 999999999)
    @ApiModelProperty(value = "告警次数")
    private Integer alarmCnt;

    @Max(value = 999999999)
    @ApiModelProperty(value = "绝对阀值")
    private BigDecimal thresholdAbs;

    @Max(value = 99999)
    @ApiModelProperty(value = "百分比阀值")
    private BigDecimal thresholdPer;

    @Length(max = 50, message = "meteType长度必须小于等于50")
    @TableField(value = "mete_type",updateStrategy = FieldStrategy.IGNORED)
    private String meteType;

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

    @Max(value = 99999999)
    private Integer alarmState;

    private String isJudge;

    private Integer pageNum = 1;

    private Integer pageSize = 0;

    private String redundantType;
}
