/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.imitator.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/27
 * @since [产品/模块版本] （可选）
 */
@Data
@Schema(name = "数据主动返回点位信息", description = "数据主动返回点位信息")
public class ResponseItem<T> {
    @Schema(description = "点位标识")
    private String objectId;

    @Schema(description = "结果编码，正确 2000")
    private int code;

    @Schema(description = "结果列表，JSON格式的数组")
    private List<T> results;
}
