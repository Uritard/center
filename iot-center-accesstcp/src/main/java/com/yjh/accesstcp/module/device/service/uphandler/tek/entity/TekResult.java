/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler.tek.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.yjh.accesstcp.commons.result.Result;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2024/12/6
 * @since [产品/模块版本] （可选）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TekResult extends Result {
    private boolean flag = true;
    private String msg;

    public TekResult(){
        super(1, "");
    }

    public TekResult(int code, String message) {
        super(code, message);
        this.msg = message;
    }

    public void success() {
        super.setCode(0, "success");
        this.msg = "sucess";
        this.flag = true;
    }

    public void error(String message) {
        super.setCode(1, message);
        this.msg = message;
        this.flag = false;
    }

    @Override
    public void setCode(int code, String message) {
        super.setCode(code, message);
        this.msg = message;
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        this.msg = message;
    }

    @Override
    public void setMessage(int code, String message) {
        super.setMessage(code, message);
        this.msg = message;
    }

    @Override
    @JsonIgnore
    public String getMessage(){
        return super.getMessage();
    }
}
