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
 * @since 2020-08-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysRoleRegion对象", description = "权限区域表")
public class SysRoleRegion implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "roleId不为空")
    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "角色ID")
    private Long roleId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "机器人ID")
    private Long regionId;

    @Length(max = 12,message = "isChecked长度必须小于等于12")
    private String isChecked;


}
