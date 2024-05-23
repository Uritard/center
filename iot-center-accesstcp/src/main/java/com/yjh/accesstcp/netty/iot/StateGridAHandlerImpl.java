/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.netty.iot;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.netty.NettyClient;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.entiy.Message;
import com.yjh.accesstcp.netty.entiy.MessageHeader;
import com.yjh.accesstcp.netty.handler.ProtocolEnum;
import com.yjh.accesstcp.thread.*;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.RequiredArgsConstructor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.stereotype.Component;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/7/25
 * @since [产品/模块版本] （可选）
 */
@Component
@ChannelHandler.Sharable
public class StateGridAHandlerImpl extends SimpleChannelInboundHandler<Message> implements TCPClientHandler {

    /**
     * 发送会话序列号
     */
    AtomicLong sessionId = new AtomicLong(0L);

    private ChannelHandlerContext ctx;

    private volatile boolean connect = false;
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        Document document = null;
        long sendSessionId = msg.getSendSessionId();
        long receiveSessionId = msg.getReceiveSessionId();
        byte sessionType = msg.getSessionType();
        try {
            String content = new String(msg.getContent(), StandardCharsets.UTF_8);
            log.info("准备解析的xml=={}\nsendSessionId:{}, receiveSessionId:{}, sessionType: {}", content, sendSessionId, receiveSessionId, sessionType);
            document = DocumentHelper.parseText(content);
        } catch (DocumentException e) {
            log.error("parse xml error", e);
        }
        XMLBaseModel xmlRes = PlatformXMLUtil.readStringXmlOut(document);
        if (xmlRes.getSendCode() == null) {
            log.info("客户端 {} 与服务端连接可能断了，等待重连.....", ctx.channel().remoteAddress());
        } else {
            MessageHeader header =
                    new MessageHeader().setSessionId(sendSessionId).setReceiveSessionId(receiveSessionId).setSessionType(sessionType);
            MessageThread.doProcessMessage(ProtocolEnum.IOT, this, xmlRes, header);
            log.info("+++++++++++++++++解包完成+++++++++++++++++");
        }

    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("Connection is Added.");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        ctx.close().sync();
        ctx.flush();
        log.info("Connection is Removed.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
        ctx.close().sync();
        ctx.flush();
        super.channelInactive(ctx);
        connect = false;
        SocketAddress remoteAdds = ctx.channel().remoteAddress();
        TCPClientHandlerHashMap.remove(remoteAdds);
        // 暂停心跳发送
        HeartBeatThead.terminate(getServer());
        // 暂停运行数据发送
        RunningThread.terminate(getServer());
        // 暂停微气象发送
        WeatherThread.terminate(getServer());
        // 暂停机巢运行数据发送
        NestRunThread.terminate(getServer());

        log.error("服务端主动断开连接！{}", remoteAdds);
        log.info("analysisClientHandlerHashMap: {}", TCPClientHandlerHashMap);
        //使用过程中断线重连
        if (Objects.nonNull(NettyClient.BOOTSTRAP_MAP.get(remoteAdds))) {
            doConnect(remoteAdds, NettyClient.BOOTSTRAP_MAP.get(remoteAdds));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel在线处理，都会触发这个方法
        this.ctx = ctx;
        connect = true;
        //发送注册
        sendRegister();

        SocketAddress remoteAdds = ctx.channel().remoteAddress();
        TCPClientHandlerHashMap.putIfAbsent(remoteAdds, this);
        log.info("客户端注册成功: {}", remoteAdds);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exceptionCaught:" + cause.toString());
        if ("java.io.IOException: 远程主机强迫关闭了一个现有的连接。".equals(cause.toString()) || "java.io.IOException: Connection reset by peer".equals(cause.toString())) {
            log.info("ExceptionCaught: Client Disconnect The Connection.");
            ctx.close().sync();
            ctx.flush();
        }

    }

    @Override
    public String getCruise() {
        return Constant.cruise();
    }

    @Override
    public String getServer() {
        return Constant.server();
    }

    @Override
    public String getRootName() {
        return "PatrolHost";
    }

    @Override
    public Long getSessionId() {
        return sessionId.incrementAndGet();
    }

    @Override
    public ChannelHandlerContext getChannel() {
        return ctx;
    }

    @Override
    public boolean isConnected() {
        return connect;
    }
}
