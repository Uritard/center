package com.yjh.accessrobot.common.enumeration;

public enum MeterTypeEnum {
    /**
     * 避雷器动作次数表
     */
    METER_TYPE_412(412, "避雷器动作次数表", 2),

    /**
     * 油温表
     */
    METER_TYPE_417(417, "油温表", 7),

    /**
     * 档位表
     */
    METER_TYPE_418(418, "档位表", 8),

    /**
     * 泄漏电流表
     */
    METER_TYPE_413(413, "泄漏电流表", 3),

    /**
     * SF6压力表
     */
    METER_TYPE_414(414, "SF6压力表", 4),

    /**
     * 液压表
     */
    METER_TYPE_415(415, "液压表", 5),

    /**
     * 油位表
     */
    METER_TYPE_411(411, "油位表", 1),

    /**
     * 气压表
     */
    METER_TYPE_419(419, "气压表", 9),

    /**
     * 开关动作次数表
     */
    METER_TYPE_416(416, "开关动作次数表", 6);

    Integer dictCode;
    String dictNode;
    Integer upDict;

    MeterTypeEnum(Integer dictCode, String dictNode, Integer upDict) {
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
        for (MeterTypeEnum meterTypeEnum : MeterTypeEnum.values()) {
            if (meterTypeEnum.getUpDict().equals(upDict)) {
                return meterTypeEnum.getDictCode();
            }
        }
        return null;
    }
    public static Integer getDictCodeByNote(String note) {
        for (MeterTypeEnum meterTypeEnum : MeterTypeEnum.values()) {
            if (meterTypeEnum.getDictNote().equals(note)) {
                return meterTypeEnum.getDictCode();
            }
        }
        return null;
    }
}

