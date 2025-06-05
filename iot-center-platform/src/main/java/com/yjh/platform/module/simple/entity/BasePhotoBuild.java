package com.yjh.platform.module.simple.entity;

import lombok.Data;

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
     * 底图序号
     */
    private Integer photoNum;

    /**
     * 机器人id
     */
    private Long robotId;

    /**
     * 底图url
     */
    private String photoPath;

    /**
     * 设备id
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 底图名称
     */
    private String photoName;

}
