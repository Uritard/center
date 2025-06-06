package com.yjh.platform.module.patrol.entity.interlanalysis;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/5
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class TargetBoxRequest {
    /**
     * 待识别图片路径
     * 传绝对路径
     */
    private String imagePath;

    /**
     * type  类型   "zz_bj","sx_bj","yb","zsd","xn"
     */
    private String type;

}
