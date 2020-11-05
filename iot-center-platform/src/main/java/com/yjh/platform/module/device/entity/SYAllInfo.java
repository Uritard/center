package com.yjh.platform.module.device.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * @author lqh
 * @since 2020/11/4
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "所有的四遥信息", description = "四要信息")
public class SYAllInfo implements Serializable {

    @ApiModelProperty(value = "监控量编码")
    private String meteId;

    @ApiModelProperty(value = "测点标准化编码")
    private String meteType;

    @ApiModelProperty(value = "四遥类型 0：遥信，1：遥测，2：遥控，3：遥调，4：遥脉")
    private Integer meteKind;

    @ApiModelProperty(value = "测点标准名")
    private String meteName;

    @ApiModelProperty(value = "测点编码")
    private String meteCode;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "测点解释")
    private String meteExplainType;

    private Date createTime;

    private Date updateTime;

    @ApiModelProperty(value = "系数")
    private Integer modulus;

    private String stationName;

    private String stationId;

    private Integer upEffect;

    private Integer downEffect;

    private Integer alarmlevel;

    private Integer alarmthresbhold;

    private String describer;

    private Integer metePrecision;

    private Float changeLimit;

    private Float hilimit1;

    private Float lolimit1;

    private Float hilimit2;

    private Float lolimit2;

    private Float hilimit3;

    private Float lolimit3;

    private Float hilimit4;

    private Float stander;

    private Integer controlenable;


    @ApiModelProperty(value = "设备编号")
    private String deviceId;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "设备编码+设备测点地址")
    private String deviceCode;



    @ApiModelProperty(value = "关联设备编码")
    private String relationCode;


    @ApiModelProperty(value = "描述")
    private String remark;


    @ApiModelProperty(value = "同一设备下的监控量序号")
    private Integer meteIndex;

    @ApiModelProperty(value = "CID")
    private Integer meteCid;

    @ApiModelProperty(value = "遥调量类型")
    private Integer adjustKind;

    @ApiModelProperty(value = "当前值")
    private Float lastValue;

    @ApiModelProperty(value = "更新时间")
    private Date lastTime;

    @ApiModelProperty(value = "监控量描述")
    private String description;

    @ApiModelProperty(value = "可控状态")
    private Integer controlStatus;

    @ApiModelProperty(value = "控制使能条件表达式")
    private String enableString;

    @ApiModelProperty(value = "控制成功条件表达式")
    private String succeedString;

    @ApiModelProperty(value = "触发条件表达式")
    private String triggerString;

    @ApiModelProperty(value = "控制参数")
    private Integer controlValue;

    @ApiModelProperty(value = "上下限带宽")
    private Float limitBand;

    @ApiModelProperty(value = "有效性表达式变量串")
    private String validMid;

    @ApiModelProperty(value = "有效性判断表达式")
    private String validString;

    @ApiModelProperty(value = "无效时的值")
    private Float invalidValue;

    @ApiModelProperty(value = "四级告警下限")
    private Float lolimit4;

    @ApiModelProperty(value = "是否屏蔽")
    private Integer isshield;

    @ApiModelProperty(value = "存储周期")
    private Long storageperiod;

    @ApiModelProperty(value = "关联的遥信量")
    private String linkMeteId;

    @ApiModelProperty(value = "遥信量种类")
    private Integer signalKind;

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

}
