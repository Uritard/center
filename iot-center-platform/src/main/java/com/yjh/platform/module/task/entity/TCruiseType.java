package com.yjh.platform.module.task.entity;

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
 * @since 2020-11-17
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "TCruiseType对象", description = "巡视类型关联实例点表")
public class TCruiseType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999)
    @ApiModelProperty(value = "角色ID")
    private Integer subType;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "实例ID")
    private Long instanceId;

    @Length(max = 12,message = "remark长度必须小于等于12")
    private String remark;


}
