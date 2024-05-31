/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.demo.task;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.PathUtil;
import cn.hutool.core.thread.ThreadUtil;
import com.yjh.commons.CollectionUtil;
import com.yjh.demo.entity.BaseTree;
import com.yjh.demo.entity.MessageWait;
import com.yjh.demo.service.ClientService;
import com.yjh.demo.service.IMessageSender;
import com.yjh.demo.service.ServerService;
import com.yjh.demo.util.XmlToMessageUtil;
import com.yjh.demo.ws.message.AInterfaceMessage;
import com.yjh.messager.api.msg.BaseMessage;
import com.yjh.messager.api.msg.Msg;
import com.yjh.messager.api.msg.SimpleMessageSender;
import com.yjh.protocol_a.InboundMessage;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.OutboundMessage;
import com.yjh.protocol_a.impl.MessageCodecHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.DocumentException;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Collator;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/28
 * @since [产品/模块版本] （可选）
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AutomationTask {
    private static final String PACKETS_DIR = "packets/";
    private static final List<String> STATION_LIST = new ArrayList<>();
    private static final Map<String, List<String>> PLATFORM_MAP = new TreeMap<>(Collator.getInstance(Locale.CHINA));
    private static final Map<String, List<String>> PROTOCOL_MAP = new LinkedHashMap<>(32);
    private static final Map<String, List<String>> MESSAGE_MAP = new LinkedHashMap<>(64);
    private static final Map<String, Collection<String>> COMMAND_MESSAGE_MAP = new LinkedHashMap<>(64);
    public static final Map<Long, MessageWait> SESSION_MESSAGE_MAP = new ConcurrentHashMap<>(8);

    public String CLIENT_AUTO_PROTOCOL = null;
    public String SERVER_AUTO_PROTOCOL = null;
    private boolean autoReply = true;

    private final ServerService serverService;
    private final ClientService clientService;
    private final SimpleMessageSender wsMessageSender;

    @PostConstruct
    public void initPackets() {
        try {

            File root = ResourceUtils.getFile(ResourceUtils.FILE_URL_PREFIX + PACKETS_DIR);
            if (!root.exists()) {
                root = ResourceUtils.getFile(ResourceUtils.CLASSPATH_URL_PREFIX + PACKETS_DIR);
            }
            if (!root.exists()) {
                log.error("消息初始化失败！！！");
                return;
            }
            fileList(root, null, 0);
        } catch (Exception e) {
            log.error("初始化失败", e);
        }
    }

    public String startProtocolTask(String station, String platform, boolean isServer) {
        String protocolType = station + "_" + platform;
        if (isServer) {
            SERVER_AUTO_PROTOCOL = protocolType;
        } else {
            CLIENT_AUTO_PROTOCOL = protocolType;
        }
        return MessageCodecHandler.rootTag(platform);
    }

    public List<BaseTree> protocolTree(String station, String platform) {
        List<BaseTree> trees = new ArrayList<>();
        if (StringUtils.isAnyEmpty(station, platform)) {
            PLATFORM_MAP.forEach((k, v) -> {
                BaseTree baseTree = new BaseTree(k);
                trees.add(baseTree);
                List<BaseTree> children = v.stream().map(BaseTree::new).collect(Collectors.toList());
                baseTree.setChildren(children);
            });
        } else {
            String protocalType = station + "_" + platform;
            List<String> protocols = PROTOCOL_MAP.get(protocalType);
            if (protocols == null) {
                log.warn("协议模板不存在: {}", protocalType);
                return trees;
            }
            for (String proto : protocols) {
                BaseTree baseTree = new BaseTree(proto);
                trees.add(baseTree);
                String protoKey = protocalType + "_" + proto;
                List<String> messages = MESSAGE_MAP.get(protoKey);
                if (CollectionUtils.isNotEmpty(messages)) {
                    List<BaseTree> children = messages.stream()
                        .map(m -> new BaseTree(StringUtils.substringAfterLast(m, File.separator)))
                        .collect(Collectors.toList());
                    baseTree.setChildren(children);
                }
            }
        }

        return trees;
    }

    public String messageContent(String station, String platform, String protocol, String message, boolean isServer)
        throws DocumentException {

        String protoKey = station + "_" + platform + "_" + protocol;
        List<String> messages = MESSAGE_MAP.get(protoKey);
        if (CollectionUtils.isEmpty(messages)) {
            return "请先选择协议";
        }

        File parent = FileUtil.getParent(new File(messages.get(0)), 1);
        Message messageXml = loadMessage(new File(parent, message));
        IMessageSender sender = isServer ? serverService : clientService;
        messageXml.setSendCode(sender.sendCode());
        messageXml.setReceiveCode(sender.receiveCode());
        messageXml.setTime(LocalDateTime.now());

        return MessageCodecHandler.encodeXml(sender.getMessageCodec(), platform, messageXml);
    }

    public void protocolTaskRunning(boolean isServer) {
        String protocolType = isServer ? SERVER_AUTO_PROTOCOL : CLIENT_AUTO_PROTOCOL;
        if (StringUtils.isEmpty(protocolType)) {
            throw new RuntimeException("待测试协议没有创建相应服务，请创建客户端/服务端");
        }

        ThreadUtil.execute(() -> {
            MESSAGE_MAP.forEach((k, v) -> {
                if (k.startsWith(protocolType)) {
                    for (String file : v) {
                        sendRequestMessage(file, isServer);
                    }
                }
            });
        });

    }

    private void sendRequestMessage(String file, boolean isServer) {
        String filename = FileUtil.getName(file);
        // 必须是以 request 开头的协议才自动发送
        if (!StringUtils.startsWith(filename, "request")) {
            return;
        }
        try {
            log.info("自动测试消息: {}, isServer: {}", file, isServer);
            BaseMessage msg = sendMessage(file, isServer, null);

            long msgId = (long)msg.getMsgId();
            MessageWait wait = new MessageWait(msgId, file);
            SESSION_MESSAGE_MAP.put(msgId, wait);

            synchronized (wait) {
                wait.wait(15000);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    private BaseMessage sendMessage(String file, boolean isServer, InboundMessage inboundMessage) throws DocumentException {
        Message message = loadMessage(file);

        IMessageSender sender = isServer ? serverService : clientService;
        message.setSendCode(sender.sendCode());
        message.setReceiveCode(sender.receiveCode());
        message.setTime(LocalDateTime.now());
        BaseMessage msg = sender.sendMessage(message, inboundMessage);

        String xml = MessageCodecHandler.encodeXml(sender.getMessageCodec(), message);
        String name = isServer ? "给客户端": "给服务端";
        // 打印发送日志
        AInterfaceMessage.AInterfaceData data = new AInterfaceMessage.AInterfaceData(
            name + "发送会话序列号：" + msg.getMsgId() + "        " +
                "接收会话序列号：" + (inboundMessage != null ?inboundMessage.getPacket().getReceiveSessionId() : 0) + "        " +
                "会话源标识：0x0" + (inboundMessage != null ? 1 : 0) + "        " +
                "xml内容：\n" + xml, "");
        wsMessageSender.send(new AInterfaceMessage(data));

        return msg;
    }

    public String validMessage(InboundMessage inboundMessage, boolean isServer) {
        byte retFlag = inboundMessage.getPacket().getSessionType();
        long msgId = (long)inboundMessage.getMsgId();
        long receiveSessionId = inboundMessage.getPacket().getReceiveSessionId();
        IMessageSender sender = isServer ? serverService : clientService;
        if (retFlag == (byte)0) {
            sender.updateReqMsg(inboundMessage);
        }
        StringBuilder outBuilder = new StringBuilder();

        String protocolType = isServer ? SERVER_AUTO_PROTOCOL : CLIENT_AUTO_PROTOCOL;
        if (protocolType == null) {
            log.warn("自动校验报文未启动");
            return "";
        }
        try {
            // String xml = new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8);
            //
            // Message message = XmlToMessageUtil.decode(xml);
            Message message = inboundMessage.getMsg();
            // 判断是否注册，并自动发送心跳
            scheduleHeart(message);
            String type = message.getType() + "_" + message.getCommand();
            String cmd = protocolType + "_" + type + "_" + retFlag;
            Collection<String> messageFiles = COMMAND_MESSAGE_MAP.get(cmd);
            if (CollectionUtils.isEmpty(messageFiles)) {
                outBuilder.append("消息【").append(cmd).append("】没有对应解析文件");
                return outBuilder.toString();
            }

            MessageWait wait = SESSION_MESSAGE_MAP.remove(receiveSessionId);
            String requestFile = "";
            if (wait != null) {
                requestFile = wait.getFile();
                synchronized (wait) {
                    wait.notifyAll();
                }
            }
            if (StringUtils.isEmpty(requestFile)) {
                requestFile = getRequestFile(inboundMessage, protocolType);
            }

            String file = findMessage(requestFile, messageFiles);
            if (StringUtils.isNotEmpty(file)) {
                Message localMessage = loadMessage(file);
                String valid = compareMessage(message, localMessage);
                outBuilder.append(valid).append("\n");
                // 自动发送返回报文
                if (retFlag == (byte)0 && autoReply) {
                    sendResponseMessage(file, isServer, inboundMessage);
                }
            } else {
                Path pathName = PathUtil.subPath(Paths.get(CollectionUtil.safeGetFirst(messageFiles)), -3, -1);
                outBuilder.append("消息【").append(cmd).append("】对应解析文件匹配失败，subfix: ").append(pathName);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return outBuilder.toString();
    }

    String getRequestFile(InboundMessage inboundMessage, String protocolType) {
        OutboundMessage msg = (OutboundMessage)inboundMessage.getOriginReq();
        if (msg == null) {
            log.info("下发返回报文");
            return "";
        }
        Message message = msg.getMsg();
        String type = message.getType() + "_" + message.getCommand();
        String cmd = protocolType + "_" + type + "_0";
        Collection<String> messageFiles = COMMAND_MESSAGE_MAP.get(cmd);
        if (CollectionUtils.isNotEmpty(messageFiles)) {
            return messageFiles.stream().findFirst().orElse("");
        }
        log.info("没有匹配到请求报文");
        return "";
    }

    private String findMessage(String requestFile, Collection<String> messageFiles) {
        String subfix = StringUtils.substringAfterLast(requestFile, "request_");

        String localFile = "";
        if (StringUtils.isNotEmpty(requestFile)) {
            File f1 = new File(FileUtil.getParent(new File(requestFile), 1), "response.xml");
            localFile = f1.getAbsolutePath();
        }
        for (String file : messageFiles) {
            // 自动发送但不匹配返回后缀
            boolean subfixNotMatching = StringUtils.isNotEmpty(subfix) && !StringUtils.endsWith(file, subfix);
            // 自动发送但不匹配返回
            boolean requestNotMatching = StringUtils.isNotEmpty(requestFile) && !equalsParentPath(requestFile, file);
            if (!(subfixNotMatching || requestNotMatching)) {
                localFile = file;
                break;
            }
        }
        return localFile;
    }

    private boolean equalsParentPath(String file1, String file2) {
        File f1 = FileUtil.getParent(new File(file1), 1);
        File f2 = FileUtil.getParent(new File(file2), 1);
        return FileUtil.pathEquals(f1, f2);
    }

    /**
     * 根据匹配到请求报文路径自动发送返回报文
     */
    private void sendResponseMessage(String file, boolean isServer, InboundMessage inboundMessage) {
        try {
            String subfix = StringUtils.substringAfterLast(file, "request_");
            File parent = FileUtil.getParent(new File(file), 1);
            subfix = StringUtils.isEmpty(subfix) ? ".xml" : "_" + subfix;

            File resFile = new File(parent, "response" + subfix);
            if (!resFile.exists()) {
                log.warn("指定协议不存在，继续查找: {}", resFile.getAbsolutePath());
                resFile = new File(parent, "response.xml");
            }
            if (!resFile.exists()) {
                log.error("指定默认协议不存在，请检查返回报文: {}", resFile.getAbsolutePath());
                return;
            }
            sendMessage(resFile.getAbsolutePath(), isServer, inboundMessage);
        } catch (Exception e) {
            log.error("自动发送返回报文失败", e);
        }
    }

    public String compareMessage(Message inMessage, Message localMessage) {
        StringJoiner outJoiner = new StringJoiner("  ");
        List<Map<String, Object>> inItems = inMessage.getItems();
        List<Map<String, Object>> localItems = localMessage.getItems();

        for (Map<String, Object> localItem : localItems) {
            for (Map.Entry<String, Object> localEntry : localItem.entrySet()) {
                String key = localEntry.getKey();
                String val = (String)localEntry.getValue();
                boolean constant = false;
                String inVal = null;
                for (Map<String, Object> inItem : inItems) {
                    inVal = MapUtils.getString(inItem, key);
                    constant = inItem.containsKey(key) && (StringUtils.isEmpty(val) || StringUtils.isNotEmpty(inVal));
                    if (constant) {
                        break;
                    }
                }
                if (!constant) {
                    outJoiner.add(key + "不匹配，值为【" + inVal + "】");
                }
            }
        }
        return outJoiner.toString();
    }

    public void fileList(File file, String parentName, int level) {
        if (file.isFile()) {
            parseXmlFile(file);
            return;
        }
        File[] files = file.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            return;
        }
        for (File f : files) {
            if (f.isFile() && !"xml".equals(FileUtil.extName(f))) {
                continue;
            }
            String name = f.getName();
            String pname = StringUtils.isEmpty(parentName) ? name : parentName;
            String nextNname = StringUtils.isEmpty(parentName) ? name : parentName + "_" + name;
            List<String> childList = null;
            switch (level) {
                case 0:
                    childList = STATION_LIST;
                    break;
                case 1:
                    childList = PLATFORM_MAP.computeIfAbsent(pname, k -> new ArrayList<>());
                    break;
                case 2:
                    childList = PROTOCOL_MAP.computeIfAbsent(pname, k -> new ArrayList<>());
                    break;
                case 3:
                    childList = MESSAGE_MAP.computeIfAbsent(pname, k -> new ArrayList<>());
                    name = f.getAbsolutePath();
                    break;
                default:
                    break;
            }
            if (childList != null) {
                if (StringUtils.isNotEmpty(name)) {
                    childList.add(name);
                }
                fileList(f, nextNname, level + 1);
            }
        }
    }

    public void parseXmlFile(File file) {
        try {
            if (!"xml".equals(FileUtil.extName(file))) {
                return;
            }
            Message message = loadMessage(file);
            String type = message.getType() + "_" + message.getCommand();
            int retFlag = StringUtils.contains(file.getName(), "request") ? 0 : 1;

            String protocol = PathUtil.subPath(file.toPath(), -4, -2).toString();
            protocol = StringUtils.replace(protocol, File.separator, "_");

            String cmd = protocol + "_" + type + "_" + retFlag;
            COMMAND_MESSAGE_MAP.computeIfAbsent(cmd, k -> new TreeSet<>()).add(file.getAbsolutePath());

        } catch (Exception e) {
            log.error("解析xml文件失败: {}", file.getAbsolutePath(), e);
        }
    }

    public Message loadMessage(String file) throws DocumentException {
        return loadMessage(new File(file));
    }

    public Message loadMessage(File file) throws DocumentException {
        if (!file.exists()) {
            throw new RuntimeException("报文文件不存在，请检查报文文件！！！");
        }
        String xmlFile = FileUtil.readString(file, StandardCharsets.UTF_8);

        return XmlToMessageUtil.decode(xmlFile);
    }

    public void setServerReceiveCode(InboundMessage inboundMessage) {

        try {
            String xml = new String(inboundMessage.getPacket().getPayload(), StandardCharsets.UTF_8);

            Message message = XmlToMessageUtil.decode(xml);
            if ("251".equals(message.getType()) && "1".equals(message.getCommand())) {
                serverService.setReceiveCode(message.getSendCode());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    public void autoReply(boolean flag) {
        autoReply = flag;
    }

    public boolean scheduleHeart(Message message) {
        if ("251".equals(message.getType()) && "4".equals(message.getCommand())) {
            List<Map<String, Object>> items = message.getItems();
            if (CollectionUtils.isNotEmpty(items)) {
                long heartBeatInterval = 0L;
                for (Map<String, Object> item : items) {
                    Object interval = item.get("heart_beat_interval");
                    if (interval != null) {
                        heartBeatInterval = NumberUtils.toLong((String)interval);
                        break;
                    }
                }

                if (heartBeatInterval > 0L) {
                    log.info("增加心跳定时器: {}", heartBeatInterval);
                    HeartBeatThead.addThread(clientService, heartBeatInterval);
                    return true;
                }
            }
        }
        return false;
    }
}
