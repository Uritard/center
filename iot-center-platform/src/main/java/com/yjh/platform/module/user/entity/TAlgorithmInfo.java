package com.yjh.platform.module.user.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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


    private Long algorithmId;

    private String algorithmName;

    @ApiModelProperty(value = "算法类型")
    private String aliasName;

    private String describel;

    @ApiModelProperty(value = "算法编码")
    private Integer algorithmCode;

    private String analyseType;


}
