package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;

/**
 * @author tt
 * @since 2020-07-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysRole对象", description = "角色表")
public class SysRole implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "主键")
    @TableId(value = "role_id", type = IdType.AUTO)
    @TableField(value = "role_id",updateStrategy = FieldStrategy.IGNORED)
    private Long roleId;

    @Length(max = 20,message = "roleName长度必须小于等于20")
    @ApiModelProperty(value = "角色名称")
    @TableField(value = "role_name",updateStrategy = FieldStrategy.IGNORED)
    private String roleName;


    @ApiModelProperty(value = "创建时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "创建人")
     @TableField(value = "creator_id",updateStrategy = FieldStrategy.IGNORED)
    private Long creatorId;

    @Max(value=9)
    @ApiModelProperty(value = "1系统权限，0非系统权限")
    private Integer sysState;
    private Integer pageNum=1;

    private Integer pageSize=0;

}
