package com.yjh.accessvqd.netty.server;

import com.yjh.accessvqd.module.diagnose.dao.TDiagnosePlanDao;
import com.yjh.accessvqd.module.diagnose.service.ChanResultService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.yjh.accessvqd.common.Constant.maps;

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class VqdServerHandler extends ChannelInboundHandlerAdapter {

    private ChanResultService chanResultService;
    private TDiagnosePlanDao tDiagnosePlanDao;

    public ChanResultService getChanResultService() { return chanResultService; }

    public void setChanResultService(ChanResultService chanResultService) { this.chanResultService = chanResultService; }

    public TDiagnosePlanDao gettDiagnosePlanDao() { return tDiagnosePlanDao; }

    public void settDiagnosePlanDao(TDiagnosePlanDao tDiagnosePlanDao) { this.tDiagnosePlanDao = tDiagnosePlanDao; }

    private RedisTemplate redisTemplate;

    public void setRedisTemplate(RedisTemplate redisTemplate) { this.redisTemplate = redisTemplate; }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    private ChannelHandlerContext ctx;

    //遥调遥控
    private static Map<Object, VqdServerHandler> vqdServerHandlerMap = new HashMap<>();

    public static Map<Object, VqdServerHandler> getVqdServerHandlerMap() { return vqdServerHandlerMap; }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception { log.info("Connection is Added."); }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //TO DO: channel 和 ChannelPipeline 是否需要关闭？？
        ctx.close().sync();
        ctx.flush();
        log.info("Connection is Removed."+ctx.channel().id());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
        log.info("channelInactive----->" + ctx);
        Channel channel = ctx.channel();
        if (channel.id() != null) {
            maps.remove(channel.id().toString());
            log.info("id: " + channel.id() + " left," + "Onlinesize: " + maps.size());
        }
        try {
            ctx.close().sync();
            ctx.flush();
            super.channelInactive(ctx);
        } catch (Exception e) {
            log.error("vqdClientDisconnect: " + e.getMessage());
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //channel在线处理，都会触发这个方法
        log.info("channelActive----->" + ctx);
        this.ctx = ctx;
        maps.put(ctx.channel().id().toString(), ctx);
        log.info("mapsAfterAdded: " + maps);
        log.info("id: " + ctx.channel().id() + " connected," + "Onlinesize: " + maps.size());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("exceptionCaught:" + cause.toString());
        if (cause.toString().equals("java.io.IOException: 远程主机强迫关闭了一个现有的连接。") || cause.toString().equals("java.io.IOException: Connection reset by peer")) {
            log.info("ExceptionCaught: Client Disconnect The Connection.");
            ctx.close().sync();
            ctx.flush();
        }
//        ctx.close().sync();
//        ctx.flush();
    }
    @Override
    public  void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收机器人发送的指令
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);

        StringBuilder Str = new StringBuilder();
        for (byte byteItem : bytes) {
            Str.append(String.format("%02x ", byteItem));
        }
        log.info("vqd发送的的指令是<start>" + Str + "<end>");
    }

    /*
    * 3、2、1走你
    * */
    private void send(ChannelHandlerContext ctx, byte[] bytes) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(bytes);
        StringBuilder sendToVadStr = new StringBuilder();
        for (byte byteItem : bytes) {
            sendToVadStr.append(String.format("%02x ", byteItem));
        }
        log.info("commandSendToVqd:" + sendToVadStr);
        ctx.pipeline().writeAndFlush(byteBuf);
        log.info("commandSendToVqdSuccess");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }
}