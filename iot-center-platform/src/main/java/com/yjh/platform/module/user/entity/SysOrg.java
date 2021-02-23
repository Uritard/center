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
 * @since 2020-07-24
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysOrg对象", description = "组织机构表")
public class SysOrg implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "组织ID")
    @TableId(value = "org_id", type = IdType.AUTO)
    @TableField(value = "org_id",updateStrategy = FieldStrategy.IGNORED)
    private Long orgId;

    @Length(max = 50,message = "orgName长度必须小于等于50")
    @ApiModelProperty(value = "组织名称")
     @TableField(value = "org_name",updateStrategy = FieldStrategy.IGNORED)
    private String orgName;

    @Length(max = 50,message = "orgCode长度必须小于等于50")
    @ApiModelProperty(value = "组织编码")
    @TableField(value = "org_code",updateStrategy = FieldStrategy.IGNORED)
    private String orgCode;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "上级组织ID")
     @TableField(value = "up_id",updateStrategy = FieldStrategy.IGNORED)
    private Long upId;

    @Max(value=999999999)
    @ApiModelProperty(value = "排序")
    private Integer sort;

    @Past
    @ApiModelProperty(value = "创建时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Max(value=999999999999999999l)
     @TableField(value = "creator_id",updateStrategy = FieldStrategy.IGNORED)
    private Long creatorId;

    @Max(value=999999999)
    @ApiModelProperty(value = "层级")
    @Length(max = 255,message = "orgPath长度必须小于等于255")
    private Integer orgLevel;
    @TableField(value = "org_path",updateStrategy = FieldStrategy.IGNORED)
    private String orgPath;

    @Length(max = 64,message = "deptName长度必须小于等于64")
    @ApiModelProperty(value = "部门名称")
    @TableField(value = "dept_name",updateStrategy = FieldStrategy.IGNORED)
    private String deptName;

    private Integer pageNum=1;

    private Integer pageSize=0;
}
