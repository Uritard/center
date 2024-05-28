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
@Schema(name = "采集音频文件信息", description = "采集音频文件信息")
public class CollectNotify {
    @Schema(description = "wav 文件名称")
    private String filename;

    @Schema(description = "wav 文件下载 URL")
    private String fileUrl;
}
