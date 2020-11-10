package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.module.device.service.SysLogsService;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

import static com.yjh.accessrobot.common.Constant.*;

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class RobotServerHandler extends ChannelInboundHandlerAdapter {

    public RobotServerHandler() {
        m_wSendSerial = 0;
        m_wRecvSerial = 0;
        hisT3 = System.currentTimeMillis();
        T3 = 20000;
    }

    private SysLogsService sysLogsService;
    public void setSysLogsService(SysLogsService sysLogsService) {
        this.sysLogsService = sysLogsService;
    }

    private RedisTemplate redisTemplate;
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    //场站号
    private String strRobotId = "TT";
    private boolean isThreadStart;
    public boolean getIsThreadStart() { return isThreadStart; }
    // 发送序列号
    private short m_wSendSerial;
    // 接收序列号
    private short m_wRecvSerial;
    private long hisT3;
    private long T3;

    private byte[] bufBytes = new byte[1024];
    private ChannelHandlerContext ctx;
    //遥调遥控
    private static Map<Object, RobotServerHandler> robotServerHandlerMap = new HashMap<>();
    public static Map<Object, RobotServerHandler> getRobotServerHandlerMap() { return robotServerHandlerMap; }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        log.info("Connection is Added.");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        //TO DO: channel 和 ChannelPipeline 是否需要关闭？？
        ctx.close().sync();
        ctx.flush();
        log.info("Connection is Removed.");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
        log.info("channelInactive----->" + ctx);
        isThreadStart = false;
        Channel channel = ctx.channel();
        if (channel.id() != null) {
            maps.remove(channel.id().toString());
            log.info("id: " + channel.id() + ", strRobotId: " + strRobotId + " left," + "Onlinesize: " + maps.size());
        }
        try {
            ctx.close().sync();
            ctx.flush();
            super.channelInactive(ctx);
        } catch (Exception e) {
            isThreadStart = false;
            log.error("clientDisconnect: " + e.getMessage());
        }
        log.info("mapsAfterRemoved: " + maps);
        //1.判断是否为注册连接，是注册连接带strChannelID，不是则是空,无须修改状态 2.可以改为若strChannelID为空，则不可注册
        if (strRobotId == null || strRobotId.equals("")) {
            log.info("strChannelID is null, No need to modify the device status");
        } else {
            robotServerHandlerMap.remove(strRobotId);
            strRobotId = strRobotId.replace("\0", "");
            Object strid2 = redisTemplate.opsForHash().entries(String.format("dmp_device_base:104_%s", strRobotId)).get("deviceId");
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
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        handlerData();
        ReferenceCountUtil.release(byteBuf);
    }

    private void handlerData() { }

    void ProcSend() {
        // 心跳报文(客户端,服务端均可发起测试);
        if (istimeout(HEARTBEAT, hisT3, false)) {
            SendHeartBeat();
            hisT3 = System.currentTimeMillis();
        }

    }

    private void send(ChannelHandlerContext ctx, byte[] bytes) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(bytes);
        StringBuilder Str = new StringBuilder();
        for (byte byteitem : bytes) {
            Str.append(String.format("%02x ", byteitem));
        }
        log.info("commandSendToRobot:" + Str + " : " + strRobotId);
        ctx.pipeline().writeAndFlush(byteBuf);
        log.info("commandSendToRobotSuccess");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }

    private boolean istimeout(byte type, long value, boolean set) {
        if (type == HEARTBEAT) {
            //log.info("commandSend:68 04 43 00 00 00 "+System.currentTimeMillis() + ":" + value +":" +(System.currentTimeMillis() - value));
            if ((System.currentTimeMillis() - value) > T3) {
                if (set) hisT3 = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        } else return false;
    }

    public void SendHeartBeat () {

    }

}