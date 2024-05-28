package com.yjh.accesstcp.netty.handler.iot;

import com.yjh.accesstcp.netty.handler.IHandlerEnum;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/15
 * @since [产品/模块版本] （可选）
 */
public enum IotHandlerEnum implements IHandlerEnum {
    /**
     * 消息返回
     * 所有消息返回统一处理
     */
    RESPONSE(RES + "251", "response"),
    /**
     * 任务下发
     */
    TASK_SEND("101", "taskSend"),
    /**
     * 联动任务下发
     */
    LINKAGE_TASK_SEND("102", "linkageTaskSend"),
    /**
     * 任务控制
     */
    TASK_CONTROL("41", "taskControl"),
    /**
     * 模型同步
     */
    MODEL_SYNC("61", "modelSync"),
    /**
     * 机器人/无人机控制
     */
    MODEL_CHANGE("1,2,3,4,21,22,23,20001,20002,20003,20004,20005", "modelChange"),
    /**
     * 联动文件下发
     */
    LINKAGE_FILE("71", "linkFile"),
    /**
     * 检修区域同步
     */
    MAINTENANCE("81", "maintenance"),
    /**
     * 巡视结果统计
     */
    RESULT_STATISTICAL("121", "resultStatistical"),
    /**
     * 相机预置位图片和ptz信息同步
     */
    PRESET_SYNC("2023", "presetSync"),
    /**
     * 巡视结果确认
     */
    REVIEW_RESULT("67", "reviewResult"),
    /**
     * 告警确认
     */
    REVIEW_WARN("64", "reviewWarn"),
    /**
     * 标准点位模型下发
     */
    DEVICEMODEL_SEND("1104", "meteModelSend");


    IotHandlerEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    static {
        for (IotHandlerEnum value : IotHandlerEnum.values()) {
            handlerEnumHashMap.put(value.getType(), value);
            handlerEnumHashMap.put(value.getDesc(), value);
        }
    }

    /**
     * 消息类型，若是请求返回消息以R开头
     */
    private final String type;
    /**
     * 协议描述
     */
    private final String desc;

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getDesc() {
        return desc;
    }

}
