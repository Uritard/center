package com.yjh.accessudp.module.device.entity;

/**
 * @author hyh
 * @since 2022/11/18
 **/
public enum EdgeEnum {
    /**
     * 边缘节点
     */
    EDGE_NODE("1", "边缘节点"),
    /**
     * 巡视主机
     */
    REGION_NODE("2", "巡视主机"),
    /**
     * 上级系统
     */
    UP_SYSTEM_NODE("2", "上级系统");
    /**
     * 状态值
     */
    private final String code;
    /**
     * 类型描述
     */
    private final String value;

    EdgeEnum(String code, String value){
        this.code = code;
        this.value = value;
    }

    public String getCode() {
        return code;
    }
}
