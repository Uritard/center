package com.yjh.accessudp.netty.server;

import com.sun.xml.internal.bind.v2.TODO;
import com.yjh.accessudp.common.utils.ByteUtil;
import com.yjh.accessudp.module.device.service.SysLogsService;
import com.yjh.accessudp.thread.TaskExecutePool;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;

import io.netty.channel.socket.DatagramPacket;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class UDPServerHandler extends SimpleChannelInboundHandler<DatagramPacket> {

    private SysLogsService sysLogsService;
    private RedisTemplate redisTemplate;
    public UDPServerHandler(SysLogsService sysLogsService, RedisTemplate redisTemplate) {
        this.sysLogsService = sysLogsService;
        this.redisTemplate = redisTemplate;
    }
    private boolean isThreadStart = true;
    public boolean getIsThreadStart() { return isThreadStart; }

    //场站号
    private String strChannelID = "TT";

    private byte[] bufBytes = new byte[1024 * 512];
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
        log.info("channelInactive----->" + ctx);
        Channel channel = ctx.channel();
        if (channel.id() != null) {
            log.info("id: " + channel.id() + ", strChannelID: " + strChannelID + " left," + "Onlinesize: ");
        }
        try {
            ctx.close().sync();
            ctx.flush();
            super.channelInactive(ctx);
        } catch (Exception e) {
            log.error("clientDisconnect: " + e.getMessage());
        }
        log.info("mapsAfterRemoved: ");
        //1.判断是否为注册连接，是注册连接带strChannelID，不是则是空,无须修改状态 2.可以改为若strChannelID为空，则不可注册
        if (strChannelID == null || strChannelID.equals("")) {
            log.info("strChannelID is null, No need to modify the device status");
        } else {
            strChannelID = strChannelID.replace("\0", "");
            Object strid2 = redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", strChannelID)).get("deviceId");
            if (strid2 != null) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date dataTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(df.format(new Date()));
                log.info("offlineClientId:" + strid2 + "  channel.isActive(): " + channel.isActive());
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel在线处理，都会触发这个方法
        log.info("channelActive----->" + ctx);
        this.ctx = ctx;
        log.info("mapsAfterAdded: ");
        log.info("id: " + ctx.channel().id() + " connected," + "Onlinesize: ");
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

//    @Override
//    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        ByteBuf byteBuf = (ByteBuf) msg;
//        byte[] bytes = new byte[byteBuf.readableBytes()];
//        byteBuf.readBytes(bytes);
//        log.info("bytes: "+new String(bytes).replace("\0", ""));
//        log.info("online.. " + "remoteAddress: " + ctx.channel().remoteAddress());
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date dataTime=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(df.format(new Date()));
//        DataDealThread dataDealThread = new DataDealThread(this, true);
//        Thread thread = new Thread(dataDealThread);
//        thread.setDaemon(true);
//        thread.start();
//        ReferenceCountUtil.release(byteBuf);
//    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, DatagramPacket datagramPacket) throws Exception {
        // 解析数据包
        String msgString = datagramPacket.content().toString(CharsetUtil.UTF_8);
        System.out.println(" 发来的消息：" + msgString);
    }

    private void send(ChannelHandlerContext ctx, byte[] bytes) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(bytes);
        StringBuilder Str = new StringBuilder();
        for (byte byteitem : bytes) {
            Str.append(String.format("%02x ", byteitem));
        }
        log.info("commandSend:" + Str + " : " + strChannelID);
        ctx.pipeline().writeAndFlush(byteBuf);
        log.info("commandSendSuccess");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }

    public void ProcSend() {
        //            定时读取文件内容
        log.info("定时读取。。。");
    }

}