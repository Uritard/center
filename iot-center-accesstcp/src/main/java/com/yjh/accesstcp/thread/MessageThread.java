/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.utils.ThreadPoolUtil;
import com.yjh.accesstcp.module.device.callback.CallbackHandlerStrategy;
import com.yjh.accesstcp.module.device.callback.CallbackHandlerStrategyFactory;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.BaseModel;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.IHandlerEnum;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategy;
import com.yjh.accesstcp.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import com.yjh.accesstcp.netty.handler.iot.IotHandlerEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/6/22
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class MessageThread {

    public static <T extends BaseModel> void doProcessMessage(ProtocolEnum type, TCPClientHandler clientHandler, T xmlBaseModel,
                                                              MessageHeader header) {
        ThreadPoolUtil.PATROL_POOL.execute(() -> {
            try {
                processMessage(type, clientHandler, xmlBaseModel, header);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    private static <T extends BaseModel> void processMessage(ProtocolEnum protocol, TCPClientHandler clientHandler, T xmlBaseModel,
                                                             MessageHeader header) {
        // 报文处理
        try {
            boolean flag = header.getSessionType() == (byte) 1;
            String type = flag ? IHandlerEnum.RES + xmlBaseModel.getType() : xmlBaseModel.getType();
            MessageHandlerStrategy messageHandlerStrategy =
                    MessageHandlerStrategyFactory.getStrategyType(protocol, IHandlerEnum.getEnm(type));
            if (Optional.ofNullable(messageHandlerStrategy).isPresent()) {
                messageHandlerStrategy.handler(clientHandler, xmlBaseModel, header);
            } else {
                log.warn("消息处理未定义，type: {}", type);
                clientHandler.normalResponse("400", header.getSessionId());
            }
            CallbackHandlerStrategy callbackHandlerStrategy =
                    CallbackHandlerStrategyFactory.getStrategyType(clientHandler.getRootName() + header.getReceiveSessionId());
            if (Optional.ofNullable(callbackHandlerStrategy).isPresent()) {
                callbackHandlerStrategy.handler(clientHandler, xmlBaseModel, header);
            } else {
                log.info("回调未定义,不处理。reSessionId: {}", header.getReceiveSessionId());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
