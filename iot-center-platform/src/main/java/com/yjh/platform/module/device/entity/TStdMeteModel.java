package com.yjh.platform.module.device.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;

/**
 * @author tt
 * @since 2020-08-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdMetemodel对象", description = "系统测点模版表")
public class TStdMeteModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "模板ID")
    @TableId(value = "model_id", type = IdType.AUTO)
    @TableField(value = "model_id",updateStrategy = FieldStrategy.IGNORED)
    private Long modelId;

    @Length(max = 128, message = "modelName长度必须小于等于128")
    @ApiModelProperty(value = "模板名称")
    @TableField(value = "model_name",updateStrategy = FieldStrategy.IGNORED)
    private String modelName;

    @Max(value = 99999999)
    @ApiModelProperty(value = "所属设备类型")
    private Integer deviceType;

    @Length(max = 200, message = "deviceTypeName长度必须小于等于200")
    @ApiModelProperty(value = "设备类型名")
    private String deviceTypeName;

    @Length(max = 512, message = "remark长度必须小于等于512")
    @ApiModelProperty(value = "模板备注")
    @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    @ApiModelProperty(value = "层级标志")
    private int level = 2;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
