package com.yjh.platform.module.simple.entity;

import lombok.Data;

/**
 * <功能描述>
 * 点位底图信息
 *
 * @author huyuhang
 * @date 2025/6/6
 * @since [产品/模块版本] （可选）
 */
@Data
public class PointPhotoInfo {
    /**
     * 底图序号
     */
    private Integer photoNum;
    /**
     * 底图url
     */
    private String photoPath;
    /**
     * 底图名称
     */
    private String photoName;
}
