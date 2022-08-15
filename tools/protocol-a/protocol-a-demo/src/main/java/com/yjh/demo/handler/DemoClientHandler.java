package com.yjh.demo.handler;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.ws.message.AInterfaceMessage;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.BaseMessageHandler;
import com.yjh.messager.api.msg.ExtPeerState;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.protocol_a.InboundMessage;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.OutboundMessage;
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
public class DemoClientHandler extends BaseMessageHandler {
    final MessageSender sender;
    long sessionId= 0l;

    private final SimpleMessageSender wsMessageSender;

    public DemoClientHandler(
            ExecutorService clientMsgProcessingExecutor,
            RxBus clientRxBus,
            MessageSender clientMessageSender,
            SimpleMessageSender wsMessageSender
    ) {
        super(clientMsgProcessingExecutor, clientRxBus);

        this.sender = clientMessageSender;
        this.wsMessageSender = wsMessageSender;
        subscribeInbound(ExtPeerState.class, InboundMessage.class);
    }

    @Override
    protected void handleMessage(BaseMessage baseMsg) {
        log.info("Recv Msg: {}", baseMsg);
        if (baseMsg instanceof ExtPeerState) {
            ExtPeerState peerState = (ExtPeerState) baseMsg;
            if (peerState.getData().isConnected()) {
                Message msg = new Message();
                msg.setType("251");
                msg.setCommand("1");
                OutboundMessage outboundMessage = new OutboundMessage(msg);
                outboundMessage.setSessionId(peerState.getSessionId());
                sender.send(outboundMessage);
            }
        } else if (baseMsg instanceof InboundMessage) {
            InboundMessage inboundMessage = (InboundMessage) baseMsg;
            this.sessionId = inboundMessage.getSessionId();
            log.info("接收到服务端的内容: \n{}\n", new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8));
            log.info("接收到服务端的sessionId: {}", sessionId);
            AInterfaceMessage.AInterfaceData data = new AInterfaceMessage.AInterfaceData(
                    "发送会话序列号：" + inboundMessage.getPacket().getSendSessionId() + "        " +
                    "接收会话序列号：" + inboundMessage.getPacket().getReceiveSessionId() + "        " +
                    "会话源标识：0x0" + inboundMessage.getPacket().getSessionType()+ "        " +
                    "xml内容：" + new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8)+ "\n");
            wsMessageSender.send(new AInterfaceMessage(data));
        }
    }

    public Long getSessionId(){
        return sessionId;
    }
}
