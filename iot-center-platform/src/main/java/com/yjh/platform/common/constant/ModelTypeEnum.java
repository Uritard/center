package com.yjh.platform.common.constant;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/3/21
 * @since [产品/模块版本] （可选）
 */
public enum ModelTypeEnum {
    /**
     * 边缘节点模型
     */
    HOST("1", "边缘节点模型"),
    /**
     * 机器人模型
     */
    ROBOT("2", "机器人模型"),
    /**
     * 摄像机模型
     */
    CAMERA("3", "摄像机模型"),
    /**
     * 点位模型
     */
    METE("4", "点位模型"),
    /**
     * 无人机模型
     */
    DRONE("5", "无人机模型"),
    /**
     * 声纹模型
     */
    VOICE("6", "声纹模型"),
    /**
     * 任务模型
     */
    TASK("7", "任务模型"),
    /**
     * 检修区域配置文件
     */
    FIX("8", "检修区域配置文件"),
    /**
     * 地图文件
     */
    MAP("9", "地图文件"),
    /**
     * 设备资源信息配置文件
     */
    SOURCE("10", "设备资源信息配置文件");

    private String type;
    private String name;

    ModelTypeEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static Map<String, String> exportMap() {
        Map<String,String> map = Maps.newHashMap();
        for(ModelTypeEnum modelTypeEnum : ModelTypeEnum.values()) {
            map.put(modelTypeEnum.getType(), modelTypeEnum.getName());
        }
        return map;
    }
}
