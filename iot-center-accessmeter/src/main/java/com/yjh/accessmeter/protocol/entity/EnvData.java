/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/1
 * @since [产品/模块版本] （可选）
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class EnvData extends EnvBase {

    List<DataItem> data;
}
