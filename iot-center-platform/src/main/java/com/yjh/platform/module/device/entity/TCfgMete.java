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
@ApiModel(value = "TCfgMete对象", description = "系统测点信息表")
public class TCfgMete implements Serializable {

    private static final long serialVersionUID = 1L;


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

    private Integer lowEffect;

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


}
