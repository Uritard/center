package com.yjh.platform.module.simple.entity;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/6
 * @since [产品/模块版本] （可选）
 */
@Data
public class CalibrationDataBuild {

    /**
     * 外观id
     */
    @NotNull(message = "外观id不能为空")
    private Long inspectionId;

    /**
     * 标定数据
     */
    private List<DeviceConfig> deviceConfig;

}
