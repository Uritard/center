package com.yjh.accesstcp.netty.server;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.common.utils.ByteUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accesstcp.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accesstcp.commons.logs.SpringBeanUtils;
import com.yjh.accesstcp.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.thread.TaskExecutePool;
import com.yjh.accesstcp.thread.WeatherThread;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class TCPClientHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private RedisTemplate redisTemplate;
    private SendToUpSystemServices sendToUpSystemServices;

    public TCPClientHandler(RedisTemplate redisTemplate,SendToUpSystemServices sendToUpSystemServices) {
        this.redisTemplate = redisTemplate;
        this.sendToUpSystemServices = sendToUpSystemServices;

    }
    private boolean isThreadStart = true;
    public boolean getIsThreadStart() { return isThreadStart; }

    private static Map<Integer, TCPClientHandler> TCPClientHandlerHashMap = new HashMap<>();

    public static Map<Integer, TCPClientHandler> getTCPClientHandlerHashMap() {
        return TCPClientHandlerHashMap;
    }

    //场站号
    private String strChannelID = "TT";

    private byte[] bufBytes = new byte[1024];
    private int bufdateLen;
    private ChannelHandlerContext ctx;


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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        try {
            //处理接收到的数据
            DataDealThread dataDealThread=new DataDealThread(bytes,this,redisTemplate, sendToUpSystemServices);
            TaskExecutePool.getInstance().execute(dataDealThread);

        } catch (Exception e) {
            e.getMessage();
        }

        ReferenceCountUtil.release(byteBuf);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        // 解析数据包

    }

    public void send( byte[] bytes) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(bytes);
        StringBuilder Str = new StringBuilder();
        for (byte byteitem : bytes) {
            Str.append(String.format("%02x ", byteitem));
        }
        //log.info("commandSendToRobot:" + Str + " : " + strRobotCode);
        ctx.pipeline().writeAndFlush(byteBuf);
        log.info("发送成功");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }

    public void sendString(ChannelHandlerContext ctx, String msg) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(msg.getBytes());
        log.info("客户端发送报文:" + msg);
        ctx.channel().writeAndFlush(byteBuf);
        log.info("客户端发送报文成功！");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }
    //注册
    public void sendRegister(){
        //构造注册消息
        //todo sendcode 要从配置文件里读取
        XMLBaseModel xmlBaseModel = new XMLBaseModel()
                .setSendCode("Client02)")
                .setReceiveCode("Srerver02")
                .setType("251")
                .setCommand("1");
        String xml = PlatformXMLUtil.generateXml(xmlBaseModel);
        byte[] send = PlatformPacketUtil.createPacket(Constant.sendSessionId,0L,true,xml);
        send(send);
        //sendString(ctx,PlatformXMLUtil.generateXml(xmlBaseModel));
    }

    //心跳
    public void ProcSend() {
        try {
            // 心跳报文(客户端,服务端均可发起测试);
            XMLBaseModel xmlBaseModel = new XMLBaseModel()
                    .setSendCode("Client02)")
                    .setReceiveCode("Srerver02")
                    .setType("251")
                    .setCommand("2");
            String xml = PlatformXMLUtil.generateXml(xmlBaseModel);
            byte[] send = PlatformPacketUtil.createPacket(Constant.sendSessionId,0L,true,xml);
            send(send);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //重新连接tcp服务端
    private void doConnect(InetSocketAddress remoteAddress, Bootstrap bootstrap) {
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

}