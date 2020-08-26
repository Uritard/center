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
@ApiModel(value = "TCfgTelemeter对象", description = "遥测量表")
public class TCfgTelemeter implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "设备编号")
    private String deviceId;

    @ApiModelProperty(value = "监控量编号")
    private String meteId;

    @ApiModelProperty(value = "监控量名称")
    private String meteName;

    @ApiModelProperty(value = "有效上限")
    private Float upEffect;

    @ApiModelProperty(value = "有效下限")
    private Float lowEffect;

    @ApiModelProperty(value = "小数点后的有效位数")
    private Integer metePrecision;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "同一设备下的监控量序号")
    private Integer meteIndex;

    @ApiModelProperty(value = "CID")
    private Integer meteCid;

    @ApiModelProperty(value = "上下限带宽")
    private Float limitBand;

    @ApiModelProperty(value = "变化幅度门限")
    private Float changeLimit;

    @ApiModelProperty(value = "有效性表达式变量串")
    private String validMid;

    @ApiModelProperty(value = "有效性判断表达式")
    private String validString;

    @ApiModelProperty(value = "无效时的值")
    private Float invalidValue;

    @ApiModelProperty(value = "当前值")
    private Float lastValue;

    @ApiModelProperty(value = "当前值更新时间")
    private Date lastTime;


    @ApiModelProperty(value = "信号标准化编码")
    private String meteCode;


    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "监控量描述")
    private String description;

    @ApiModelProperty(value = "一级告警上限")
    private Float hilimit1;

    @ApiModelProperty(value = "一级告警下限")
    private Float lolimit1;

    @ApiModelProperty(value = "二级告警上限")
    private Float hilimit2;

    @ApiModelProperty(value = "二级告警下限")
    private Float lolimit2;

    @ApiModelProperty(value = "三级告警上限")
    private Float hilimit3;

    @ApiModelProperty(value = "三级告警下限")
    private Float lolimit3;

    @ApiModelProperty(value = "四级告警上限")
    private Float hilimit4;

    @ApiModelProperty(value = "四级告警下限")
    private Float lolimit4;

    @ApiModelProperty(value = "标称值")
    private Float stander;

    @ApiModelProperty(value = "是否屏蔽")
    private Integer isshield;

    @ApiModelProperty(value = "存储周期")
    private Long storageperiod;

    @ApiModelProperty(value = "关联的遥信量")
    private String linkMeteId;


}
