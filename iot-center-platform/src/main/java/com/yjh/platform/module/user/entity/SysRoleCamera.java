package com.yjh.platform.module.user.entity;

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
import javax.validation.constraints.NotNull;

/**
 * @author tt
 * @since 2020-08-05
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysRoleCamera对象", description = "角色和摄像机关联表")
public class SysRoleCamera implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "角色ID")
    @TableField(value = "role_id",updateStrategy = FieldStrategy.IGNORED)
    private Long roleId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "摄像机ID")
    @TableField(value = "camera_id",updateStrategy = FieldStrategy.IGNORED)
    private Long cameraId;

    @Length(max = 12,message = "isChecked长度必须小于等于12")
    @TableField(value = "is_checked",updateStrategy = FieldStrategy.IGNORED)
    private String isChecked;


}
