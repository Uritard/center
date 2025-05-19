package com.yjh.platform.module.user.entity.enums;

import lombok.Getter;

/**
 * <功能描述>
 * 相机类型枚举定义
 *
 * @author huyuhang
 * @date 2025/5/15
 * @since [产品/模块版本] （可选）
 */
@Getter
public enum CameraDeviceTypeEnum {

    /**
     * 相机类型枚举定义
     */
    ALL(-1, "全部"),
    DRONE(1, "无人机"),
    CAMERA(2, "相机"),
    SIMPLE_ROBOT(3, "简易机器人"),
    TRADITION_ROBOT(4, "传统机器人");

    private final Integer type;
    private final String value;

    CameraDeviceTypeEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }

    public Integer getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
