package com.yjh.platform.module.task.entity;

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
 * @author czh
 * @since 2020-08-25
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "THisSignalData对象", description = "遥信历史数据表")
public class THisSignalData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "编号ID")
    private Long Id;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "监控量编号")
    private Long meteId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备编号")
    private Long deviceId;

    @Past
    @ApiModelProperty(value = "数值时间")
    private Date recordTime;

    @Max(value=999999999)
    @ApiModelProperty(value = "监控量种类")
    private Integer meteKind;

    @Length(max = 100,message = "meteValue长度必须小于等于100")
    @ApiModelProperty(value = "值")
    private String meteValue;

    @Length(max = 100,message = "lastMeteValue长度必须小于等于100")
    @ApiModelProperty(value = "上一次值")
    private String lastMeteValue;


}
