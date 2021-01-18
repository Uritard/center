package com.yjh.platform.module.device.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Past;

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

    @Length(max = 20,message = "deviceId长度必须小于等于20")
    @ApiModelProperty(value = "设备编号")
    private String deviceId;

    @Length(max = 20,message = "meteId长度必须小于等于20")
    @ApiModelProperty(value = "监控量编号")
    private String meteId;

    @Length(max = 256,message = "meteName长度必须小于等于256")
    @ApiModelProperty(value = "监控量名称")
    private String meteName;

    @Max(value=999999999)
    @ApiModelProperty(value = "有效上限")
    private Integer upEffect;

    @Max(value=999999999)
    @ApiModelProperty(value = "有效下限")
    private Integer downEffect;

    @Max(value=999999999)
    @ApiModelProperty(value = "同一设备下的监控量序号")
    private Integer meteIndex;

    @Max(value=999999999)
    @ApiModelProperty(value = "CID")
    private Integer meteCid;

    @Max(value=999999999)
    @ApiModelProperty(value = "遥信量种类")
    private Integer signalKind;

    @Max(value=999999999)
    @ApiModelProperty(value = "当前值")
    private Integer lastValue;

    @Past
    @ApiModelProperty(value = "当前值更新时间")
    private Date lastTime;

    @Max(value=999999999)
    @ApiModelProperty(value = "解释类型")
    private Integer explainType;

    @Max(value=999999999)
    @ApiModelProperty(value = "上报模式")
    private Integer reportType;

    @Max(value=999999999)
    @ApiModelProperty(value = "上报级别")
    private Integer reportLevel;

    @Max(value=999999999)
    @ApiModelProperty(value = "屏蔽类型")
    private Integer maskType;

    @Length(max = 256,message = "maskMid长度必须小于等于256")
    @ApiModelProperty(value = "屏蔽表达式变量串")
    private String maskMid;

    @Length(max = 256,message = "maskString长度必须小于等于256")
    @ApiModelProperty(value = "屏蔽表达式")
    private String maskString;

    @Max(value=999999999)
    @ApiModelProperty(value = "屏蔽值")
    private Integer maskValue;

    @Max(value=999999999)
    @ApiModelProperty(value = "防抖延时门限")
    private Integer delayTime;

    @Length(max = 20,message = "meteCode长度必须小于等于20")
    @ApiModelProperty(value = "信号标准化编码")
    private String meteCode;

    @Length(max = 20,message = "deviceType长度必须小于等于20")
    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @Length(max = 512,message = "description长度必须小于等于512")
    @ApiModelProperty(value = "监控量描述")
    private String description;

    @Max(value=999999999)
    @ApiModelProperty(value = "是否屏蔽")
    private Integer isshield;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "存储周期")
    private Long storageperiod;

    @Length(max = 512,message = "describer长度必须小于等于512")
    @ApiModelProperty(value = "态值描述")
    private String describer;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警触发值")
    private Integer alarmthresbhold;

    @Max(value=999999999)
    @ApiModelProperty(value = "告警等级")
    private Integer alarmlevel;

    @Length(max = 512,message = "linkMeteId长度必须小于等于512")
    @ApiModelProperty(value = "关联的遥信量")
    private String linkMeteId;


}
