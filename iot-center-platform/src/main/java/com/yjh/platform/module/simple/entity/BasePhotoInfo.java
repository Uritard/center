package com.yjh.platform.module.simple.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/6/3
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class BasePhotoInfo {

    /**
     * 设备Id
     */
    private Long deviceId;
    /**
     * 设备名称
     */
    private String deviceName;
    /**
     * 主图路径
     */
    private String mainPath;
    /**
     * 备图路径
     */
    private String sparePath;
    /**
     * 分割数
     */
    private Integer splitNum;
    /**
     * 中心点偏移量
     */
    private String offset;
}
