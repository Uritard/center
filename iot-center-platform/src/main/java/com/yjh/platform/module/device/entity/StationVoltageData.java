package com.yjh.platform.module.device.entity;

import lombok.Data;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/17
 * @since [产品/模块版本] （可选）
 */
@Data
public class StationVoltageData {

    private int count;

    private String voltageLevel;

    private String voltageLevelName;
}
