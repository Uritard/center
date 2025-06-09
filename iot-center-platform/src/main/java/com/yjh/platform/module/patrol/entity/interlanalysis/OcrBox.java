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
public class OcrBox {
    /**
     * 坐标框唯一id
     */
    private String boxId;
    /**
     * 旋转后的坐标框
     */
    private RotateBox rotateBox;

    /**
     * 文字类型的参数
     */
    private String ocrType;

    /**
     * 旋钮文字信息
     */
    private String ocrContent;
}
