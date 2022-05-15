package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static com.yjh.accessrobot.common.Constant.*;
import static com.yjh.accessrobot.common.Constant.robotRemoveCounts;

/**
 * 〈功能详细描述〉
 *
 * @author fei23
 * @date 2022-5-8
 * @see [相关类/方法]（可选）
 * @since [产品/模块版本] （可选）
 */
public interface RobotServerHandler {
    Logger log = LoggerFactory.getLogger(RobotServerHandler.class);

    Map<String, Object> channelPacket = new ConcurrentHashMap<>();
    Map<String, Object> robotResultMap = new ConcurrentHashMap<>();

    void heartBeatSuccessAfter(ChannelHandlerContext ctx, String robotCode, long sendSessionId,
            Map<String, String> robotStatusMap);

    void heartBeatFailAfter(String robotCode, Map<String, String> robotStatusMap);

    static Map<String, Object> getRobotResultMap() {
        return robotResultMap;
    }

    /**
     * 通过文件路径解析XML
     *
     * @param filePathAndName 文件路径
     * @return XMLBaseModel
     */
    public static XMLBaseModel getXmlMessage(String filePathAndName) throws DocumentException {
        SAXReader reader = new SAXReader();
        Document document = reader.read(new File(filePathAndName));
        return PlatformXMLUtil.readStringXmlOut(document);
    }

    /**
     * 快速创建command=3 的消息体
     *
     * @param flag     接收消息成功与否标识
     * @param sendCode 接收方唯一标识
     * @return XMLBaseModel
     */
    static XMLBaseModel sendMessageForCommandThree(boolean flag, String sendCode) {
        XMLBaseModel xmlBaseModelEmpty = new XMLBaseModel();
        xmlBaseModelEmpty.setCommand("3");
        xmlBaseModelEmpty.setTime(DateTimeUtil.getDateTimeString(false));
        xmlBaseModelEmpty.setType("251");
        xmlBaseModelEmpty.setCode("200");
//        xmlBaseModelEmpty.setCode(flag ? "200" : "500");
        xmlBaseModelEmpty.setSendCode(Constant.sendCode);
        xmlBaseModelEmpty.setReceiveCode(sendCode);
        return xmlBaseModelEmpty;
    }


    /**
     * 发送消息
     *
     * @param bytes     发送的内容
     * @param robotCode 机器人唯一标识
     * @return void
     */
    static void send(byte[] bytes, String robotCode) {
        log.info("+++++++++++++++++robotCode:{}发送消息+++++++++++++++++", robotCode);
        ChannelHandlerContext context = getChannelHandlerContextByRobot(robotCode);
        if (null != context) {
            ByteBuf byteBuf = context.alloc().buffer();
            byteBuf.writeBytes(bytes);
            context.pipeline().writeAndFlush(byteBuf);
            log.info("command Send To Robot {} Success", robotCode);
            if (byteBuf.refCnt() >= 1) {
                ReferenceCountUtil.release(context);
            }
        } else {
            log.error("robotCode:{},通道为空,发送指令失败", robotCode);
        }
    }


    /**
     * 获取ChannelHandlerContext对象
     *
     * @param robotCode 机器人唯一标识
     * @return ChannelHandlerContext
     */
    static ChannelHandlerContext getChannelHandlerContextByRobot(String robotCode) {
        String channelId = robotChannels.getOrDefault(robotCode, "");
        if (StringUtils.isNotEmpty(channelId)) {
            log.info("robotCode:{}获取会话通道channelId:{}", robotCode, channelId);
            return maps.getOrDefault(channelId, null);
        }
        return null;
    }

    /**
     * 和客户端断开连接
     *
     * @param robotCode 机器人唯一标识
     * @return void
     */
    public static void removeLink(String robotCode) {
        log.info("+++++++++++++++++robotCode:{}断连开始+++++++++++++++++", robotCode);
        if (StringUtils.isNotEmpty(robotCode)) {
            ChannelHandlerContext context = RobotServerHandler.getChannelHandlerContextByRobot(robotCode);

            if (Objects.nonNull(context)) {
                ChannelId channelId = context.channel().id();
                log.info("准备断连的是:{}==", channelId);
                Constant.robotThreadFlag.put(robotCode, false);

                if (Objects.nonNull(channelId)) {
                    maps.remove(channelId.toString());
                    robotChannels.remove(robotCode);
                    log.info(
                            "id: " + channelId + ", robotCode: " + robotCode + " left," + "onlineSize: " + maps.size());

                    try {
                        context.close().sync();
                        context.flush();
                    } catch (Exception e) {
                        Constant.robotThreadFlag.put(robotCode, false);
                        log.error("clientDisconnect: " + e.getMessage());
                    }

                    log.info("mapsAfterRemoved: " + maps);
                    log.info("robotChannelsAfterRemoved: " + robotChannels);
                    log.info("channel.isActive(): " + context.channel().isActive());
                    Constant.robotRegisterFlag.put(robotCode, false);
                }
            } else {
                log.error("robotCode:{},通道为空,发送指令失败", robotCode);
            }
            robotRegisterCounts.remove(robotCode);
            robotRemoveCounts.remove(robotCode);
        }
    }
}
