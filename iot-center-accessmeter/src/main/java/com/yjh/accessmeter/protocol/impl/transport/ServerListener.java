/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.impl.transport;

import io.netty.channel.ChannelHandlerContext;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/20
 * @since [产品/模块版本] （可选）
 */
public interface ServerListener {
    String dispatch(ChannelHandlerContext ctx, String message);
}
