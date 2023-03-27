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
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "SysUser对象", description = "系统用户表")
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    @Max(value = 999999999999999999l)
    @ApiModelProperty(value = "用户id")
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getTrueName() {
        return trueName;
    }

    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }

    public String geteMail() {
        return eMail;
    }

    public void seteMail(String eMail) {
        this.eMail = eMail;
    }

    public String getWorkNo() {
        return workNo;
    }

    public void setWorkNo(String workNo) {
        this.workNo = workNo;
    }

    public String getFaceId() {
        return faceId;
    }

    public void setFaceId(String faceId) {
        this.faceId = faceId;
    }

    public String getFingerId() {
        return fingerId;
    }

    public void setFingerId(String fingerId) {
        this.fingerId = fingerId;
    }

    public String getVoiceId() {
        return voiceId;
    }

    public void setVoiceId(String voiceId) {
        this.voiceId = voiceId;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getUserTitle() {
        return userTitle;
    }

    public void setUserTitle(String userTitle) {
        this.userTitle = userTitle;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Integer getUserStatus() {
        return userStatus;
    }

    public void setUserStatus(Integer userStatus) {
        this.userStatus = userStatus;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getInvalidTime() {
        return invalidTime;
    }

    public void setInvalidTime(Integer invalidTime) {
        this.invalidTime = invalidTime;
    }

    public Date getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(Date lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Date getLockTime() {
        return lockTime;
    }

    public void setLockTime(Date lockTime) {
        this.lockTime = lockTime;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getpCode() {
        return pCode;
    }

    public void setpCode(String pCode) {
        this.pCode = pCode;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

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

    @Length(max = 64, message = "eMail长度必须小于等于64")
    @ApiModelProperty(value = "eMail")
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

    private String pCode;

    private Integer pageNum = 1;

    private Integer pageSize = 0;

    private String identifier;
}
