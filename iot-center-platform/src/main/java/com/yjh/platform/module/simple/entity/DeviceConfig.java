package com.yjh.platform.module.simple.entity;

import com.yjh.platform.module.patrol.entity.interlanalysis.CalibrationBaseParams;
import com.yjh.platform.module.patrol.entity.interlanalysis.CalibrationParams;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <功能描述>
 *  子点位配置信息
 *
 * @author huyuhang
 * @date 2025/6/6
 * @since [产品/模块版本] （可选）
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DeviceConfig extends CalibrationBaseParams {
    /**
     * 设备类型
     */
    private String devType;
    /**
     * 设备目标区域
     */
    private List<List<Integer>> tmplDevPos;
    /**
     * 标定参数
     */
    private List<CalibrationParams> params;
    /**
     * 指示灯类型的参数
     */
    private String cascadeType;
}
