package com.yjh.platform.module.patrol.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/5/15
 * @since [产品/模块版本] （可选）
 */
@Data
public class LabelInfo {
    /**
     * 标签名称
     */
    private String defectContent;
    /**
     * 标签是否准确 286 属实   287 不属实
     */
    private Integer dealType;
}
