package com.yjh.platform.module.patrol.entity;

/**
 * @author 丫C
 * @date 2023/2/22
 * 系统识别类型与协议对应
 */
public enum RecognitionTypeEnum {

    /**
     * 表计读取
     */
    METER_READ("221", "1"),
    /**
     * 位置状态识别
     */
    POSITION_STATUS("219", "2"),
    /**
     * 设备外观查看
     */
    APPEARANCE_VIEW("220", "3"),
    /**
     * 红外测温
     */
    INFRARED_TEM("222", "4"),
    /**
     * 声音检测
     */
    VOICE_CHECK("223", "5"),
    /**
     * 闪烁检测
     */
    TWINKLE_CHECK("433", "6"),
    /**
     * 局放超声波检测
     */
    EMISSION_ULTRASOUND("6931", "11"),
    /**
     * 局放地电压检测
     */
    DISCHARGE_VOLTAGE("6932", "12"),
    /**
     * 局放特高频检测
     */
    AMPLIFIER_HIGH("6933", "13"),
    /**
     * 环境温度检测
     */
    TEMPERATURE_CHECK("814", "101"),
    /**
     * 环境湿度检测
     */
    HUMIDITY_CHECK("815", "102"),
    /**
     * 氧气浓度检测
     */
    OXYGEN_CHECK("816", "103"),
    /**
     * SF6浓度检测
     */
    SF6_CHECK("817","104");

    String sysRecognize;

    public String getSysRecognize() {
        return sysRecognize;
    }
    String protocolRecognize;

    RecognitionTypeEnum(String sysRecognize, String protocolRecognize){
        this.sysRecognize = sysRecognize;
        this.protocolRecognize = protocolRecognize;
    }

    public static RecognitionTypeEnum getProRecognize(String sysRecognize){
        for (RecognitionTypeEnum result : values()) {
            if (result.getSysRecognize().equals(sysRecognize)) {
                return result;
            }
        }
        return null;
    }


}
