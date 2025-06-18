package com.yjh.platform.module.device.entity;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2025/5/27
 * @since [产品/模块版本] （可选）
 */
public enum AnalyseTypeEnum {
    /**
     *  1:指针 2:计数 3:数显 4:油位 5:指示灯 6:刀闸 7:吸湿 8:物体 9:红外 "
     *  "10:开关 11:判别 12:静默监视 13:声音 14:预置位偏移检测 398:缺陷识别 15:ocr文字识别 16:目标框识别
     */
    POINTER("1", "指针"),
    COUNTING("2", "计数"),
    DISPLAY("3", "数显"),
    OIL_LEVEL("4", "油位"),
    INDICATOR("5", "指示灯"),
    DOOR_BARRIER("6", "刀闸"),
    HUMIDITY("7", "吸湿"),
    OBJECT("8", "物体"),
    INFRARED("9", "红外"),
    SWITCH("10", "开关"),
    DETECTION("11", "判别"),
    SILENT_MONITORING("12", "静默监视"),
    SOUND("13", "声音"),
    PRESET_POSITION_OFFSET_DETECTION("14", "presetCheck"),
    DEFECT_RECOGNITION("398", "缺陷识别"),
    OCR_RECOGNITION("15", "ocrAnalyse"),
    TARGET_BOX_RECOGNITION("16", "autoLabelAnalyse"),
    ANALYSE_NEW_METER_TEST("17", "analyseNewMeterTest");

    private final String code;

    private final String name;

    AnalyseTypeEnum(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    /**
     * 判断是否存在
     *
     * @param name 名称
     * @return 是否存在
     */
    public static Boolean contains(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }
        return Arrays.stream(AnalyseTypeEnum.values()).anyMatch(item -> name.contains(item.getName()));
    }
}
