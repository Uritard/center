package com.yjh.platform.module.patrol.entity.interlanalysis;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/17
 * @since [产品/模块版本] （可选）
 */
@Data
public class CalibrationBaseParams {
    /**
     * 设备名称
     */
    private String devName;
    /**
     * 设备编号
     */
    private String devUuid;
}
