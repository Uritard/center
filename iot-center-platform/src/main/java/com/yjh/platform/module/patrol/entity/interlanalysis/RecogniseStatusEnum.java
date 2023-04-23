package com.yjh.platform.module.patrol.entity.interlanalysis;

import java.util.Objects;

/**
 * 设备状态识别结果枚举类
 *
 * @author 丫C
 * @date 2022/5/1
 */
public enum RecogniseStatusEnum {

    /**
     * 刀闸-分状态
     */
    ISOLATOR_OFF(61, "分"),
    /**
     * 刀闸-合状态
     */
    ISOLATOR_ON(62, "合"),
    /**
     * 刀闸-分位异常状态
     */
    ISOLATOR_OFF_ABNORMAL(63, "分位异常"),
    /**
     * 刀闸-合位异常状态
     */
    ISOLATOR_ON_ABNORMAL(64, "合位异常"),
    /**
     * 开关/压板-分状态
     */
    SWITCH_OFF(101, "分"),
    /**
     * 开关/压板-合状态
     */
    SWITCH_ON(102, "合"),
    /**
     * 开关/压板-预留
     */
    SWITCH_RESERVED(100, "预留"),
    /**
     * 声音-正常声音
     */
    SOUND_NORMAL(131, "正常声音"),
    /**
     * 声音-异常声音
     */
    SOUND_ABNORMAL(132, "异常声音"),
    /**
     * 指示灯、闪烁灯-灯灭
     */
    LIGHT_OFF(51, "灯灭"),
    /**
     * 指示灯、闪烁灯-灯亮
     */
    LIGHT_ON(52, "灯亮"),
    /**
     * 指示灯、闪烁灯-绿灯(常)亮
     */
    LIGHT_GREEN_ON(53, "绿灯(常)亮"),
    /**
     * 指示灯、闪烁灯-红灯(常)亮
     */
    LIGHT_RED_ON(54, "红灯(常)亮"),
    /**
     * 指示灯、闪烁灯-绿灯闪烁
     */
    LIGHT_GREEN_FLICKER(55, "绿灯闪烁"),
    /**
     * 指示灯、闪烁灯-红灯闪烁
     */
    LIGHT_RED_FLICKER(56, "红灯闪烁"),
    /**
     * 指示灯、闪烁灯-红灯闪烁
     */
    UNKNOWN(-1, "结果未识别");

    private int code;
    private String value;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    private RecogniseStatusEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    /**
     * 根据value返回枚举类型
     * @param code
     * @return RecogniseStatusEnum
     */
    public static RecogniseStatusEnum getValueByCode(int code) {
        for (RecogniseStatusEnum statusEnum : RecogniseStatusEnum.values()) {
            if (Objects.equals(statusEnum.getCode(), code)) {
                return statusEnum;
            }
        }
        return null;
    }
    public static RecogniseStatusEnum getCodeByValue(String value) {
        for (RecogniseStatusEnum statusEnum : RecogniseStatusEnum.values()) {
            if (Objects.equals(statusEnum.getValue(), value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
