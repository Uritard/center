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
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author tt
 * @since 2020-07-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysMenu对象", description = "菜单表")
public class SysMenu implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "主键")
    @TableId(value = "menu_id", type = IdType.AUTO)
    @TableField(value = "menu_id",updateStrategy = FieldStrategy.IGNORED)
    private Long menuId;

    @Length(max = 20,message = "menuName长度必须小于等于20")
    @ApiModelProperty(value = "菜单名称")
     @TableField(value = "menu_name",updateStrategy = FieldStrategy.IGNORED)
    private String menuName;

    @Length(max = 20,message = "menuCode长度必须小于等于20")
    @ApiModelProperty(value = "菜单编码")
    @TableField(value = "menu_code",updateStrategy = FieldStrategy.IGNORED)
    private String menuCode;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "父级ID")
    @TableField(value = "up_id",updateStrategy = FieldStrategy.IGNORED)
    private Long upId;

    @Length(max = 20,message = "iconCode长度必须小于等于20")
    @ApiModelProperty(value = "字体图标编码")
    @TableField(value = "icon_code",updateStrategy = FieldStrategy.IGNORED)
    private String iconCode;

    @Length(max = 255,message = "iconUrl长度必须小于等于255")
    @ApiModelProperty(value = "图标地址")
     @TableField(value = "icon_url",updateStrategy = FieldStrategy.IGNORED)
    private String iconUrl;

    @Max(value=9)
    @ApiModelProperty(value = "菜单类型")
    private Integer menuType;

    @Max(value=99)
    @ApiModelProperty(value = "菜单级别")
    private Integer menuLevel;

    @Length(max = 255,message = "elementCode长度必须小于等于255")
    @ApiModelProperty(value = "页面元素 add|edit")
    @TableField(value = "element_code",updateStrategy = FieldStrategy.IGNORED)
    private String elementCode;

    @Max(value=9)
    @ApiModelProperty(value = "状态（0 无效，1有效）")
    private Integer state;

    @Max(value=999999999)
    @ApiModelProperty(value = "排序")
    private Integer sort;

    @Max(value=9)
    @ApiModelProperty(value = "连接类型")
    private Integer linkType;

    @Length(max = 255,message = "url长度必须小于等于255")
    @ApiModelProperty(value = "连接地址")
    @TableField(value = "url",updateStrategy = FieldStrategy.IGNORED)
    private String url;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "创建人")
     @TableField(value = "creator_id",updateStrategy = FieldStrategy.IGNORED)
    private Long creatorId;

    @Max(value=9)
    @ApiModelProperty(value = "系统状态（0系统，1 非系统）")
    private Integer sysState;

    private Integer pageNum=1;

    private Integer pageSize=0;
}
