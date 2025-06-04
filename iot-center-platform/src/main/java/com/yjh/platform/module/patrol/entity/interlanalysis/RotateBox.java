package com.yjh.platform.module.patrol.entity.interlanalysis;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/5/30
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class RotateBox {
    /**
     * 旋转框中心点X坐标
     */
    private float centerX;
    /**
     * 旋转框中心点Y坐标
     */
    private float centerY;
    /**
     * 旋转框宽
     */
    private float width;
    /**
     * 旋转框高
     */
    private float height;
    /**
     * 旋转角度
     */
    private float angle;

}
