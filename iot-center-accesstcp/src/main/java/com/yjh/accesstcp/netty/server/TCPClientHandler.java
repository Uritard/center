/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.netty.server;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoop;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
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

    Map<Integer, TCPClientHandler> TCPClientHandlerHashMap = new HashMap<>();

    boolean getIsThreadStart();

    String getCruise();

    String getServer();

    ChannelHandlerContext getChannel();

    static Map<Integer, TCPClientHandler> getTCPClientHandlerHashMap() {
        return TCPClientHandlerHashMap;
    }


    //重新连接tcp服务端
    default void doConnect(InetSocketAddress remoteAddress, Bootstrap bootstrap) {
        try {
            if (bootstrap != null) {
                bootstrap.remoteAddress(remoteAddress);
                ChannelFuture f = bootstrap.connect().addListener((ChannelFuture futureListener) -> {
                    final EventLoop eventLoop = futureListener.channel().eventLoop();
                    if (!futureListener.isSuccess()) {
                        //重连
                        log.info("与服务端" + remoteAddress + "连接失败!主动尝试重连!");
                        eventLoop.schedule(() -> doConnect(remoteAddress, bootstrap), 60, TimeUnit.SECONDS);
                    }
                });
            }
        } catch (Exception e) {
            log.info("------主动连接服务端连接失败------" + e.getMessage());
        }
    }

    //注册
    default void sendRegister(){
        //构造注册消息
        //todo sendcode 要从配置文件里读取
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
            .setSendCode(getCruise())
            .setReceiveCode(getServer())
            .setType("251")
            .setCommand("1");
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel);
        byte[] send = PlatformPacketUtil.createPacket(Constant.sendSessionId.incrementAndGet(),0L,true,xml);
        send(send);
        //sendString(ctx,PlatformXMLUtil.generateXml(xmlBaseModel));
    }

    //心跳
    default void ProcSend() {
        try {
            // 心跳报文(客户端,服务端均可发起测试);
            XMLBaseModel xmlBaseModel = new XMLBaseModel().setSendCode(getCruise()).setReceiveCode(getServer()).setType("251").setCommand("2");
            String xml = PlatformXMLUtil.generateXml(xmlBaseModel);
            long sendSessionId = Constant.sendSessionId.incrementAndGet();//刷新sendSessionId
            byte[] send = PlatformPacketUtil.createPacket(sendSessionId, 0L, true, xml);
            send(send);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }

    default void send( byte[] bytes) {
        ChannelHandlerContext ctx = getChannel();
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(bytes);
        StringBuilder Str = new StringBuilder();
        for (byte byteitem : bytes) {
            Str.append(String.format("%02x ", byteitem));
        }
        log.info("commandSendToRobot:" + Str + " : " );
        ctx.pipeline().writeAndFlush(byteBuf);
        log.info("发送成功");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }

    default void sendString(ChannelHandlerContext ctx, String msg) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(msg.getBytes());
        log.info("客户端发送报文:" + msg);
        ctx.channel().writeAndFlush(byteBuf);
        log.info("客户端发送报文成功！");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }
}
