package com.yjh.demo.handler;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.ws.message.AInterfaceMessage;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.BaseMessageHandler;
import com.yjh.messager.api.msg.ExtPeerState;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.protocol_a.InboundMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;

/**
 * <功能描述>
 *
 * @author zilong
 * @date 2022/3/28
 */
@Component
@Slf4j
public class DemoServerHandler extends BaseMessageHandler {

    long sessionId = 0l;

    private final SimpleMessageSender wsMessageSender;

    public DemoServerHandler(ExecutorService serverMsgProcessingExecutor, RxBus serverRxBus
    ,SimpleMessageSender wsMessageSender) {
        super(serverMsgProcessingExecutor, serverRxBus);
        this.wsMessageSender = wsMessageSender;
        subscribeInbound(ExtPeerState.class, InboundMessage.class);
    }

    @Override
    protected void handleMessage(BaseMessage baseMsg) {
        log.info("Recv Msg: {}", baseMsg);
        InboundMessage inboundMessage = (InboundMessage) baseMsg;
        sessionId = inboundMessage.getSessionId();
        log.info("接收到客户端的内容: \n{}\n", new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8));
        log.info("接收到客户端的sessionId: {}", sessionId);
        AInterfaceMessage.AInterfaceData data = new AInterfaceMessage.AInterfaceData(new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8));
        wsMessageSender.send(new AInterfaceMessage(data));
    }

    public long getSessionId(){
        return sessionId;
    }
}
