package com.yjh.accesstcp.module.device.entity;


import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TStdMete对象", description = "系统测点信息表")
public class TStdMete {
    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999L)
    @ApiModelProperty(value = "测点ID")
    @TableId(value = "std_mete_id", type = IdType.AUTO)
    @TableField(value = "std_mete_id",updateStrategy = FieldStrategy.IGNORED)
    private Long stdMeteId;

    @Max(value = 999999999)
    @ApiModelProperty(value = "设备类型")

    private Integer deviceType;

    @Length(max = 10, message = "meteType长度必须小于等于10")
    @TableField(value = "mete_type",updateStrategy = FieldStrategy.IGNORED)
    @ApiModelProperty(value = "测点类型")
    private String meteType;


    @Length(max = 128, message = "meteName长度必须小于等于128")
    @ApiModelProperty(value = "测点标准名")
    @TableField(value = "mete_name",updateStrategy = FieldStrategy.IGNORED)
    private String meteName;

    @Length(max = 128, message = "redundantType长度必须小于等于128")
    @ApiModelProperty(value = "测点标准名")
    @TableField(value = "redundant_type",updateStrategy = FieldStrategy.IGNORED)
    private String redundantType;

    @ApiModelProperty(value = "测点标准名")
    private String remark;

}
