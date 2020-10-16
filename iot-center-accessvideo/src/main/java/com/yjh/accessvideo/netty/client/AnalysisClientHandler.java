package com.yjh.accessvideo.netty.client;

import com.yjh.accessvideo.commons.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static com.yjh.accessvideo.common.Constant.TYPET3;

/**
 * Created by tt on 2019/7/31.
 */
public class AnalysisClientHandler extends ChannelInboundHandlerAdapter {

    private Logger log = LoggerFactory.getLogger(AnalysisClientHandler.class);

    private String gatewayName;

    private byte[] bufBytes = new byte[1024 * 512];
    private int bufdateLen;
    private long hisT3 = System.currentTimeMillis();
    private long hisTtimeout;
    private long T3 = 20000;
    public boolean isThreadStart;
    private ChannelHandlerContext ctx;

    private int changeDataNum;
    public int getChangeDataNum() { return changeDataNum; }
    public void setChangeDataNum(int changeDataNum) { this.changeDataNum = changeDataNum; }

    public AnalysisClientHandler() { }

    public AnalysisClientHandler(String gatewayName, int changeDataNum) {
        this.gatewayName = gatewayName;
        this.changeDataNum = changeDataNum;
    }

    private static Map<Object, AnalysisClientHandler> analysisClientHandlerHashMap = new HashMap<>();
    public static Map<Object, AnalysisClientHandler> getAnalysisClientHandlerHashMap() { return analysisClientHandlerHashMap; }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
        sendStartRegister(ctx);
        Thread.sleep(3000);
        this.ctx = ctx;
        isThreadStart = true;
        //启动心跳检测
        HeartBeatThread heartBeatThread = new HeartBeatThread(this, true);
        //new Thread(dataDealThread).start();
        Thread thread = new Thread(heartBeatThread);
        thread.setDaemon(true);
        thread.start();
        if (analysisClientHandlerHashMap.get(gatewayName) == null) { analysisClientHandlerHashMap.put(gatewayName, this); }
        log.info("客户端注册成功");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,服务端下线或者强制退出等任何情况都触发这个方法
        ctx.close().sync();
        ctx.flush();
        super.channelInactive(ctx);
        isThreadStart = false;
        analysisClientHandlerHashMap.remove(gatewayName);
        log.error("服务端断开连接！");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        System.arraycopy(bytes, 0, bufBytes, bufdateLen, bytes.length);
        bufdateLen = bytes.length + bufdateLen;
        handlerData();
        ReferenceCountUtil.release(byteBuf);
    }

    private void handlerData() {
        while (FrameCallBack(bufBytes, bufdateLen)) {
            if (bufBytes[1] == 0x04) {
                if (bufBytes[2]==0x43 && bufBytes[3]==0x00 && bufBytes[4]==0x00 && bufBytes[5]==0x00 ) {
                    //心跳计时
                    hisT3 = System.currentTimeMillis();
                }
                System.arraycopy(bufBytes, 6, bufBytes, 0, bufdateLen - 6);
                bufdateLen = bufdateLen - 6;
            } else {
                switch (bufBytes[6]) {
                    case 0x64:
                        //收到总召命令，发送数据
                        log.info("发送总召数据");
                        byte[] bytesYX1 = new byte[141];
                        break;
                    default:
                        log.info("未知数据类型: " + bufBytes[6]);
                }
                //粘包去除已处理数据，正常包清除缓存
                if ((bufdateLen - (bufBytes[1] + 2)) > 0) {
                    System.arraycopy(bufBytes, bufBytes[1] + 2, bufBytes, 0, bufdateLen - bufBytes[1] - 2);
                    bufdateLen -= (bufBytes[1] + 2);
                } else {
                    bufBytes = new byte[1024 * 512];
                    bufdateLen = 0;
                }
            }
        }

    }

    //拆包
    private boolean FrameCallBack(byte[] bytes, int nLength) {
        while (nLength >= 5) {
            if ( nLength==0x06 ) {
                if ( bytes[0]==0x68 && bytes[1]==0x04 ) {
                    StringBuffer Str = new StringBuffer();
                    int dataLength = ByteUtil.byte2Int(bytes[1]) + 2;
                    for (int i = 0; i < dataLength; i++) {
                        Str.append(String.format("%02x ", bufBytes[i]));
                    }
                    log.info("收到服务端消息: " + Str);
                    return true;
                } else {
                    //挪位直到第一个为68
                    if (bufdateLen > 0) {
                        System.arraycopy(bufBytes, 1, bufBytes, 0, --bufdateLen);
                    } else { bufdateLen = 0; }
                    return false;
                }
            } else {
                if ( bytes[0]==0x68 ) {
                    if (nLength < (ByteUtil.byte2Int(bytes[1]) + 2)) {
                        return false;
                    } else if (nLength == (ByteUtil.byte2Int(bytes[1]) + 2)) {
                        StringBuffer Str = new StringBuffer();
                        int dataLength = ByteUtil.byte2Int(bytes[1]) + 2;
                        for (int i = 0; i < dataLength; i++) {
                            Str.append(String.format("%02x ", bufBytes[i]));
                        }
                        log.info("收到服务端消息: " + Str);
                        return true;
                    } else if (nLength > (ByteUtil.byte2Int(bytes[1]) + 2)) {
                        StringBuilder Str = new StringBuilder();
                        for (int i = 0; i < nLength; i++) {
                            Str.append(String.format("%02x ", bufBytes[i]));
                        }
                        log.info("收到服务端长消息: " + Str);
                        return true;
                    }
                } else {
                    //挪位直到第一个为68
                    if (bufdateLen > 0) {
                        System.arraycopy(bufBytes, 1, bufBytes, 0, --bufdateLen);
                    } else { bufdateLen = 0; }
                    return false;
                }
            }
        }
        return false;
    }

    public void ProcSend() {
        try {
            // 心跳报文(客户端,服务端均可发起测试);
            if (isTimeout(TYPET3, hisT3, false)) {
                SendHeartBeat(ctx);
                hisT3 = hisTtimeout = System.currentTimeMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isTimeout(byte type, long value, boolean set) {
        if (type == TYPET3) {
            //log.info("commandSend:68 04 43 00 00 00 "+System.currentTimeMillis() + ":" + value +":" +(System.currentTimeMillis() - value));
            if ((System.currentTimeMillis() - value) > T3) {
                if (set) hisT3 = System.currentTimeMillis();
                return true;
            } else {
                return false;
            }
        } else return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage());
        ctx.close();
    }

    private void sendBytes(ChannelHandlerContext ctx, String msg) {
        ByteBuf byteBuf = ctx.alloc().buffer();
        byteBuf.writeBytes(byteBuf);
        log.info("客户端发送报文:" + msg);
        ctx.pipeline().writeAndFlush(msg);
        log.info("客户端发送报文成功！");
        if (byteBuf.refCnt() >= 1) {
            ReferenceCountUtil.release(ctx);
        }
    }

    private void sendString(ChannelHandlerContext ctx, String msg) {
        log.info("客户端发送报文:" + msg);
        ctx.channel().writeAndFlush(msg);
        log.info("客户端发送报文成功！");
    }

    private void sendStartRegister(ChannelHandlerContext ctx) {
        // 发送json字符串
        String registerMsg = "{\n\"msgType\": \"3\", \n\"msgData\": {\n\"desNode\": \"serverSocket\", \n\"srcNode\": \"clientSocket001\",\n\"registerKey\": \"yijiahe\"\n}\n}\n";
        sendString(ctx, registerMsg);
    }

    private void SendHeartBeat(ChannelHandlerContext ctx) {
        String registerMsg = "{\n\"msgType\": \"5\", \n\"msgData\": {\n\"desNode\": \"serverSocket\", \n\"srcNode\": \"clientSocket001\"\n}\n}\n";
        sendString(ctx, registerMsg);
    }

    private void sendChangeData (ChannelHandlerContext ctx) throws Exception {
        log.info("跳变数据上送量： "+changeDataNum);
        for (int num=0;num<changeDataNum;num++) {
            byte[] bytesChangeData = new byte[20];
            bytesChangeData[0] = 0x68;
            bytesChangeData[1] = 0x12;
            bytesChangeData[2] = (byte) 0xa2;
            bytesChangeData[3] = (byte) 0xfc;
            bytesChangeData[4] = (byte) 0xe8;
            bytesChangeData[5] = 0x12;
            bytesChangeData[6] = 0x0d;
            bytesChangeData[7] = 0x01;
            bytesChangeData[8] = 0x01;
            bytesChangeData[9] = 0x00;
            bytesChangeData[10] = 0x00;
            bytesChangeData[11] = 0x00;
            bytesChangeData[12] = 0x01;
            bytesChangeData[13] = 0x40;
            bytesChangeData[14] = 0x00;
            bytesChangeData[15] = ByteUtil.int2Byte(nextInt(0,15))[0];
            bytesChangeData[16] = ByteUtil.int2Byte(nextInt(0,15))[0];
            bytesChangeData[17] = ByteUtil.int2Byte(nextInt(0,15))[0];
            bytesChangeData[18] = ByteUtil.int2Byte(nextInt(0,3))[0];
            bytesChangeData[19] = 0x00;
            StringBuffer Str = new StringBuffer();
            int dataLength = ByteUtil.byte2Int(bytesChangeData[1]) + 2;
            for (int i = 0; i < dataLength; i++) {
                Str.append(String.format("%02x ", bytesChangeData[i]));
            }
            log.info("客户端发送跳变数据: " + Str);
            ByteBuf byteBuf = ctx.alloc().buffer();
            byteBuf.writeBytes(bytesChangeData);
            ctx.pipeline().writeAndFlush(byteBuf);
            if (byteBuf.refCnt() >= 1) {
                ReferenceCountUtil.release(ctx);
            }
            log.info("跳变数据发送成功！");
        }
    }

    public int nextInt(int min, int max) throws Exception {
        if (max < min) { throw new Exception("min < max"); }
        if (min == max) { return min; }
        return min + ((max - min)*new Random().nextInt());
    }


}
