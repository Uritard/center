package com.yjh.platform.audiodevice.impl.standard;

/**
 * <功能描述>
 *
 * @author zilong
 * @date 2022/4/17
 * @since [产品/模块版本] （可选）
 */
public enum ActionType {
    /**
     * 开始录音
     */
    START("ON"),

    /**
     * 结束录音
     */
    STOP("OFF");

    private final String code;

    ActionType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
