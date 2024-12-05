/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler;

import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.impl.UpType;

import java.util.stream.Stream;

/**
 * 对上级系统处理类
 * @author Chenfei
 * @date 2024/12/4
 * @since [产品/模块版本] （可选）
 */
public interface IUpHandler {

    /**
     * 具体协议类型，支持一个处理类注册多个协议
     * @return 数组，表示多个协议
     */
    default String[] command() {
        String[] types = subType().getType().split(",");
        return Stream.of(types).map(s -> upType().name() + "_" + s).toArray(String[]::new);
    }

    /**
     * 上级系统类型
     * @return 上级系统的枚举
     */
    UpType upType();

    /**
     * 具体协议类型，复用与上级通信的电网接口定义
     * @return 接口类型枚举
     */
    UpHandlerEnum subType();

    /**
     * 发送上级处理类
     * @param xmlBaseModel 发送上级内容
     * @return 200 成功  1 成功 -1 失败
     */
    int sendUpHandler(XMLBaseModel xmlBaseModel);
}
