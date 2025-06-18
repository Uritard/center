package com.yjh.platform.module.simple.entity;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/3
 * @since [产品/模块版本] （可选）
 */
@Data
public class BasePhotoBuild {
    /**
     * 机器人id
     */
    @NotNull(message = "robotId不为空！")
    private Long robotId;

    /**
     * 设备id
     */
    @NotNull(message = "deviceId不为空！")
    private Long deviceId;

    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空！")
    private String deviceName;

    /**
     * 中心点偏移量
     */
    private String offset;

    /**
     * 底图信息
     */
    @NotEmpty(message = "底图信息不能为空！")
    private List<PointPhotoInfo> photoInfo;

}
