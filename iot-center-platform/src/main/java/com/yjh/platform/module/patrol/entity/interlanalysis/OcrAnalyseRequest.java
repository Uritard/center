package com.yjh.platform.module.patrol.entity.interlanalysis;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/5/30
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class OcrAnalyseRequest {
    /**
     * 图片路径
     */
    private String imagePath;
    /**
     * ocr标注框
     */
    private List<OcrBox> ocrBoxes;
}
