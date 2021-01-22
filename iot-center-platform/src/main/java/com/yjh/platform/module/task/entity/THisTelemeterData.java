package com.yjh.platform.module.task.entity;

import java.util.Date;
import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@ApiModel(value = "THisTelemeterData对象", description = "遥测历史数据表")
public class THisTelemeterData implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "ID编号")
    @TableField(value = "id",updateStrategy = FieldStrategy.IGNORED)
    private Long id;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "监控量编号")
    @TableField(value = "mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long meteId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "设备编号")
    @TableField(value = "device_id",updateStrategy = FieldStrategy.IGNORED)
    private Long deviceId;

    @Past
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @ApiModelProperty(value = "数值时间")
    private Date recordTime;

    @Max(value=9999)
    @ApiModelProperty(value = "监控量种类")

    private Integer meteKind;

    @Length(max = 100,message = "meteValue长度必须小于等于100")
    @ApiModelProperty(value = "值")
    @TableField(value = "mete_value",updateStrategy = FieldStrategy.IGNORED)
    private String meteValue;

    @Length(max = 100,message = "lastMeteValue长度必须小于等于100")
    @ApiModelProperty(value = "上一次值")
    @TableField(value = "last_mete_value",updateStrategy = FieldStrategy.IGNORED)
    private String lastMeteValue;

}
