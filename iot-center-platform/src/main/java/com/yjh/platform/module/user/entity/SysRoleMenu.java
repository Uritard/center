package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
 * @since 2020-07-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysRoleMenu对象", description = "角色菜单表")
public class SysRoleMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "菜单ID")
    @TableId(value = "rp_id", type = IdType.AUTO)
    @TableField(value = "rp_id",updateStrategy = FieldStrategy.IGNORED)
    private Long rpId;

    @Length(max = 20,message = "menuCode长度必须小于等于20")
    @ApiModelProperty(value = "菜单编码")
    @TableField(value = "menu_code",updateStrategy = FieldStrategy.IGNORED)
    private String menuCode;

    @Max(value=999999999)
    @ApiModelProperty(value = "菜单排序")
    private Integer sort;

    @Length(max = 255,message = "elementCode长度必须小于等于255")
    @ApiModelProperty(value = "元素编码列表")
     @TableField(value = "element_code",updateStrategy = FieldStrategy.IGNORED)
    private String elementCode;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "角色id")
     @TableField(value = "role_id",updateStrategy = FieldStrategy.IGNORED)
    private Long roleId;


}
