package com.yjh.platform.module.patrol.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/9
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode
public class CalibrationData {
    /**
     * 模板ID
     */
    private String templateId;
    /**
     * 标定图片ftps地址
     */
    private String picPath;
    /**
     * 标定文件ftps地址
     */
    private String filePath;
}
