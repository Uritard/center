package com.yjh.accessrobot.common.enumeration;

public enum DeviceTypeEnum {
    /**
     * 母线及绝缘子
     */
    DEVICE_TYPE_293(293, "母线及绝缘子", 12),
    /**
     * 消防系统
     */
    DEVICE_TYPE_311(311, "消防系统", 30),
    /**
     * 穿墙套管
     */
    DEVICE_TYPE_294(294, "穿墙套管", 13),
    /**
     * 站用交流电源
     */
    DEVICE_TYPE_304(304, "站用交流电源", 22),
    /**
     * 耦合电容器
     */
    DEVICE_TYPE_298(298, "耦合电容器", 16),
    /**
     * 避雷器
     */
    DEVICE_TYPE_197(197, "避雷器", 8),
    /**
     * 电力电缆
     */
    DEVICE_TYPE_295(295, "电力电缆", 0),
    /**
     * 油浸式变压器
     */
    DEVICE_TYPE_328(328, "油浸式变压器", 1),
    /**
     * 设备构架
     */
    DEVICE_TYPE_306(306, "设备构架", 24),
    /**
     * 二次屏柜
     */
    DEVICE_TYPE_310(310, "二次屏柜", 29),
    /**
     * 开关柜
     */
    DEVICE_TYPE_194(194, "开关柜", 5),
    /**
     * 串联补偿装置
     */
    DEVICE_TYPE_292(292, "串联补偿装置", 11),
    /**
     * 高频阻波器
     */
    DEVICE_TYPE_297(297, "高频阻波器", 15),
    /**
     * 接地装置
     */
    DEVICE_TYPE_301(301, "接地装置", 19),
    /**
     * 断路器
     */
    DEVICE_TYPE_154(154, "断路器", 2),
    /**
     * 避雷针
     */
    DEVICE_TYPE_309(309, "避雷针", 27),
    /**
     * 土建设施
     */
    DEVICE_TYPE_308(308, "土建设施", 26),
    /**
     * 变电站环境
     */
    DEVICE_TYPE_812(812, "变电站环境", 32),
    /**
     * 避雷器动作次数表
     */
    DEVICE_TYPE_432(432, "避雷器动作次数表", 28),
    /**
     * 端子箱及检修电源箱
     */
    DEVICE_TYPE_331(331, "端子箱及检修电源箱", 20),
    /**
     * 并联电容器组
     */
    DEVICE_TYPE_330(330, "并联电容器组", 9),
    /**
     * 站用变压器
     */
    DEVICE_TYPE_303(303, "站用变压器", 21),
    /**
     * 辅助设施
     */
    DEVICE_TYPE_307(307, "辅助设施", 25),
    /**
     * 隔离开关
     */
    DEVICE_TYPE_193(193, "隔离开关", 4),
    /**
     * 电压互感器
     */
    DEVICE_TYPE_195(195, "电压互感器", 7),
    /**
     * 高压熔断器
     */
    DEVICE_TYPE_299(299, "高压熔断器", 17),
    /**
     * 组合电器
     */
    DEVICE_TYPE_182(182, "组合电器", 3),
    /**
     * 电流互感器
     */
    DEVICE_TYPE_196(196, "电流互感器", 6),
    /**
     * 站用直流电源
     */
    DEVICE_TYPE_305(305, "站用直流电源", 23),
    /**
     * 中性点隔直装置
     */
    DEVICE_TYPE_300(300, "中性点隔直装置", 18),
    /**
     * 干式电抗器
     */
    DEVICE_TYPE_291(291, "干式电抗器", 10),
    /**
     * 消弧线圈
     */
    DEVICE_TYPE_296(296, "消弧线圈", 14);

    Integer dictCode;
    String dictNode;
    Integer upDict;

    DeviceTypeEnum(Integer dictCode, String dictNode, Integer upDict) {
        this.dictCode = dictCode;
        this.dictNode = dictNode;
        this.upDict = upDict;
    }

    public Integer getDictCode() {
        return this.dictCode;
    }

    public String getDictNote() {
        return this.dictNode;
    }

    public Integer getUpDict() {
        return this.upDict;
    }

    public static Integer getDictCodeByUpDict(Integer upDict) {
        for (DeviceTypeEnum deviceTypeEnum:DeviceTypeEnum.values()) {
            if (deviceTypeEnum.getUpDict().equals(upDict)) {
                return deviceTypeEnum.getDictCode();
            }
        }
        return null;
    }
    public static String getDictNodeByUpDict(Integer upDict) {
        for (DeviceTypeEnum deviceTypeEnum:DeviceTypeEnum.values()) {
            if (deviceTypeEnum.getUpDict().equals(upDict)) {
                return deviceTypeEnum.getDictNote();
            }
        }
        return null;
    }
    public static Integer getUpDictByDictCode(Integer dictCode) {
        for (DeviceTypeEnum deviceTypeEnum:DeviceTypeEnum.values()) {
            if (deviceTypeEnum.getDictCode().equals(dictCode)) {
                return deviceTypeEnum.getUpDict();
            }
        }
        return null;
    }
}
