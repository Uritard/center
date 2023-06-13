package com.yjh.platform.module.patrol.entity.interlanalysis;

import java.util.Objects;

/**
 * 设备状态识别结果枚举类
 *
 * @author 丫C
 * @date 2022/5/1
 */
public enum RecogniseStatusExEnum {

    //这几个选算法的时候是不确定选刀闸还是开关的

    /**
     * 就地
     */
    ISOLATOR_OFF(61, "就地"),
    /**
     * 远方
     */
    ISOLATOR_ON(62, "远方"),
    /**
     * 储能
     */
    ISOLATOR_OFF_ABNORMAL(101, "储能"),
    /**
     * 非储能
     */
    ISOLATOR_ON_ABNORMAL(102, "非储能"),
    /**
     * 开关/压板-开状态
     */
    GATE_SWITCH_OFF(101, "开"),
    /**
     * 开关/压板-关状态
     */
    GATE_SWITCH_ON(102, "关")
    ;

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

    private RecogniseStatusExEnum(int code, String value) {
        this.code = code;
        this.value = value;
    }

    /**
     * 根据value返回枚举类型
     * @param code
     * @return RecogniseStatusEnum
     */
    public static RecogniseStatusExEnum getValueByCode(int code) {
        for (RecogniseStatusExEnum statusEnum : RecogniseStatusExEnum.values()) {
            if (Objects.equals(statusEnum.getCode(), code)) {
                return statusEnum;
            }
        }
        return null;
    }
    public static RecogniseStatusExEnum getCodeByValue(String value) {
        for (RecogniseStatusExEnum statusEnum : RecogniseStatusExEnum.values()) {
            if (Objects.equals(statusEnum.getValue(), value)) {
                return statusEnum;
            }
        }
        return null;
    }
}
