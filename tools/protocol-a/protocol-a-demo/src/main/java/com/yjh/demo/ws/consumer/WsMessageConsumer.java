package com.yjh.demo.ws.consumer;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.yjh.demo.ws.WsSessionManager;
import com.yjh.messager.api.msg.BaseMessageConsumer;
import com.yjh.messager.api.msg.ExtPeerState;
import com.yjh.messager.api.msg.ExtPeerStateData;
import com.yjh.messager.api.msg.SimpleMessageSender;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

@Component
public class WsMessageConsumer extends BaseMessageConsumer {
    private final SimpleMessageSender sender;
    private final WsSessionManager wsSessionManager;

    public WsMessageConsumer(EventBus wsMessageDispatchBus,
                             SimpleMessageSender wsMessageSender,
                             WsSessionManager wsSessionManager) {
        super(wsMessageDispatchBus);
        this.sender = wsMessageSender;
        this.wsSessionManager = wsSessionManager;
    }

    @Subscribe
    public void consumePeerStateMessage(ExtPeerState peerState) {
        ExtPeerStateData data = peerState.getData();
        log.info("客户端({}){}线", peerState.getData().getClientContext().getSessionId(),
                peerState.getData().isConnected() ? "上" : "下");
        if (data.isConnected()) {
            wsSessionManager.addSession(data.getClientContext().getSessionId());
        } else {
            wsSessionManager.removeSession(data.getClientContext().getSessionId());
        }

        handleState(peerState);
    }

    private void handleState(ExtPeerState peerState) {
        StandardWebSocketSession socketSession = (StandardWebSocketSession) peerState.getData().getClientContext().getSession();
        long sessionId = peerState.getData().getClientContext().getSessionId();
        String userId = String.valueOf(socketSession.getHandshakeHeaders().get("userId"));
        log.info("sessionId:{}", sessionId);
        log.info("userId:{}", userId);
    }
}
