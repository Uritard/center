package com.yjh.platform.module.device.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * @author lqh
 * @since 2020-10-13
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruisePointAttr对象", description = "巡检点属性表")
public class TCruisePointAttr implements Serializable {

    private static final long serialVersionUID = 1L;


    @ApiModelProperty(value = "关联巡检点定义实例表id")
    private Long instanceId;

    @ApiModelProperty(value = "巡检点实例名称")
    private String instanceName;

    @ApiModelProperty(value = "属性名")
    private String attrName;

    @ApiModelProperty(value = "属性值")
    private String attrValue;

    @ApiModelProperty(value = "备用1")
    private String remark1;


}
