package com.yjh.platform.module.device.entity;

import java.math.BigDecimal;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;

/**
 * @author tt
 * @since 2020-08-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdMetemodelDetail对象", description = "系统测点模版详细表")
public class TStdMeteModelDetail extends TStdDeviceMeteDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "监控量模板ID")
    @TableField(value = "model_id",updateStrategy = FieldStrategy.IGNORED)
    private Long modelId;

    @ApiModelProperty(value = "部位类型名称")
    private String customTypeName;

    @ApiModelProperty(value = "测点类型名称")
    @TableField(value = "mete_type",updateStrategy = FieldStrategy.IGNORED)
    private String meteTypeName;

    @ApiModelProperty(value = "设备类型")
    private Integer deviceType;

    @ApiModelProperty(value = "设备类型名称")
    private String deviceTypeName;

    @ApiModelProperty(value = "信号解释")
    @TableField(value = "alarm_explain",updateStrategy = FieldStrategy.IGNORED)
    private String alarmExplain;


    @ApiModelProperty(value = "customId前端接收替代量")
    private String customType;

    private String modelName;








}
