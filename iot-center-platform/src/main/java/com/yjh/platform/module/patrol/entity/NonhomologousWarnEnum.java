package com.yjh.platform.module.patrol.entity;

/**
 * <功能描述>
 *
 * @author 张新
 * @date 2023/3/2
 * @since [产品/模块版本] （可选）
 */
public enum NonhomologousWarnEnum {

    ALL("全部",-1),
    HONGWAI("红外测温",1),
    WEIZHI("位置状态类",2),
    SHUXIAN("数显表计类",3),
    ZHIZHEN("指针表计类",4),
    SANXIANG("三相",5),
    QUSHI("趋势对比类",6),
    WUCI("五次不变类",7);

    String name;
    Integer code;

    NonhomologousWarnEnum(String name, Integer code) {
        this.name = name;
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public Integer getCode() {
        return code;
    }
}
