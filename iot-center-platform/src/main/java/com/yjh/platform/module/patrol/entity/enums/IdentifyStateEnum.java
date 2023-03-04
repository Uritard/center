package com.yjh.platform.module.patrol.entity.enums;

import io.swagger.models.auth.In;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/3/3
 * @since [产品/模块版本] （可选）
 */
public enum IdentifyStateEnum {
    /**
     * 算法识别正确
     */
    CORRECT(258,"识别正确"),
    /**
     * 算法识别错误
     */
    FAULT(259,"识别错误");

    Integer code;
    String desc;

    IdentifyStateEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
