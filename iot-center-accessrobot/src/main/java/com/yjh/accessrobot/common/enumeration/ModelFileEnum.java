package com.yjh.accessrobot.common.enumeration;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.Set;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/2/2
 * @since [产品/模块版本] （可选）
 */
public enum ModelFileEnum {
    /**
     * 机器人模型文件
     */
    DEVICE("device_file_path","1"),
    HOST("host_file_path","2"),
    ROBOT("robot_file_path","3"),
    VIDEO("video_file_path","4"),
    DRONE("drone_file_path","5"),
    VOICE("voice_file_path","6"),
    OVERHAULAREA("overhaularea_file_path","8"),
    MAP("map_file_path","9"),
    SOURCE("source_file_path","10")
    ;

    /**
     * 模型文件名称
     */
    private String name;
    /**
     * 模型文件编码
     */
    private String code;

    ModelFileEnum(String name, String code) {
        this.name = name;
        this.code = code;
    }

    public static String getCodeByName(String name) {
        for (ModelFileEnum modelFileEnum : ModelFileEnum.values()) {
            if(modelFileEnum.name.equals(name)) {
                return modelFileEnum.code;
            }
        }
        return null;
    }

    public static Map<String,String> getMapByNameSet(Set<String> name) {
        Map<String,String> map = Maps.newHashMap();
        for (ModelFileEnum modelFileEnum : ModelFileEnum.values()) {
            if(name.contains(modelFileEnum.name)) {
                map.put("name",modelFileEnum.name);
                map.put("code",modelFileEnum.code);
            }
        }
        return null;
    }
}
