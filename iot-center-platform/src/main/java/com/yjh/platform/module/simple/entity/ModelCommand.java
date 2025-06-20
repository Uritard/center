/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.simple.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 简易机器人模型下发
 * @author Chenfei
 * @date 2025-06-18
 * @since [产品/模块版本] （可选）
 */
@Data
@ApiModel(value = "ModelSend对象", description = "机器人模型下发表")
public class ModelCommand {
    /**
     * 简易机器人ID
     */
    @NotNull(message = "机器人ID不可为空！")
    @ApiModelProperty(value = "简易机器人ID")
    private long robotId;
    /**
     * 模型同步指令
     * <1>:=三维模型
     * <2>:=点位模型
     * <3>:=参考路径模型
     */
    @ApiModelProperty(value = "模型同步指令，为空则全部下发，1-三维模型 2-点位模型 3-参考路径模型")
    private String command;
}
