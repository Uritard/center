package com.yjh.demo.handler;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.controller.DemoClientBatchController;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.BaseMessageHandler;
import com.yjh.messager.api.msg.ExtPeerState;
import com.yjh.messager.api.socket.BaseSocketClient;
import com.yjh.protocol_a.InboundMessage;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.OutboundMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;


/**
 * @ClassName: DemoBatchClientHandler
 * @Description:
 * @author: yanhao
 * @date: 2022/8/27
 */
@Slf4j
public class DemoBatchClientHandler extends BaseMessageHandler {

    final MessageSender sender;

    long sessionId = 0l;

    private Message sendMessage;


    private BaseSocketClient socketClient;

    public DemoBatchClientHandler(ExecutorService clientMsgProcessingExecutor, RxBus clientRxBus, MessageSender clientMessageSender, Message sendMessage, BaseSocketClient socketClient, long sessionId) {
        super(clientMsgProcessingExecutor, clientRxBus);
        this.sender = clientMessageSender;
        this.sendMessage = sendMessage;
        this.socketClient = socketClient;
        this.sessionId = sessionId;
        subscribeInbound(ExtPeerState.class, InboundMessage.class);
    }

    @Override
    protected void handleMessage(BaseMessage baseMsg) {
        log.info("Recv Msg ==>  {}  \nSendMsg ==> {}", baseMsg, sendMessage);
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
            socketClient.stop();
            log.info("接收到服务端的sessionId: {}", sessionId);
            try {
                Message receiveMessage = XmlToMessageUtil.decode(new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8));
                log.info("发送 SendCode={} ，接收 ReceiveCode={}  isequal = {}", sendMessage.getSendCode(), receiveMessage.getReceiveCode(), StringUtils.equals(sendMessage.getSendCode(), receiveMessage.getReceiveCode()));
                if (!StringUtils.equals(sendMessage.getSendCode(), receiveMessage.getReceiveCode())) {
                    DemoClientBatchController.errorCount.incrementAndGet();
                }
            } catch (Exception e) {
                log.error("接收message格式错误", e);
            }
        }
    }


    public Long getSessionId() {
        return sessionId;
    }
}
