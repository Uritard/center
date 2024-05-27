/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.netty;

import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.commons.logs.SpringBeanUtils;
import com.yjh.accesstcp.module.device.callback.CallbackHandlerStrategyFactory;
import com.yjh.accesstcp.module.device.callback.handler.RegisterHandler;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.thread.RegisterManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/7/25
 * @since [产品/模块版本] （可选）
 */
public interface TCPClientHandler {
    Logger log = LoggerFactory.getLogger(TCPClientHandler.class);

    Map<SocketAddress, TCPClientHandler> TCPClientHandlerHashMap = new HashMap<>();

    /**
     * 发送方标识
     * @return 发送方标识
     */
    String getCruise();

    /**
     * 接收方标识
     * @return 接收方标识
     */
    String getServer();
    /**
     * 报文根目录
     * @return 报文根目录
     */
    String getRootName();
    /**
     * 会话序列号
     * @return 会话序列号
     */
    Long getSessionId();

    /**
     * 通信通道
     * @return 通信通道
     */
    ChannelHandlerContext getChannel();
    /**
     * 是否连接
     * @return boolean
     */
    boolean isConnected();

    /**
     * 根据地址获取 clientHandler
     * @param remoteAddress 地址
     * @return clientHandler
     */
    static TCPClientHandler getTcpClientHandlerHashMap(SocketAddress remoteAddress) {
        return TCPClientHandlerHashMap.get(remoteAddress);
    }

    /**
     * 注册
     */
    default void sendRegister() {
        // 构造注册消息
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode(getCruise())
                .setReceiveCode(getServer())
                .setType("251")
                .setCommand("1");
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel, getRootName());
        long sendSessionId = getSessionId();
        byte[] send = PlatformPacketUtil.createPacket(sendSessionId, 0L, true, xml);
        RegisterHandler registerHandler = SpringBeanUtils.getBean(RegisterHandler.class);
        assert registerHandler != null;
        registerHandler.createCallback(getRootName() + sendSessionId);
        send(send, xmlBaseModel.getReceiveCode());
    }

    /**
     * 发送带xml的报文
     *
     * @param xmlBaseModel     xml
     * @param receiveSessionId 返回序列号
     * @param isSend           是否发送
     */
    default void send(XMLBaseModel xmlBaseModel, long receiveSessionId, boolean isSend) {
        xmlBaseModel.setSendCode(getCruise()).setReceiveCode(getServer());
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel, getRootName());
        long sendSessionId = getSessionId();
        byte[] send = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, isSend, xml);
        send(send, xmlBaseModel.getReceiveCode());
        log.info("发送给上级系统的消息：{}\nsendSessionId:{}, receiveSessionId:{}", xml, sendSessionId, receiveSessionId);
    }

    /**
     * 发送带xml的报文
     *
     * @param xmlBaseModel     xml
     * @param receiveSessionId 返回序列号
     * @param isSend           是否发送
     */
    default void send(XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId, boolean isSend) {
        xmlBaseModel.setSendCode(getCruise()).setReceiveCode(getServer());
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel, getRootName());
        byte[] send = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, isSend, xml);
        send(send, xmlBaseModel.getReceiveCode());
        log.info("发送给上级系统的消息：{}\nsendSessionId:{}, receiveSessionId:{}", xml, sendSessionId, receiveSessionId);
    }

    /**
     * 返回 2513
     *
     * @param code             状态码
     * @param receiveSessionId 返回序列号
     */
    default void normalResponse(String code, long receiveSessionId) {
        XMLBaseModel xmlBaseModel =
                new XMLBaseModel().setType("251").setCommand("3").setCode(code).setSendCode(getCruise()).setReceiveCode(getServer());
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel, getRootName());
        long sendSessionId = getSessionId();
        byte[] send = PlatformPacketUtil.createPacket(sendSessionId, receiveSessionId, false, xml);
        send(send, xmlBaseModel.getReceiveCode());
        log.info("发送给上级系统的消息：{}\nsendSessionId:{}, receiveSessionId:{}", xml, sendSessionId, receiveSessionId);
    }


    /**
     *
     * @param bytes 数据
     * @param code 状态码
     */
    default void send(byte[] bytes, String code) {
        if (!isConnected()) {
            throw new RuntimeException("服务未连接，无法发送报文" + code);
        }
        ChannelHandlerContext context = getChannel();
        ByteBuf byteBuf = context.alloc().buffer();
        byteBuf.writeBytes(bytes);
        context.channel().writeAndFlush(byteBuf);
        log.info("command Send To system {} Success", code);
    }

}
