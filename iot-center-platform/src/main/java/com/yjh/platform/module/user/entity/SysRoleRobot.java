package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author tt
 * @since 2020-07-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysRoleRobot对象", description = "角色和机器人关联表")
public class SysRoleRobot implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "角色ID")
    @TableField(value = "role_id",updateStrategy = FieldStrategy.IGNORED)
    private Long roleId;

    @NotNull(message = "roleId不为空")
    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "机器人ID")
    @TableField(value = "robot_id",updateStrategy = FieldStrategy.IGNORED)
    private Long robotId;

    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
