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
import org.hibernate.validator.constraints.Range;

import javax.validation.Valid;
import javax.validation.constraints.*;

/**
 * @author tt
 * @since 2020-07-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysUser对象", description = "系统用户表")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "用户id")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    //  @Length(max = 20,message = "userName长度必须小于等于20")
    @ApiModelProperty(value = "用户名")
    private String userName;

    // @Length(max = 256,message = "password长度必须小于等于256")
    @ApiModelProperty(value = "密码")
    private String password;

    @Length(max = 20, message = "trueName长度必须小于等于20")
    @ApiModelProperty(value = "真实姓名")
    private String trueName;

    @Max(value = 9)
    @ApiModelProperty(value = "账号1-在线，0-离线")
    private Integer userType;

    @Max(value = 9)
    @ApiModelProperty(value = "性别0女，1 男，2未知")
    private Integer sex;

    @Length(max = 20, message = "eMail长度必须小于等于20")
    @ApiModelProperty(value = "email")
    private String eMail;



    @Length(max = 30, message = "workNo长度必须小于等于30")
    @ApiModelProperty(value = "工号")
    private String workNo;

    @Length(max = 32, message = "faceId长度必须小于等于32")
    @ApiModelProperty(value = "人脸ID")
    private String faceId;

    @Length(max = 50, message = "fingerId长度必须小于等于50")
    @ApiModelProperty(value = "指纹ID")
    private String fingerId;

    @Length(max = 32, message = "voiceId长度必须小于等于32")
    @ApiModelProperty(value = "声纹ID")
    private String voiceId;

    @Max(value = 9)
    @ApiModelProperty(value = "1 正常，0 删除，2 锁定")
    private Integer state;

    @Length(max = 32, message = "userTitle长度必须小于等于32")
    @ApiModelProperty(value = "用户职称")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String userTitle;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "创建人")
    private Long creatorId;

    @Length(max = 20, message = "appkey长度必须小于等于20")
    @ApiModelProperty(value = "用户appkey 建议使用10位随机数——用户ID")
    private String appkey;

    @Length(max = 255, message = "imageUrl长度必须小于等于255")
    @ApiModelProperty(value = "头像路径")
    private String imageUrl;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "角色ID")
    private Long roleId;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "组织机构ID")
    private Long orgId;

    @Max(value = 9999)
    @ApiModelProperty(value = "员工状态")
    private Integer userStatus;


    @ApiModelProperty(value = "创建日期", example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;


    @ApiModelProperty(value = "修改日期", example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Max(value = 999999999)
    @ApiModelProperty(value = "失效时间")
//    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Integer invalidTime;


    @ApiModelProperty(value = "最后登录时间", example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLogin;

    @ApiModelProperty(value = "锁定时间时间", example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lockTime;


    private Integer pageNum = 1;

    private Integer pageSize = 0;
}
