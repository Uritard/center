/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.netty.TCPClientHandler;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2024/12/19
 * @since [产品/模块版本] （可选）
 */
public class DefaultEmptyHandlerImpl implements TCPClientHandler {

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSuccess(){
        return this.code == Result.SUCCESS;
    }

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
    public void normalResponse(String code, long receiveSessionId, String message) {
        this.code = NumberUtils.toInt(code);
        this.message = message;
    }

    @Override
    public void normalResponse(String code, long receiveSessionId) {
        this.code = NumberUtils.toInt(code);
    }
}
