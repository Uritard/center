/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.audiodevice.impl.standard.tcp;

import com.yjh.messager.api.msg.Msg;

/**
 * @author zilong
 * @date 2022/4/14
 */
public class MessageCodec implements Msg.Codec {

    public MessageCodec() {
    }

    @Override
    public Msg decodeMsg(Object o) {
        Packet packet = (Packet) o;
        InboundMessage inboundMessage = new InboundMessage(packet);
        return inboundMessage;
    }

    @Override
    public Object encodeMsg(Msg msg) {
        throw new UnsupportedOperationException("不需要发送数据");
    }
}
