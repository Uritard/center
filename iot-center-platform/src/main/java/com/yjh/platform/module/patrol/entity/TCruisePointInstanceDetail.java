package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2021/3/11 19:45
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePointInstanceDetail对象", description = "巡视点算法详细表")
public class TCruisePointInstanceDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "测点Id")
    private Long deviceMeteId;

    @ApiModelProperty(value = "检测的ID")
    private Long devicePointId;

    @ApiModelProperty(value = "巡视点Id")
    private Long instanceId;

    @ApiModelProperty(value = "算法类型")
    private Integer analyseType;

    @ApiModelProperty(value = "是否缺陷")
    private String isAi;

    @ApiModelProperty(value = "是否判别")
    private String isJudge;

}
