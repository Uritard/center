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
@ApiModel(value = "TCfgTeleadjust对象", description = "遥调量表")
public class TCfgTeleadjust implements Serializable {

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
    private Float downEffect;

    @ApiModelProperty(value = "小数点后的有效位数")
    private Integer metePrecision;

    @ApiModelProperty(value = "单位")
    private String unit;

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

    @ApiModelProperty(value = "信号标准化编码")
    private String meteCode;;

    @ApiModelProperty(value = "设备类型")
    private String deviceType;

    @ApiModelProperty(value = "监控量描述")
    private String description;

    @ApiModelProperty(value = "标称值")
    private Float stander;

    @ApiModelProperty(value = "是否可控")
    private Integer controlenable;


}
