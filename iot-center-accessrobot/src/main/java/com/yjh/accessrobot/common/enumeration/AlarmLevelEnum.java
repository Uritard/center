package com.yjh.accessrobot.common.enumeration;

import java.util.Arrays;

/**
 * <功能描述>
 *
 * @author yanhao
 * @date 2022/11/23
 * @since [产品/模块版本] （可选）
 */
public enum AlarmLevelEnum {
    EARLY_WARNING("130", "1", "预警"),
    GENERAL_ALARM("131", "2", "一般告警"),
    CRITICAL_ALARM("132", "3", "严重告警"),
    EMERGENCY_ALARM("134", "4", "危急告警");
    private String code;

    private String protocolCode;

    private String desc;

    public String getCode() {
        return code;
    }

    public String getProtocolCode() {
        return protocolCode;
    }

    public String getDesc() {
        return desc;
    }

    AlarmLevelEnum(String code, String protocolCode, String desc) {
        this.code = code;
        this.protocolCode = protocolCode;
        this.desc = desc;
    }

    public static AlarmLevelEnum getAlarmLevelByCode(String code) {
        return Arrays.stream(AlarmLevelEnum.values()).filter(alarmLevelEnum -> alarmLevelEnum.getCode().equals(code)).findFirst().orElse(null);
    }

    public static AlarmLevelEnum getAlarmLevelByProtocolCode(String protocolCode) {
        return Arrays.stream(AlarmLevelEnum.values()).filter(alarmLevelEnum -> alarmLevelEnum.getProtocolCode().equals(protocolCode)).findFirst().orElse(null);
    }
}
