package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.EdgeEnum;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.Message;
import com.yjh.accessrobot.netty.handler.MessageHandlerStrategy;
import com.yjh.accessrobot.netty.handler.MessageHandlerStrategyFactory;
import com.yjh.accessrobot.threadpool.ThreadPoolUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.collections4.CollectionUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.yjh.accessrobot.common.Constant.*;

/**
 * 国网 A 接口的业务处理
 *
 * @author fei23
 * @date 2022-5-8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public class StateGridAHandlerImpl extends SimpleChannelInboundHandler<Message> implements RobotServerHandler {
    private static final Logger log = LoggerFactory.getLogger(StateGridAHandlerImpl.class);

    private RobotService robotService;
    private RedisTemplate redisTemplate;
    private ChannelHandlerContext ctx;

    public void setRobotService(RobotService robotService) {
        this.robotService = robotService;
    }

    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        this.ctx = ctx;

        Document document = null;
        long sendSessionId = msg.getSendSessionId();
        long receiveSessionId = msg.getReceiveSessionId();
        try {
            String content = new String(msg.getContent(), StandardCharsets.UTF_8);
            log.info("准备解析的xml=={}\nsendSessionId:{}, receiveSessionId:{}", content, sendSessionId, receiveSessionId);
            document = DocumentHelper.parseText(content);
        } catch (DocumentException e) {
            log.error("parse xml error", e);
        }
        XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);
        if (xmlRes.getSendCode() == null) {
            log.info("客户端 {} 与服务端连接可能断了，等待重连.....", ctx.channel().remoteAddress());
        } else {
            ThreadPoolUtil.PATROL_POOL.addThread(() -> {
                doProcessMessage(ctx, xmlRes, sendSessionId, receiveSessionId);
            });
            log.info("+++++++++++++++++解包完成+++++++++++++++++");
        }

    }

    /**
     * 分析解析后的xml,进行响应处理
     *
     * @param ctx              通道
     * @param xmlBaseModel     xml格式的内容
     * @param sendSessionId    发送会话序列号
     * @param receiveSessionId 接收会话序列号
     * @return void
     */
    private void doProcessMessage(ChannelHandlerContext ctx, XMLBaseModel xmlBaseModel, long sendSessionId,
            long receiveSessionId) {
        log.info("++++++此时的robotChannels:{}", Constant.robotChannels);
        try {
            String handlerType;
            String resultType = "251";
            String type = xmlBaseModel.getType();
            if (Objects.equals(resultType, type)) {
                String command = xmlBaseModel.getCommand();
                //判断本节点是巡视主机下级为边缘节点
                String edgeLevel = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeLevel", "content"));
                boolean isEdge = robotService.selectByRegionCodeAndState(xmlBaseModel.getSendCode(), 1);
                boolean is102 = CollectionUtils.isNotEmpty(xmlBaseModel.getItems()) && xmlBaseModel.getItems().get(0).containsKey("error_code");
                if (EdgeEnum.REGION_NODE.getCode().equals(edgeLevel) && isEdge && !is102) {
                    if ("3".equals(command)){
                        command = "4";
                    }else if ("4".equals(command)){
                        command = "3";
                    }
                }
                handlerType = type + command;
            } else {
                handlerType = type;
            }
            MessageHandlerStrategy messageHandlerStrategy;
            // 63,64命令类型 私有协议与220kv规约冲突
            if ("63".equals(type) || "64".equals(type)) {
                boolean isSubSystem = robotService.isSubSystem(xmlBaseModel.getSendCode());
                messageHandlerStrategy = buildMessageHandlerStrategy(type,isSubSystem);
            } else {
                messageHandlerStrategy = MessageHandlerStrategyFactory.getStrategyType(handlerType);
            }
            if (Optional.ofNullable(messageHandlerStrategy).isPresent()) {
                messageHandlerStrategy.handler(ctx, this, xmlBaseModel, sendSessionId, receiveSessionId);
            }
        } catch (Exception e) {
            log.error("处理机器人响应消息错误:", e);
            String responseMsgXmlString = PlatformXMLUtil.generateXml(
                    RobotServerHandler.sendMessageForCommandThree(false, xmlBaseModel.getSendCode()));
            byte[] responseMsgProtocol = PlatformPacketUtil
                    .createPacket(Constant.sendSessionId, sendSessionId, false, responseMsgXmlString);
            RobotServerHandler.send(responseMsgProtocol, xmlBaseModel.getSendCode());
            log.info("巡视主机给机器人{}响应了", xmlBaseModel.getSendCode());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // channel在线处理，都会触发这个方法
        log.info("channelActive----->" + ctx);
        log.info("Client " + ctx.channel().remoteAddress() + " connected");
        this.ctx = ctx;
        maps.put(ctx.channel().id().toString(), ctx);
        log.info("mapsAfterAdded: " + maps);
        log.info("id: " + ctx.channel().id() + " connected," + "Onlinesize: " + maps.size());
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
        log.info("channelInactive----->" + ctx);
        Channel channel = ctx.channel();
        ChannelId id = channel.id();
        if (id != null) {
            maps.remove(id.toString());
            log.info("mapsAfterRemoved: " + maps);
        }

        try {
            ctx.close().sync();
            ctx.flush();
            super.channelInactive(ctx);
        } catch (Exception e) {
            log.error("clientDisconnect: " + e.getMessage());
        }
        synchronized (ROBOT) {
            for (Map.Entry<String, String> vo : Constant.robotChannels.entrySet()) {
                log.info("当前的robotChannels的key为" + vo.getKey());
                String robotCode = vo.getKey();
                String channelId = Constant.robotChannels.get(robotCode);

                if (channelId.equals(String.valueOf(id))) {
                    robotService.updateRobotInfo(robotCode, "离线");
                    Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
                    // abnormal
                    robotStatusMap.put("value", "1");
                    // Update Robot Network Status
                    redisTemplate.opsForHash().putAll("RobotStatus:" + robotCode + ":2", robotStatusMap);
                    Constant.robotChannels.remove(robotCode);
                    Constant.robotThreadFlag.put(robotCode, false);
                    Constant.robotRegisterFlag.put(robotCode, false);
                    log.info("id: " + channel.id() + ", robotCode: " + robotCode + " left," + "onlineSize: " + maps.size());
                }
            }
        }

        log.info("channel.isActive(): " + channel.isActive());
        log.info("此时的packet=====" + channelPacket.get(channel.id().toString()));
        channelPacket.put(channel.id().toString(), "");

    }


    /**
     * 成功收到心跳指令,发送响应并更新机器人状态
     *
     * @param ctx            通道
     * @param robotCode      机器人唯一标识
     * @param sendSessionId  发送会话序列号
     * @param robotStatusMap 机器人在线状态
     * @return void
     */
    @Override
    public void heartBeatSuccessAfter(ChannelHandlerContext ctx, String robotCode, long sendSessionId,
            Map<String, String> robotStatusMap) {
        String heartXmlString = PlatformXMLUtil.generateXml(
                RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] heartProtocol = PlatformPacketUtil
                .createPacket(Constant.sendSessionId, sendSessionId, false, heartXmlString);
        RobotServerHandler.send(heartProtocol, robotCode);
        log.info("maps:{}", Constant.maps);
        log.info("robotChannels:{}", Constant.robotChannels);
        // 为了等待客户端和服务端连接稳定,收到三次以上再修改
        if (Constant.robotChannels.containsKey(robotCode)) {
            log.info("连接稳定且注册成功,客户端:{}正常", robotCode);
            robotService.updateRobotInfo(robotCode, "在线");
            // normal
            robotStatusMap.put("value", "0");
            // Update Robot Network Status
            redisTemplate.opsForHash().putAll("RobotStatus:" + robotCode + ":2", robotStatusMap);
        }
    }

    /**
     * 没有收到心跳指令,removeLink && updateRobotStatus
     *
     * @param robotCode      机器人唯一标识
     * @param robotStatusMap 机器人在线状态
     * @return void
     */
    @Override
    public void heartBeatFailAfter(String robotCode, Map<String, String> robotStatusMap) {
        RobotServerHandler.removeLink(robotCode);
        robotService.updateRobotInfo(robotCode, "离线");
        // abnormal
        robotStatusMap.put("value", "1");
        // Update Robot Network Status
        redisTemplate.opsForHash().putAll("RobotStatus:" + robotCode + ":2", robotStatusMap);
    }

}
