package com.yjh.demo.handler;

import com.yjh.commons.rxbus.RxBus;
import com.yjh.demo.controller.DemoClientTaskContoller;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.demo.ws.message.BatchTaskMessage;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.BaseMessageHandler;
import com.yjh.messager.api.msg.ExtPeerState;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.protocol_a.InboundMessage;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.OutboundMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @ClassName: DemoBatchTaskHandler
 * @Description:
 * @author: yanhao
 * @date: 2022/9/7
 */
@Slf4j
public class DemoBatchTaskHandler extends BaseMessageHandler {


    public AtomicLong taskPatrolledId = new AtomicLong(1000000000l);

    private int index;

    private String sendCode;

    private String receiveCode;

    private MessageSender sender;

    private SimpleMessageSender wsMessageSender;


    long sessionId = 0l;

    public DemoBatchTaskHandler(Executor messageProcessingExecutor, RxBus bus, MessageSender sender, SimpleMessageSender wsMessageSender, String sendCode, String receiveCode, int index) {
        super(messageProcessingExecutor, bus);
        this.sender = sender;
        this.wsMessageSender = wsMessageSender;
        this.sendCode = sendCode;
        this.receiveCode = receiveCode;
        this.index = index;
        subscribeInbound(ExtPeerState.class, InboundMessage.class);

    }

    @Override
    protected void handleMessage(BaseMessage baseMsg) {
        log.info("sendCode:{}   sessionId:{}   index:{}  Recv Msg: {}", sendCode, sessionId, index, baseMsg);
        if (baseMsg instanceof ExtPeerState) {
            ExtPeerState peerState = (ExtPeerState) baseMsg;
            if (peerState.getData().isConnected()) {
                Message msg = new Message();
                msg.setType("251");
                msg.setCommand("1");
                msg.setSendCode(sendCode);
                msg.setReceiveCode(receiveCode);
                OutboundMessage outboundMessage = new OutboundMessage(msg);
                outboundMessage.setSessionId(peerState.getSessionId());
                sender.send(outboundMessage);
            }
        } else if (baseMsg instanceof InboundMessage) {
            InboundMessage inboundMessage = (InboundMessage) baseMsg;
            this.sessionId = inboundMessage.getSessionId();
            log.info("接收到服务端的sessionId :{} sendCode {}  接收到服务端的内容: \n{}\n", sessionId, sendCode, new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8));
            String xml = new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8);
            String msg =
                    "发送会话序列号：" + inboundMessage.getPacket().getSendSessionId() + "        " +
                            "接收会话序列号：" + inboundMessage.getPacket().getReceiveSessionId() + "        " +
                            "会话源标识：0x0" + inboundMessage.getPacket().getSessionType() + "        " +
                            "xml内容：" + xml + "\n";
            BatchTaskMessage batchTaskMessage = new BatchTaskMessage();
            batchTaskMessage.setIndex(index);
            batchTaskMessage.setMsg(msg);
            wsMessageSender.send(batchTaskMessage);
            //String转XML
            Message message = null;
            try {
                message = XmlToMessageUtil.decode(xml);
            } catch (Exception e) {
                log.error("xml 格式解析失败 xml=" + xml, e);
                return;
            }
            if (StringUtils.equals("101", message.getType()) && StringUtils.equals("1", message.getCommand())) {
                sendTaskResult(message);
            }


        }
    }

    private void sendTaskResult(Message message) {
        // 获取前端配置的任务响应报文
        Message taskResponseMsg = null;
        try {
            taskResponseMsg = XmlToMessageUtil.decode(DemoClientTaskContoller.taskXml);
        } catch (Exception e) {
            log.error("xml 解析失败", e);
            return;
        }
        //设置返回 receiveCode  sendCode
        taskResponseMsg.setReceiveCode(receiveCode);
        taskResponseMsg.setSendCode(sendCode);

        //设置返回item
        for (Map<String, Object> item : message.getItems()) {
            String taskCode = String.valueOf(item.get("task_code"));
            String taskName = String.valueOf(item.get("task_name"));
            String deviceList = item.get("device_list").toString();
            String[] deviceArray = StringUtils.split(deviceList, ",");

            Map<String, Object> taskResponseItem = taskResponseMsg.getItems().get(0);
            taskResponseItem.put("task_code", taskCode);
            taskResponseItem.put("task_name", taskName);

            for (String deviceId : deviceArray) {
                taskResponseItem.put("device_id", deviceId);
                taskResponseItem.put("task_patrolled_id", taskPatrolledId.getAndIncrement());
                OutboundMessage outboundMessage = new OutboundMessage(taskResponseMsg);
                outboundMessage.setSessionId(sessionId);
                sender.send(outboundMessage);
                log.info("任务发送响应：taskResponse:{}", taskResponseMsg);
            }
        }
    }

    public Long getSessionId() {
        return sessionId;
    }


}
