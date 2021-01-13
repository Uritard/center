package com.yjh.platform.module.user.entity;

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
 * @since 2020-08-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TAlgorithmInfo对象", description = "算法表")
public class TAlgorithmInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "algorithmId")
    @Max(value=999999999999999999l)
    private Long algorithmId;

    @Length(max = 64,message = "algorithmName长度必须小于等于64")
    private String algorithmName;

    @Length(max = 64,message = "aliasName长度必须小于等于64")
    @ApiModelProperty(value = "算法类型")
    private String aliasName;

    @Length(max = 255,message = "describel长度必须小于等于255")
    private String describel;

    @Length(max = 20,message = "algorithmCode长度必须小于等于20")
    @ApiModelProperty(value = "算法编码")
    private String algorithmCode;

    @Length(max = 11,message = "analyseType长度必须小于等于11")
    private String analyseType;

    @Max(value=9)
    private Integer isAi;


}
