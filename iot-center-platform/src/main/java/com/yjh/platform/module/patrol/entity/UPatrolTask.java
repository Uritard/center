/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/11
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
@ApiModel(value = "UPatrolTask对象", description = "任务对象")
public class UPatrolTask implements Serializable {
}
