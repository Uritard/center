/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.netty.TCPClientHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2024/12/19
 * @since [产品/模块版本] （可选）
 */
@Component
public class DefaultEmptyHandlerImpl implements TCPClientHandler {

    @Override
    public String getCruise() {
        return Constant.edgeCode();
    }

    @Override
    public String getServer() {
        return Constant.server();
    }

    @Override
    public String getRootName() {
        return "PatrolHost";
    }

    @Override
    public Long getSessionId() {
        return 1L;
    }

    @Override
    public ChannelHandlerContext getChannel() {
        return null;
    }

    @Override
    public boolean isConnected() {
        return false;
    }

    @Override
    public void normalResponse(String code, long receiveSessionId) {
        log.info("this handler just print message, code: {}, receiveSessionId: {}", code, receiveSessionId);
    }
}
