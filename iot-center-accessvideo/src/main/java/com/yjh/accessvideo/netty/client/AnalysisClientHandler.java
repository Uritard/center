package com.yjh.accessvideo.netty.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.commons.utils.ByteUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static com.yjh.accessvideo.common.Constant.TYPET3;

/**
 * Created by tt on 2019/7/31.
 */
public class AnalysisClientHandler extends ChannelInboundHandlerAdapter {
    @Autowired
    private  RedisTemplate redisTemplate;
    private Logger log = LoggerFactory.getLogger(AnalysisClientHandler.class);

    private String gatewayName;

    private byte[] bufBytes = new byte[1024 * 512];
    private long hisT3 = System.currentTimeMillis();
    private long T3 = 20000;
    public boolean isThreadStart;
    private ChannelHandlerContext ctx;
    private int remotePort1;
    private int remotePort2;

    public AnalysisClientHandler(int remotePort1, int remotePort2) {
        this.remotePort1 = remotePort1;
        this.remotePort2 = remotePort2;
    }

    private static Map<Integer, AnalysisClientHandler> analysisClientHandlerHashMap = new HashMap<>();
    public static Map<Integer, AnalysisClientHandler> getAnalysisClientHandlerHashMap() { return analysisClientHandlerHashMap; }

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
        if (analysisClientHandlerHashMap.get(remotePort1) == null) { analysisClientHandlerHashMap.put(remotePort1, this); }
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
        try {
            String body = new String(bytes, "UTF-8");
            log.info("接收服务端数据:"+body);
            handlerData(body);
        } catch (Exception e) { e.getMessage(); }

        ReferenceCountUtil.release(byteBuf);
    }

    private void handlerData(String body) {
        while (body.length()>0) {
            //TODO 具体转化逻辑
            JSONObject jsonObject= JSON.parseObject(body);//全量返回结果集
            String data=jsonObject.get("Data").toString();
            JSONObject jsonObjectData=JSON.parseObject(data);//全量数据结果集

            Iterator iterator=jsonObjectData.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry entry=(Map.Entry)iterator.next();
                JSONObject jsonObjectResult=JSON.parseObject(entry.getValue().toString());//遍历每一个结果子集
                String anlyseType=jsonObjectResult.get("anlyseType").toString();
                Map anlyseResult=new HashMap();
                anlyseResult.put("taskId",jsonObjectResult.get("taskId"));
                anlyseResult.put("instanceId",jsonObjectResult.get("instanceId"));
                anlyseResult.put("anlyseType",jsonObjectResult.get("anlyseType"));
                anlyseResult.put("resultValue",jsonObjectResult.get("resultValue"));
                String name="AnlyseResult"+jsonObjectResult.get("instanceId").toString();
                redisTemplate.opsForHash().putAll(name,anlyseResult);
                log.info("数据：",entry);
                switch (anlyseType){
                    case "1":
                    case "2":
                    case "3":
                    case "4":
                    case "5":
                    case "6":
                    case "7":
                    case "8":
                    case "9":
                    case "10":
                    case "11":
                    case "12":
                    case "13":
                    case "14":
                    case "15":
                    case "16":
                    case "17":
                    case "18":
                    case "19":
                    case "20":
                    case "21":
                    case "22":
                    case "23":
                    case "24":
                    case "25":
                    case "26":
                    case "27":

                        break;
                    default:
                        log.info("");


                }

            }


        }

    }

    public void ProcSend() {
        try {
            // 心跳报文(客户端,服务端均可发起测试);
            if (isTimeout(TYPET3, hisT3, false)) {
                SendHeartBeat(ctx);
                hisT3 = System.currentTimeMillis();
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

    private void sendStartRegister(ChannelHandlerContext ctx) {
        // 发送json字符串
        String registerMsg = "{\n\"msgType\": \"3\", \n\"msgData\": {\n\"desNode\": \"serverSocket\", \n\"srcNode\": \"clientSocket001\",\n\"registerKey\": \"yijiahe\"\n}\n}\n";
        sendString(ctx, registerMsg);
    }

    private void SendHeartBeat(ChannelHandlerContext ctx) {
        String registerMsg = "{\n\"msgType\": \"5\", \n\"msgData\": {\n\"desNode\": \"serverSocket\", \n\"srcNode\": \"clientSocket001\"\n}\n}\n";
        sendString(ctx, registerMsg);
    }

    public void sendDataReguest(JSONObject analysisObject) {
        String msg = analysisObject.toString();
        sendString(ctx, msg);
    }

}
