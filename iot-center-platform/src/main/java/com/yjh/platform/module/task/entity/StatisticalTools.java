package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/9/15 - 15:19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "StatisticalTools对象", description = "统计实体类")
public class StatisticalTools  implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "字典编码")
    private Integer cType;

    @ApiModelProperty(value = "字典描述")
    private String planType;

    @ApiModelProperty(value = "字典编码")
    private Integer state;

    @ApiModelProperty(value = "字典描述")
    private String dataState;

    @ApiModelProperty(value = "次数")
    private Integer count;

}
