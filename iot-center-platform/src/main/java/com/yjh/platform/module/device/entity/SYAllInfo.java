package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @TableField(value = "mete_id",updateStrategy = FieldStrategy.IGNORED)
    private String meteId;

    @ApiModelProperty(value = "测点标准化编码")
    @TableField(value = "mete_type",updateStrategy = FieldStrategy.IGNORED)
    private String meteType;

    @ApiModelProperty(value = "四遥类型 0：遥信，1：遥测，2：遥控，3：遥调，4：遥脉")
    private Integer meteKind;

    @ApiModelProperty(value = "测点标准名")
     @TableField(value = "mete_name",updateStrategy = FieldStrategy.IGNORED)
    private String meteName;

    @ApiModelProperty(value = "测点编码")
    @TableField(value = "mete_code",updateStrategy = FieldStrategy.IGNORED)
    private String meteCode;

    @ApiModelProperty(value = "单位")
    @TableField(value = "unit",updateStrategy = FieldStrategy.IGNORED)
    private String unit;

    @ApiModelProperty(value = "测点解释")
    @TableField(value = "mete_explain_type",updateStrategy = FieldStrategy.IGNORED)
    private String meteExplainType;
     @TableField(value = "create_time",updateStrategy = FieldStrategy.IGNORED)
    private Date createTime;
    @TableField(value = "update_time",updateStrategy = FieldStrategy.IGNORED)
    private Date updateTime;

    @ApiModelProperty(value = "系数")
    private Integer modulus;
     @TableField(value = "station_name",updateStrategy = FieldStrategy.IGNORED)
    private String stationName;
     @TableField(value = "station_id",updateStrategy = FieldStrategy.IGNORED)
    private String stationId;
     @TableField(value = "describer",updateStrategy = FieldStrategy.IGNORED)
    private Integer upEffect;

    private Integer downEffect;

    private Integer alarmlevel;

    private Integer alarmthresbhold;

    private String describer;

    private Integer metePrecision;
     @TableField(value = "change_limit",updateStrategy = FieldStrategy.IGNORED)
    private Float changeLimit;
    @TableField(value = "hilimit1",updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit1;
    @TableField(value = "lolimit1",updateStrategy = FieldStrategy.IGNORED)
    private Float lolimit1;
    @TableField(value = "hilimit2",updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit2;
    @TableField(value = "lolimit2",updateStrategy = FieldStrategy.IGNORED)
    private Float lolimit2;
    @TableField(value = "hilimit3",updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit3;
    @TableField(value = "hilimit3",updateStrategy = FieldStrategy.IGNORED)
    private Float lolimit3;
    @TableField(value = "hilimit4",updateStrategy = FieldStrategy.IGNORED)
    private Float hilimit4;
     @TableField(value = "stander",updateStrategy = FieldStrategy.IGNORED)
    private Float stander;

    private Integer controlenable;


    @ApiModelProperty(value = "设备编号")
     @TableField(value = "device_id",updateStrategy = FieldStrategy.IGNORED)
    private String deviceId;

    @ApiModelProperty(value = "设备名称")
    private String deviceName;

    @ApiModelProperty(value = "设备类型")
    @TableField(value = "device_type",updateStrategy = FieldStrategy.IGNORED)
    private String deviceType;

    @ApiModelProperty(value = "设备编码+设备测点地址")
    private String deviceCode;



    @ApiModelProperty(value = "关联设备编码")
    @TableField(value = "relation_code",updateStrategy = FieldStrategy.IGNORED)
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
    @TableField(value = "last_value",updateStrategy = FieldStrategy.IGNORED)
    private Float lastValue;

    @ApiModelProperty(value = "更新时间")
    @TableField(value = "last_time",updateStrategy = FieldStrategy.IGNORED)
    private Date lastTime;

    @ApiModelProperty(value = "监控量描述")
    @TableField(value = "description",updateStrategy = FieldStrategy.IGNORED)
    private String description;

    @ApiModelProperty(value = "可控状态")
    private Integer controlStatus;

    @ApiModelProperty(value = "控制使能条件表达式")
    @TableField(value = "enable_string",updateStrategy = FieldStrategy.IGNORED)
    private String enableString;

    @ApiModelProperty(value = "控制成功条件表达式")
     @TableField(value = "succeed_string",updateStrategy = FieldStrategy.IGNORED)
    private String succeedString;

    @ApiModelProperty(value = "触发条件表达式")
    @TableField(value = "trigger_string",updateStrategy = FieldStrategy.IGNORED)
    private String triggerString;

    @ApiModelProperty(value = "控制参数")
    private Integer controlValue;

    @ApiModelProperty(value = "上下限带宽")
     @TableField(value = "limit_band",updateStrategy = FieldStrategy.IGNORED)
    private Float limitBand;

    @ApiModelProperty(value = "有效性表达式变量串")
    @TableField(value = "valid_mid",updateStrategy = FieldStrategy.IGNORED)
    private String validMid;

    @ApiModelProperty(value = "有效性判断表达式")
     @TableField(value = "valid_string",updateStrategy = FieldStrategy.IGNORED)
    private String validString;

    @ApiModelProperty(value = "无效时的值")
    @TableField(value = "invalid_value",updateStrategy = FieldStrategy.IGNORED)
    private Float invalidValue;

    @ApiModelProperty(value = "四级告警下限")
    private Float lolimit4;

    @ApiModelProperty(value = "是否屏蔽")
    private Integer isshield;

    @ApiModelProperty(value = "存储周期")
    @TableField(value = "storageperiod",updateStrategy = FieldStrategy.IGNORED)
    private Long storageperiod;

    @ApiModelProperty(value = "关联的遥信量")
    @TableField(value = "link_mete_id",updateStrategy = FieldStrategy.IGNORED)
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
     @TableField(value = "mask_mid",updateStrategy = FieldStrategy.IGNORED)
    private String maskMid;

    @ApiModelProperty(value = "屏蔽表达式")
    @TableField(value = "mask_string",updateStrategy = FieldStrategy.IGNORED)
    private String maskString;

    @ApiModelProperty(value = "屏蔽值")
    private Integer maskValue;

    @ApiModelProperty(value = "防抖延时门限")
    private Integer delayTime;

}
