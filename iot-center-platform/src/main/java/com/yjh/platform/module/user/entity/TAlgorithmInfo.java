package com.yjh.platform.module.user.entity;

import java.io.Serializable;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
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
 * @since 2020-08-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TAlgorithmInfo对象", description = "算法表")
public class TAlgorithmInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    @Max(value = 999999999999999999l)
    @TableField(value = "algorithm_id", updateStrategy = FieldStrategy.IGNORED)
    private Long algorithmId;

    @Length(max = 64, message = "algorithmName长度必须小于等于64")
    @TableField(value = "algorithm_name", updateStrategy = FieldStrategy.IGNORED)
    private String algorithmName;

    @Length(max = 64, message = "aliasName长度必须小于等于64")
    @ApiModelProperty(value = "算法类型")
    @TableField(value = "alias_name", updateStrategy = FieldStrategy.IGNORED)
    private String aliasName;

    @Length(max = 255, message = "describel长度必须小于等于255")
    @TableField(value = "describel", updateStrategy = FieldStrategy.IGNORED)
    private String describel;

    @Length(max = 20, message = "algorithmCode长度必须小于等于20")
    @ApiModelProperty(value = "算法编码")
    private String algorithmCode;

    @Length(max = 11, message = "analyseType长度必须小于等于11")
    @TableField(value = "analyse_type", updateStrategy = FieldStrategy.IGNORED)
    private String analyseType;

    @Max(value = 9)
    private Integer isAi;

    private Integer defectType;

    private Integer defectLevel;

    private String defectLevelName;
    private Integer pageNum = 1;

    private Integer pageSize = 0;

}
