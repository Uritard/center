package com.yjh.platform.module.simple.entity;

import java.util.Arrays;

/**
 * <功能描述>
 * 新算法  对应类型 识别状态 描述和名称
 *
 * @author huyuhang
 * @date 2025/6/9
 * @since [产品/模块版本] （可选）
 */
public enum AnalyseMeteTypeEnum {
    /**
     * 新表计-指针  识别类型 221-表计读数
     */
    NEW_ZZ_BJ(17, 221, "zhizhen", "新表计-指针"),
    /**
     * 新表计-数显  识别类型 222-表计读数
     */
    NEW_SX_BJ(18, 221, "wenzi", "新表计-数显"),
    /**
     * 新压板  识别类型 219-位置状态识别
     */
    NEW_YB(19, 219, "yaban", "新压板"),
    /**
     * 新指示灯  识别类型 219-位置状态识别
     */
    NEW_ZSD(20, 219, "zhishideng", "新指示灯"),
    /**
     * 新旋钮  识别类型 219-位置状态识别
     */
    NEW_XN(21, 219, "xuanniu", "新旋钮");

    /**
     * 算法类型 id
     */
    private final int type;
    /**
     * 识别类型 code
     */
    private final int code;
    /**
     * 算法类型名称
     */
    private final String name;
    /**
     * 算法类型描述
     */
    private final String desc;

    AnalyseMeteTypeEnum(int type, int code, String name, String desc) {
        this.type = type;
        this.code = code;
        this.name = name;
        this.desc = desc;
    }

    public int getType() {
        return type;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public String getName() {
        return name;
    }

    /**
     * 根据名称获取枚举
     *
     * @param name 名称
     * @return 枚举
     */
    public static AnalyseMeteTypeEnum findAny(String name) {
        return Arrays.stream(AnalyseMeteTypeEnum.values()).filter(item -> item.getName().equals(name)).findFirst()
            .orElse(AnalyseMeteTypeEnum.NEW_ZZ_BJ);
    }

    /**
     * 判断是否存在
     *
     * @param name 名称
     * @return 是否存在
     */
    public static Boolean contains(String name) {
        return Arrays.stream(AnalyseMeteTypeEnum.values()).anyMatch(item -> item.getName().equals(name));
    }
}
