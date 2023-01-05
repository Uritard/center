package com.yjh.platform.module.user.entity.enums;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/1/3
 * @since [产品/模块版本] （可选）
 */
public enum UserStateEnum {
    /**
     * 有效
     */
    VALID(1,"有效"),
    /**
     * 无效
     */
    INVALID(0,"无效"),
    /**
     * 锁定
     */
    LOCKED(2,"锁定");

    private int code;

    private String desc;

    UserStateEnum(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
