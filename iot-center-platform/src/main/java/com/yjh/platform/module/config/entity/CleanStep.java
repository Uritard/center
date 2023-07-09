/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.config.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/7/4
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class CleanStep {

    @ApiModelProperty(value = "序号")
    private int order;

    @ApiModelProperty(value = "清理步骤名称")
    private String stepName;

    @ApiModelProperty(value = "清理状态，0：不需要清理  1: 清理中  2: 清理完成  3：待清理  -1: 清理失败")
    private int status;

    @ApiModelProperty(value = "清理状态对应名称")
    private String stateName;

    @ApiModelProperty(value = "进度，保留字段")
    private float rate;

    @ApiModelProperty(value = "备注信息")
    private String remark;
}
