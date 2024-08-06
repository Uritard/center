package com.yjh.demo.handler;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.config.ServerConfig;
import com.yjh.demo.task.AutomationTask;
import com.yjh.demo.ws.message.AInterfaceMessage;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.BaseMessageHandler;
import com.yjh.messager.api.msg.ExtPeerState;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.protocol_a.InboundMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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

    private final SimpleMessageSender wsMessageSender;
    private final AutomationTask automationTask;

    public DemoServerHandler(ExecutorService serverMsgProcessingExecutor, RxBus serverRxBus
            , SimpleMessageSender wsMessageSender, AutomationTask automationTask) {
        super(serverMsgProcessingExecutor, serverRxBus);
        this.wsMessageSender = wsMessageSender;
        this.automationTask = automationTask;
        subscribeInbound(ExtPeerState.class, InboundMessage.class);
    }

    @Override
    protected void handleMessage(BaseMessage baseMsg) {
        log.info("Recv Msg: {}", baseMsg);
        InboundMessage inboundMessage = (InboundMessage) baseMsg;
        ServerConfig.sessionId = inboundMessage.getSessionId();
        log.info("接收到客户端的内容: \n{}\n接收到客户端的sessionId: {}, 发送会话序列号：{}, 接收会话序列号: {}, 会话源标识：0x0{}",
            new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8), ServerConfig.sessionId,
            inboundMessage.getPacket().getSendSessionId(), inboundMessage.getPacket().getReceiveSessionId(),
            inboundMessage.getPacket().getSessionType());
        automationTask.setServerReceiveCode(inboundMessage);
        String validStr = automationTask.validMessage(inboundMessage, true);
        validStr = StringUtils.isBlank(validStr) ? "<b>校验成功</b>" : "<p style='color:Crimson'>" + validStr + "</p>";

        AInterfaceMessage.AInterfaceData data = new AInterfaceMessage.AInterfaceData(
                ">>>>>> 接收客户端发送会话序列号：" + inboundMessage.getPacket().getSendSessionId() + "        " +
                        "接收会话序列号：" + inboundMessage.getPacket().getReceiveSessionId() + "        " +
                        "会话源标识：0x0" + inboundMessage.getPacket().getSessionType() + "        " +
                        "xml内容：\n" + new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8), validStr);
        wsMessageSender.send(new AInterfaceMessage(data));
    }

}
