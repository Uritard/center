package com.yjh.platform.module.device.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;

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

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "关联巡检点定义实例表id")
    private Long instanceId;

    @Length(max = 32,message = "instanceName长度必须小于等于32")
    @ApiModelProperty(value = "巡检点实例名称")
    private String instanceName;

    @Length(max = 64,message = "attrName长度必须小于等于64")
    @ApiModelProperty(value = "属性名")
    private String attrName;

    @Length(max = 64,message = "attrValue长度必须小于等于64")
    @ApiModelProperty(value = "属性值")
    private String attrValue;

    @Length(max = 256,message = "remark1长度必须小于等于256")
    @ApiModelProperty(value = "备用1")
    private String remark1;


}
