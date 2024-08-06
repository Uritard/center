/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.imitator.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/27
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
@Schema(name = "数据分析响应结果", description = "数据分析响应结果")
public class AnalyseNotify {

    @Schema(description = "分析类型")
    private String type;

    @Schema(description = "值 0-无缺陷 1-有缺陷")
    private String value;

    @Schema(description = "该缺陷类型起始时间")
    private float startTime;

    @Schema(description = "该缺陷类型结束时间")
    private float endTime;

    @Schema(description = "分析结果置信度")
    private float conf;

    @Schema(description = "分析结果描述")
    private String desc;
}
