package com.yjh.platform.module.simple.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/16
 * @since [产品/模块版本] （可选）
 */
@Data
public class MeterAnalyseResult {
    /**
     * 测点id
     */
    private String id;
    /**
     * 测点值
     */
    private String value;
}
