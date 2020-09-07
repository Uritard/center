package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCfgTelesignal对象", description = "遥信量表")
public class TCfgTelesignal implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备编号")
    private String deviceId;

    @ApiModelProperty(value = "监控量编号")
    private String meteId;

    @ApiModelProperty(value = "监控量名称")
    private String meteName;

    @ApiModelProperty(value = "有效上限")
    private Integer upEffect;

    @ApiModelProperty(value = "有效下限")
    private Integer downEffect;

    @ApiModelProperty(value = "同一设备下的监控量序号")
    private Integer meteIndex;

    @ApiModelProperty(value = "CID")
    private Integer meteCid;

    @ApiModelProperty(value = "遥信量种类")
    private Integer signalKind;

    @ApiModelProperty(value = "当前值")
    private Integer lastValue;

    @ApiModelProperty(value = "当前值更新时间")
    private Date lastTime;

    @ApiModelProperty(value = "解释类型")
    private Integer explainType;

    @ApiModelProperty(value = "上报模式")
    private Integer reportType;

    @ApiModelProperty(value = "上报级别")
    private Integer reportLevel;

    @ApiModelProperty(value = "屏蔽类型")
    private Integer maskType;

    @ApiModelProperty(value = "屏蔽表达式变量串")
    private String maskMid;

    @ApiModelProperty(value = "屏蔽表达式")
    private String maskString;

    @ApiModelProperty(value = "屏蔽值")
    private Integer maskValue;

    @ApiModelProperty(value = "防抖延时门限")
    private Integer delayTime;


    @ApiModelProperty(value = "信号标准化编码")
    private String meteCode;


    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "监控量描述")
    private String description;

    @ApiModelProperty(value = "是否屏蔽")
    private Integer isshield;

    @ApiModelProperty(value = "存储周期")
    private Long storageperiod;

    @ApiModelProperty(value = "态值描述")
    private String describer;

    @ApiModelProperty(value = "告警触发值")
    private Integer alarmthresbhold;

    @ApiModelProperty(value = "告警等级")
    private Integer alarmlevel;

    @ApiModelProperty(value = "关联的遥信量")
    private String linkMeteId;


}
