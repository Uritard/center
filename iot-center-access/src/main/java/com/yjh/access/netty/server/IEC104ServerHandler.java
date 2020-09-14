package com.yjh.access.netty.server;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class IEC104ServerHandler extends ChannelInboundHandlerAdapter {

    public IEC104ServerHandler() {
        bufdateLen = 0;
        m_nWaitSTARTDT = 0;
        m_nWaitTESTFR = 0;
        m_bIsWorking = false;
        m_bIsStop = true;
        m_bFirstSetClock = false;
        //m_bFirstBeckOn_Station = true;
        m_bReSendFrmTime = false;
        m_SendRecvSerial = 0;
        m_wSendSerial = 0;
        m_wRecvSerial = 0;
        hisT0 = hisT1 = hisT2 = hisT3 = hisTALL = hisTtimeout = System.currentTimeMillis();
        T0 = 30000;
        T1 = 15000;
        T2 = 10000;
        T3 = 20000;
        TALL = 180000;
        Ttimeout = 50000;
        bT0 = false;
    }

    private RedisTemplate redisTemplate;
    public void setRedisTemplate(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private RedisTemplate redisTemplate2;
    public void setRedisTemplate2(RedisTemplate redisTemplate2) {
        this.redisTemplate2 = redisTemplate2;
    }

    //场站号
    private String strChannelID = "TT";
    private StringBuffer sourcedata = new StringBuffer();
    private boolean m_bIsWorking;
    private boolean m_bIsStop;
    // 第一次发送对时帧
    private boolean m_bFirstSetClock;
    // 重发对时帧标志
    private boolean m_bReSendFrmTime;
    private boolean m_bFirstBeckOn_Station;
    private boolean isThreadStart;
    public boolean getIsThreadStart() { return isThreadStart; }
    // 等待STARTDT报文确认,启动数据传输=-1:表示数据传输已启动
    private int m_nWaitSTARTDT;
    // 等待测试报文确认
    private int m_nWaitTESTFR;
    // 发送序列号
    private short m_wSendSerial;
    // 接收序列号
    private short m_wRecvSerial;
    //已经发送的确认帧
    private short m_SendRecvSerial;
    private boolean bT0;
    private long hisT0;
    private long hisT1;
    private long hisT2;
    private long hisT3;
    private long hisTALL;
    private long hisTtimeout;
    private long T0;
    private long T1;
    private long T2;
    private long T3;
    private long TALL;
    private long Ttimeout;

    private byte[] bufBytes = new byte[1024 * 512];
    private int bufdateLen;
    private ChannelHandlerContext ctx;
    //遥调遥控
    private static Map<Object, IEC104ServerHandler> iec104ServerHandlerMap = new HashMap<>();
    public static Map<Object, IEC104ServerHandler> getIec104ServerHandlerMap() { return iec104ServerHandlerMap; }
    //private static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    //private ByteUtil byteUtil = new ByteUtil();

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
        try {
            ctx.close().sync();
            ctx.flush();
            super.channelInactive(ctx);
        } catch (Exception e) {
            isThreadStart = false;
            log.error("clientDisconnect: " + e.getMessage());
        }
        //1.判断是否为注册连接，是注册连接带strChannelID，不是则是空,无须修改状态 2.可以改为若strChannelID为空，则不可注册
        if (strChannelID == null || strChannelID.equals("")) {
            log.info("strChannelID is null, No need to modify the device status");
        } else {
            iec104ServerHandlerMap.remove(strChannelID);
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
        ReferenceCountUtil.release(byteBuf);
    }

}