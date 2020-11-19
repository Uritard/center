package com.yjh.platform.module.task.entity;

import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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


    @ApiModelProperty(value = "角色ID")
    private Integer subType;

    @ApiModelProperty(value = "实例ID")
    private Long instanceId;

    private String remark;


}
