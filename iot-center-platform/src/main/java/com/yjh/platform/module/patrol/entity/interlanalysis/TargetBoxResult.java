package com.yjh.platform.module.patrol.entity.interlanalysis;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/5/30
 * @since [产品/模块版本] （可选）
 */
@Data
public class TargetBoxResult {
    /**
     * 检测框左上角 x坐标
     */
    private float x1;
    /**
     * 检测框左上角 y坐标
     */
    private float y1;
    /**
     * 检测框右下角 x坐标
     */
    private float x2;
    /**
     * 检测框右下角 y坐标
     */
    private float y2;
}
