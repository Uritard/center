package com.yjh.accesstcp.module.device.service.uphandler;

import com.yjh.accesstcp.netty.handler.IHandlerEnum;
import lombok.Getter;

/**
 * 上报上级协议枚举
 * @author Chenfei
 * @date 2024/4/15
 * @since [产品/模块版本] （可选）
 */
@Getter
public enum UpHandlerEnum {
    /**
     * 消息返回
     * 所有消息返回统一处理
     */
    RESPONSE(IHandlerEnum.RES + "251", "response"),
    /**
     * 任务状态上报
     */
    TASK_STATE("41", "taskState"),
    /**
     * 任务结果上报
     */
    CRUISE_RESUL("61", "cruiseResul");

    UpHandlerEnum(String type, String desc) {
        this.type = type;
        this.desc = desc;
    }

    /**
     * 消息类型，若是请求返回消息以R开头
     */
    private final String type;
    /**
     * 协议描述
     */
    private final String desc;

}
