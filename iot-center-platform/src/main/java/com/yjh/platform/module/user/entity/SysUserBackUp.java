package com.yjh.platform.module.user.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import java.io.Serializable;
import java.util.Date;

/**
 * @author YC
 * @date 2020/8/28 - 11:10
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysUserBackUp对象", description = "用户备份表")
public class SysUserBackUp implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "用户id")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    @Length(max = 20,message = "userName长度必须小于等于20")
    @ApiModelProperty(value = "用户名")
    @TableField(value = "user_name",updateStrategy = FieldStrategy.IGNORED)
    private String userName;

    @Length(max = 256,message = "password长度必须小于等于256")
    @ApiModelProperty(value = "密码")
     @TableField(value = "PASSWORD",updateStrategy = FieldStrategy.IGNORED)
    private String password;

    @Length(max = 20,message = "trueName长度必须小于等于20")
    @ApiModelProperty(value = "真实姓名")
    @TableField(value = "true_name",updateStrategy = FieldStrategy.IGNORED)
    private String trueName;

    @Max(value=9)
    @ApiModelProperty(value = "账号1-在线，0-离线")

    private Integer userType;

    @Max(value=9)
    @ApiModelProperty(value = "性别0女，1 男，2未知")
    private Integer sex;

    @Length(max = 20,message = "eMail长度必须小于等于20")
    @ApiModelProperty(value = "email")
    @TableField(value = "e_mail",updateStrategy = FieldStrategy.IGNORED)
    private String eMail;

    @Length(max = 20,message = "mobilePhone长度必须小于等于20")
    @ApiModelProperty(value = "手机号")
    @TableField(value = "mobile_phone",updateStrategy = FieldStrategy.IGNORED)
    private String mobilePhone;

    @Length(max = 30,message = "workNo长度必须小于等于30")
    @ApiModelProperty(value = "工号")
     @TableField(value = "work_no",updateStrategy = FieldStrategy.IGNORED)
    private String workNo;

    @Length(max = 32,message = "faceId长度必须小于等于32")
    @ApiModelProperty(value = "人脸ID")
     @TableField(value = "face_id",updateStrategy = FieldStrategy.IGNORED)
    private String faceId;

    @Length(max = 50,message = "fingerId长度必须小于等于50")
    @ApiModelProperty(value = "指纹ID")
     @TableField(value = "finger_id",updateStrategy = FieldStrategy.IGNORED)
    private String fingerId;

    @Length(max = 32,message = "voiceId长度必须小于等于32")
    @ApiModelProperty(value = "声纹ID")
     @TableField(value = "voice_id",updateStrategy = FieldStrategy.IGNORED)
    private String voiceId;

    @Max(value=9)
    @ApiModelProperty(value = "1 正常，0 删除，2 锁定")
    private Integer state;

    @Length(max = 32,message = "userTitle长度必须小于等于32")
    @ApiModelProperty(value = "用户职称")
     @TableField(value = "user_title",updateStrategy = FieldStrategy.IGNORED)
    private String userTitle;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "创建人")
    @TableField(value = "creator_id",updateStrategy = FieldStrategy.IGNORED)
    private Long creatorId;

    @Length(max = 20,message = "appkey长度必须小于等于20")
    @ApiModelProperty(value = "用户appkey 建议使用10位随机数——用户ID")
     @TableField(value = "appkey",updateStrategy = FieldStrategy.IGNORED)
    private String appkey;

    @Length(max = 255,message = "imageUrl长度必须小于等于255")
    @ApiModelProperty(value = "头像路径")
     @TableField(value = "image_url",updateStrategy = FieldStrategy.IGNORED)
    private String imageUrl;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "角色ID")
    @TableField(value = "role_id",updateStrategy = FieldStrategy.IGNORED)
    private Long roleId;

    @Max(value=999999999999999999l)
    @ApiModelProperty(value = "组织机构ID")
     @TableField(value = "org_id",updateStrategy = FieldStrategy.IGNORED)
    private Long orgId;

    @Max(value=9999)
    @ApiModelProperty(value = "员工状态")
    private Integer userStatus;

    @Past
    @ApiModelProperty(value = "创建日期",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    @Past
    @ApiModelProperty(value = "修改日期",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    @Past
    @ApiModelProperty(value = "失效时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date invalidTime;

    @Past
    @ApiModelProperty(value = "最后登录时间",example = "2018-10-01 12:18:48")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastLogin;


}
