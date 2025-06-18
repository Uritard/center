package com.yjh.platform.module.patrol.entity.interlanalysis;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/12
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class OcrAnalyseResponse {
    /**
     * 检测框ID
     */
    private String devUuid;
    /**
     * 检测结果
     */
    private String value;
}
