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
 * @since 2020-08-04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TDictBusiness对象", description = "业务字典表")
public class TDictBusiness implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999)
    @ApiModelProperty(value = "字典ID")
    @TableId(value = "dict_id", type = IdType.AUTO)
    private Integer dictId;

    @Length(max = 20,message = "dictCode长度必须小于等于20")
    @ApiModelProperty(value = "字典编码")
    @TableField(value = "dict_code",updateStrategy = FieldStrategy.IGNORED)
    private String dictCode;

    @Length(max = 50,message = "colName长度必须小于等于50")
    @ApiModelProperty(value = "字典列名")
    @TableField(value = "col_name",updateStrategy = FieldStrategy.IGNORED)
    private String colName;

    @Length(max = 128,message = "dictNote长度必须小于等于128")
    @ApiModelProperty(value = "字典描述")
     @TableField(value = "dict_note",updateStrategy = FieldStrategy.IGNORED)
    private String dictNote;

    @Max(value=999999999)
    @ApiModelProperty(value = "上级字典ID")
    private Integer upDict;

    @Length(max = 128,message = "remark长度必须小于等于128")
    @ApiModelProperty(value = "描述")
    @TableField(value = "remark",updateStrategy = FieldStrategy.IGNORED)
    private String remark;

    @Max(value=99999999)
    @ApiModelProperty(value = "排序")
    @TableField(value = "sort",updateStrategy = FieldStrategy.IGNORED)
    private Long sort;


}
