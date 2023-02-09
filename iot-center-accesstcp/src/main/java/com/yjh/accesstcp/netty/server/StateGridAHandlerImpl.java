/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.netty.server;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.AnalysisUnionTaskFileService;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.netty.entiy.Message;
import com.yjh.accesstcp.thread.RegisterManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/7/25
 * @since [产品/模块版本] （可选）
 */
public class StateGridAHandlerImpl extends SimpleChannelInboundHandler<Message> implements TCPClientHandler {

    private RedisTemplate redisTemplate;
    private SendToUpSystemServices sendToUpSystemServices;
    private String server;
    private String cruise;
    private RegisterManager registerManager;
    private ChannelHandlerContext ctx;
    private AnalysisUnionTaskFileService analysisUnionTaskFileService;

    public StateGridAHandlerImpl(RedisTemplate redisTemplate,SendToUpSystemServices sendToUpSystemServices,AnalysisUnionTaskFileService analysisUnionTaskFileService,String server,String cruise, RegisterManager registerManager) {
        this.redisTemplate = redisTemplate;
        this.sendToUpSystemServices = sendToUpSystemServices;
        this.analysisUnionTaskFileService = analysisUnionTaskFileService;
        this.server = server;
        this.cruise =cruise;
        this.registerManager =registerManager;

    }
    private boolean isThreadStart = true;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
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
            MessageThread.doProcessMessageSync(xmlRes, sendSessionId, this, sendToUpSystemServices, analysisUnionTaskFileService, redisTemplate, registerManager);
            // doProcessMessage(ctx, xmlRes, sendSessionId, receiveSessionId);
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
        isThreadStart = false;
        String remoteAdds = ctx.channel().remoteAddress().toString();
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));
        TCPClientHandlerHashMap.remove(remotePort);
        log.error("服务端主动断开连接！");
        log.info("analysisClientHandlerHashMap: " + TCPClientHandlerHashMap);
        String serverUrl = remoteAdds.substring(0, remoteAdds.indexOf(":"));
        log.info("serverUrl: " + serverUrl.substring(1));
        InetSocketAddress remoteAddress = new InetSocketAddress(serverUrl.substring(1), remotePort);
        //使用过程中断线重连
        if (Objects.nonNull(Constant.bootstrapHashMap.get(1))) {
            doConnect(remoteAddress, Constant.bootstrapHashMap.get(1));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel在线处理，都会触发这个方法

        this.ctx = ctx;
        sendRegister();//发送注册
        isThreadStart = true;

        String remoteAdds = ctx.channel().remoteAddress().toString();
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));
        if (TCPClientHandlerHashMap.get(remotePort) == null) {
            TCPClientHandlerHashMap.put(remotePort, this);
        }
        log.info("客户端注册成功: " + ctx.channel().remoteAddress());
        log.info("客户端注册成功: " + remoteAdds);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exceptionCaught:" + cause.toString());
        if (cause.toString().equals("java.io.IOException: 远程主机强迫关闭了一个现有的连接。") || cause.toString().equals("java.io.IOException: Connection reset by peer")) {
            log.info("ExceptionCaught: Client Disconnect The Connection.");
            ctx.close().sync();
            ctx.flush();
        }

    }

    @Override
    public boolean getIsThreadStart() {
        return isThreadStart;
    }

    @Override
    public String getCruise() {
        return cruise;
    }

    @Override
    public String getServer() {
        return server;
    }

    @Override
    public ChannelHandlerContext getChannel() {
        return ctx;
    }
}
