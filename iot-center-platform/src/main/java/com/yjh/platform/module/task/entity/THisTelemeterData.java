package com.yjh.platform.module.task.entity;

import java.util.Date;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "THisTelemeterData对象", description = "遥测历史数据表")
public class THisTelemeterData implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "ID编号")
    private Long id;

    @ApiModelProperty(value = "监控量编号")
    private Long meteId;

    @ApiModelProperty(value = "设备编号")
    private Long deviceId;

    @ApiModelProperty(value = "数值时间")
    private Date recordTime;

    @ApiModelProperty(value = "监控量种类")
    private Integer meteKind;

    @ApiModelProperty(value = "值")
    private String meteValue;

    @ApiModelProperty(value = "上一次值")
    private String lastMeteValue;

    private String meteName;


}
