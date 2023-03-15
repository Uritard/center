package com.yjh.platform.common.enums;

public enum AlarmLevelEnum {
    /**
     * 严重告警
     */
    ALARM_LEVEL_132("132", "严重告警", "0"),
    /**
     * 预警
     */
    ALARM_LEVEL_130("130", "预警", "0"),
    /**
     * 一般告警
     */
    ALARM_LEVEL_131("131", "一般告警", "0"),
    /**
     * 危急告警
     */
    ALARM_LEVEL_133("133", "危急告警", "0");

    String dictCode;
    String dictNode;
    String upDict;

    AlarmLevelEnum(String dictCode, String dictNode, String upDict) {
        this.dictCode = dictCode;
        this.dictNode = dictNode;
        this.upDict = upDict;
    }

    public String getDictCode() {
        return this.dictCode;
    }

    public String getDictNote() {
        return this.dictNode;
    }

    public String getUpDict() {
        return this.upDict;
    }

    public static String getDictNodeByDictCode(String dictCode) {
        for (AlarmLevelEnum value : AlarmLevelEnum.values()) {
            if (value.getDictCode().equals(dictCode)) {
                return value.getDictNote();
            }
        }
        return null;
    }
    public static AlarmLevelEnum getDictByDictCode(String dictCode) {
        for (AlarmLevelEnum value : AlarmLevelEnum.values()) {
            if (value.getDictCode().equals(dictCode)) {
                return value;
            }
        }
        return null;
    }
}
