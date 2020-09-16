package com.yjh.platform.module.device.entity;

import java.math.BigDecimal;
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
@ApiModel(value = "TStdMetemodelDetail对象", description = "系统测点模版详细表")
public class TStdMeteModelDetail extends TStdDeviceMete implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "监控量模板ID")
    private Long modelId;

    @ApiModelProperty(value = "部位类型名称")
    private String customTypeName;

    @ApiModelProperty(value = "测点类型名称")
    private String meteTypeName;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "设备类型名称")
    private String deviceTypeName;

    @ApiModelProperty(value = "信号解释")
    private String alarmExplain;




}
