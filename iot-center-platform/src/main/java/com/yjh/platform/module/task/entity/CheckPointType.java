package com.yjh.platform.module.task.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author YC
 * @date 2020/10/29 - 16:04
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "CheckPointType对象", description = "巡检记录报表-分项预览")
public class CheckPointType implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "类别")
    private String meteType;
    @ApiModelProperty(value = "测点数")
    private Integer meteNum;
    @ApiModelProperty(value = "未处理异常数")
    private Integer regularNum;
}
