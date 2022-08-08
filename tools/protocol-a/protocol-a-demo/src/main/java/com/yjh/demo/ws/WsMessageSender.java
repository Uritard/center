package com.yjh.demo.ws;

import com.google.common.util.concurrent.RateLimiter;
import com.yjh.commons.rxbus.RxBus;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.SimpleMessageSender;

/**
 * Websocket消息发送器
 *
 * @author zilong
 * @date 2022/7/20
 */
public class WsMessageSender extends SimpleMessageSender {
    private final WsSessionManager sessionManager;
    private final RateLimiter robotStatusLimiter = RateLimiter.create(1 / 60.0);

    public WsMessageSender(RxBus rxBus, WsSessionManager sessionManager) {
        super(rxBus);

        this.sessionManager = sessionManager;
    }

    @Override
    public void send(BaseMessage msg) {
        //没有客户端在线就不要发了
        if (sessionManager.hasAnyClient()) {

            super.send(msg);
        }
    }

    private boolean shouldLog(BaseMessage msg) {
        return true;
    }
}
