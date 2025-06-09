package com.yjh.platform.module.simple.entity;

import lombok.Data;

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
    private Long robotId;

    /**
     * 设备id
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 底图信息
     */
    private List<PointPhotoInfo> photoInfo;

}
