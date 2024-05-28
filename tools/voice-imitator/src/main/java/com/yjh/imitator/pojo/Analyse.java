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
public class Analyse {

    @Schema(description = "分析点位标识")
    private String objectId;

    @Schema(description = "声纹分析类型 ")
    private String[] typeList;

    @Schema(description = "待分析声纹的URL")
    private String voiceUrl;

    @Schema(description = "声纹数据采集的时间")
    private String voiceCollectTime;
}
