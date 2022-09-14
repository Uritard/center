/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.user.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import java.io.Serializable;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/4/17
 * @since [产品/模块版本] （可选）
 */
@ApiModel(value = "SysKey 对象", description = "用户绑定秘钥及IP")
public class SysKey implements Serializable {

    @ApiModelProperty(value = "主键 ID", hidden = true)
    private Long keyId;

    @Max(value = 999999999999999999L)
    @ApiModelProperty(value = "用户 ID")
    private Long userId;

    @Max(value=9)
    @ApiModelProperty(value = "绑定类型，1-Ukey 2-ip")
    private Integer bindType;

    @ApiModelProperty(value = "公钥")
    private String pubKey;

    @ApiModelProperty(hidden = true)
    private String bindName;

    @Length(max = 32,message = "serialNum 长度必须小于等于32")
    @ApiModelProperty(value = "序列号/ip")
    private String serialNum;

    @Length(max = 32,message = "remark 长度必须小于等于32")
    @ApiModelProperty(value = "备注")
    private String remark;

    public enum BindEnum {
        /**
         * 绑定 ukey
         */
        UKEY(1),
        /**
         * 绑定 ip
         */
        IP(2);

        final int code;

        BindEnum(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static BindEnum of(int code) {
            for (BindEnum bindEnumenum : BindEnum.values()) {
                if (code == bindEnumenum.getCode()) {
                    return bindEnumenum;
                }
            }
            return null;
        }
    }

    public Long getKeyId() {
        return keyId;
    }

    public void setKeyId(Long keyId) {
        this.keyId = keyId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getBindType() {
        return bindType;
    }

    public void setBindType(Integer bindType) {
        this.bindType = bindType;
        BindEnum bindEnum = BindEnum.of(this.bindType);
        this.bindName = bindEnum == null ? "" : bindEnum.name();
    }

    public String getBindName() {
        if(StringUtils.isEmpty(bindName)){
            BindEnum bindEnum = BindEnum.of(bindType);
            bindName = bindEnum == null ? "" : bindEnum.name();
        }
        return bindName;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getSerialNum() {
        return serialNum;
    }

    public void setSerialNum(String serialNum) {
        this.serialNum = serialNum;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
