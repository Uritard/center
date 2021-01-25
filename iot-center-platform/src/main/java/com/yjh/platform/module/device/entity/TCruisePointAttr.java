package com.yjh.platform.module.device.entity;

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
    @TableField(value = "instance_id",updateStrategy = FieldStrategy.IGNORED)
    private Long instanceId;

    @Length(max = 32,message = "instanceName长度必须小于等于32")
    @ApiModelProperty(value = "巡检点实例名称")
    @TableField(value = "instance_name",updateStrategy = FieldStrategy.IGNORED)
    private String instanceName;

    @Length(max = 64,message = "attrName长度必须小于等于64")
    @ApiModelProperty(value = "属性名")
     @TableField(value = "attr_name",updateStrategy = FieldStrategy.IGNORED)
    private String attrName;

    @Length(max = 64,message = "attrValue长度必须小于等于64")
    @ApiModelProperty(value = "属性值")
     @TableField(value = "attr_value",updateStrategy = FieldStrategy.IGNORED)
    private String attrValue;

    @Length(max = 256,message = "remark1长度必须小于等于256")
    @ApiModelProperty(value = "备用1")
    @TableField(value = "remark1",updateStrategy = FieldStrategy.IGNORED)
    private String remark1;


}
