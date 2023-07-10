package com.yjh.platform.module;

import java.util.HashMap;
import java.util.Map;

/**
 * 设备树涉及常量
 *
 * @author 丫C
 * @date 2023/07/03
 * @since [产品/模块版本] （可选）
 */
public interface StdDeviceTreeConstant {
    enum PatrolDevLevel {
        /**
         * 间隔
         */
        REGION("5"),
        /**
         * 设备(巡视设备)
         */
        DEVICE("6"),
        /**
         * 好像是懒加载的啥玩意
         */
        LAZY("66");


        final String level;
        public String getLevel() {
            return level;
        }

        PatrolDevLevel(String level) {
            this.level = level;
        }

        private static final Map<String, PatrolDevLevel> LEVEL_ENUM_MAP = new HashMap<>();

        static {
            for (PatrolDevLevel value : PatrolDevLevel.values()) {
                LEVEL_ENUM_MAP.put(value.getLevel(), value);
            }
        }

        public static PatrolDevLevel getLevel(String value) {
            return LEVEL_ENUM_MAP.get(value);
        }
    }

    enum InspectedDevLevel {
        /**
         * 默认值
         */
        DEFAULT("default"),
        /**
         * 间隔
         */
        REGION("5"),
        /**
         * 设备(被巡视设备)
         */
        DEVICE("6"),
        /**
         * 部件
         */
        POSITION("7"),
        /**
         * 测点
         */
        DEVICE_METE("8"),
        /**
         * 巡视点
         */
        INSTANCE("9"),
        /**
         * 好像是懒加载的啥玩意
         */
        LAZY("66");

        final String level;
        public String getLevel() {
            return level;
        }

        InspectedDevLevel(String level) {
            this.level = level;
        }

        private static final Map<String, InspectedDevLevel> LEVEL_ENUM_MAP = new HashMap<>();

        static {
            for (InspectedDevLevel value : InspectedDevLevel.values()) {
                LEVEL_ENUM_MAP.put(value.getLevel(), value);
            }
        }

        public static InspectedDevLevel getLevel(String value) {
            return LEVEL_ENUM_MAP.getOrDefault(value, DEFAULT);
        }
    }

    enum DeviceShowEnum {
        /**
         * 默认值
         */
        DEFAULT("default"),
        /**
         * 所有
         */
        ALL("all"),
        /**
         * 设备
         */
        DEV("dev"),
        /**
         * 所有巡视设备
         */
        ALL_DEVICE("allDevice"),
        /**
         * 相机
         */
        CAMERA("camera"),
        /**
         * 机器人
         */
        ROBOT("robot");

        final String value;
        public String getValue() {
            return value;
        }

        DeviceShowEnum(String value) {
            this.value = value;
        }

        private static final Map<String, DeviceShowEnum> SHOW_ENUM_MAP = new HashMap<>();

        static {
            for (DeviceShowEnum value : DeviceShowEnum.values()) {
                SHOW_ENUM_MAP.put(value.getValue(), value);
            }
        }

        public static DeviceShowEnum getValue(String value) {
            return SHOW_ENUM_MAP.getOrDefault(value, DEFAULT);
        }
    }

    enum FilterType {
        /**
         * 默认值
         */
        DEFAULT("default"),
        /**
         * 区域间隔
         */
        REGION("region"),
        /**
         * 设备:相机设备、所有巡视设备、被巡视设备
         */
        DEV("dev"),
        /**
         * 巡视点
         */
        INS("ins");

        final String type;
        public String getType() {
            return type;
        }

        FilterType(String type) {
            this.type = type;
        }

        private static final Map<String, FilterType> TYPE_ENUM_MAP = new HashMap<>();

        static {
            for (FilterType value : FilterType.values()) {
                TYPE_ENUM_MAP.put(value.getType(), value);
            }
        }

        public static FilterType getType(String value) {
            return TYPE_ENUM_MAP.getOrDefault(value, DEFAULT);
        }
    }

    enum OnlineState {
        /**
         * 全部
         */
        ALL(2),
        /**
         * 在线
         */
        ONLINE(1),
        /**
         * 离线
         */
        OFFLINE(0);

        final int flag;
        public int getFlag() {
            return flag;
        }

        OnlineState(int flag) {
            this.flag = flag;
        }
    }
}
