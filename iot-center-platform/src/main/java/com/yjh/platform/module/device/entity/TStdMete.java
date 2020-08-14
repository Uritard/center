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
 * @since 2020-08-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdMete对象", description = "系统测点信息表")
public class TStdMete implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "测点ID")
    @TableId(value = "std_mete_id", type = IdType.AUTO)
    private Long stdMeteId;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "信号标准化编码")
    private String meteCode;

    @ApiModelProperty(value = "测点类型")
    private String meteType;

    @ApiModelProperty(value = "测点标准名")
    private String meteName;

    @ApiModelProperty(value = "信号说明")
    private String alarmNote;

    @ApiModelProperty(value = "信号解释")
    private String alarmExplain;

    @ApiModelProperty(value = "告警分类")
    private String alarmType;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "有效上限")
    private Float upEffect;

    @ApiModelProperty(value = "有效下限")
    private Float lowEffect;

    @ApiModelProperty(value = "告警级别")
    private Integer alarmLevel;

    @ApiModelProperty(value = "告警门限")
    private Integer alarmLimit;

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

    @ApiModelProperty(value = "备注")
    private String remark;


}
