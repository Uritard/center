/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.imitator.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/27
 * @since [产品/模块版本] （可选）
 */
@Data
@Schema(name = "数据采集对象", description = "数据采集对象")
public class Collect {

    @Schema(description = "采集点位标识")
    private String objectId;

    @Schema(description = "采集时长")
    private int duration;
}
