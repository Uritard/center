package com.yjh.platform.module.user.entity;

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
import javax.validation.constraints.NotNull;

/**
 * @author tt
 * @since 2020-08-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TSysParam对象", description = "系统参数表")
public class TSysParam implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999)
    @ApiModelProperty(value = "参数ID")
    @TableId(value = "param_id", type = IdType.AUTO)
    private Integer paramId;

    @Length(max = 20,message = "paramCode长度必须小于等于20")
    @ApiModelProperty(value = "参数编码")
    @TableField(value = "param_code",updateStrategy = FieldStrategy.IGNORED)
    private String paramCode;

    @Length(max = 50,message = "paramType长度必须小于等于50")
    @ApiModelProperty(value = "参数类型")
    private String paramType;

    private String paramTypeName;

    @Length(max = 50,message = "paramName长度必须小于等于50")
    @ApiModelProperty(value = "参数名称")
    @TableField(value = "param_name",updateStrategy = FieldStrategy.IGNORED)
    private String paramName;

    @Length(max = 1000,message = "content长度必须小于等于1000")
    @ApiModelProperty(value = "参数内容")
    @TableField(value = "content",updateStrategy = FieldStrategy.IGNORED)
    private String content;

    @Length(max = 128,message = "remark长度必须小于等于128")
    @ApiModelProperty(value = "描述")
    @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;


}
