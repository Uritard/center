package com.yjh.accessvideo.netty.client;


import com.alibaba.fastjson.JSONObject;
import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.module.device.entity.Analysis;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.EventLoop;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static com.yjh.accessvideo.common.Constant.TYPET3;



/**
 * Created by tt on 2019/7/31.
 */
public class AnalysisClientHandler extends ChannelInboundHandlerAdapter {

    private Logger log = LoggerFactory.getLogger(AnalysisClientHandler.class);

    private long hisT3 = System.currentTimeMillis();
    private long T3 = 20000;
    public boolean isThreadStart;
    private ChannelHandlerContext ctx;

    private RedisTemplate redisTemplate;
    private AnalyseDataOperateService analyseDataOperateService;
    private String syncWebsocketUrl;
    private String stationCode;

    public AnalysisClientHandler(RedisTemplate redisTemplate, AnalyseDataOperateService analyseDataOperateService,String syncWebsocketUrl, String stationCode) {
        this.redisTemplate = redisTemplate;
        this.analyseDataOperateService = analyseDataOperateService;
        this.syncWebsocketUrl=syncWebsocketUrl;
        this. stationCode = stationCode;
    }

    private static Map<Integer, AnalysisClientHandler> analysisClientHandlerHashMap = new HashMap<>();

    public static Map<Integer, AnalysisClientHandler> getAnalysisClientHandlerHashMap() {
        return analysisClientHandlerHashMap;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws InterruptedException {
        sendStartRegister(ctx);
        Thread.sleep(3000);
        this.ctx = ctx;
        isThreadStart = true;
        //启动心跳检测
//        HeartBeatThread heartBeatThread = new HeartBeatThread(this, true);
//        Thread thread = new Thread(heartBeatThread);
//        thread.setDaemon(true);
//        thread.start();
        String remoteAdds = ctx.channel().remoteAddress().toString();
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));
        if (analysisClientHandlerHashMap.get(remotePort) == null) {
            analysisClientHandlerHashMap.put(remotePort, this);
        }
        log.info("客户端注册成功: " + ctx.channel().remoteAddress());
        log.info("analysisClientHandlerHashMap: " + analysisClientHandlerHashMap);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效处理,服务端下线或者强制退出等任何情况都触发这个方法
        ctx.close().sync();
        ctx.flush();
        super.channelInactive(ctx);
        isThreadStart = false;
        String remoteAdds = ctx.channel().remoteAddress().toString();
        int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));
        analysisClientHandlerHashMap.remove(remotePort);
        log.error("服务端主动断开连接！");
        log.info("analysisClientHandlerHashMap: " + analysisClientHandlerHashMap);
        String serverUrl = remoteAdds.substring(0, remoteAdds.indexOf(":"));
        log.info("serverUrl: " + serverUrl.substring(1));
        InetSocketAddress remoteAddress = new InetSocketAddress(serverUrl.substring(1), remotePort);
        //使用过程中断线重连
        if (Objects.nonNull(Constant.bootstrapHashMap.get(1))) {
            doConnect(remoteAddress, Constant.bootstrapHashMap.get(1));
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws InterruptedException {
        ByteBuf byteBuf = (ByteBuf) msg;
        byte[] bytes = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(bytes);
        try {
            String body = new String(bytes, "UTF-8");
            log.info("接收服务端数据:" + body);
            // 反拆包解析
            String usefulBody = analyseDataOperateService.nonUnpacking(body);

            // Port:13668-表计识别,Port:13669-缺陷识别
            String remoteAdds = ctx.channel().remoteAddress().toString();
            int remotePort = Integer.parseInt(remoteAdds.substring(remoteAdds.indexOf(":") + 1));
            if(usefulBody !="") {
                // DataDealThread dataDealThread = new DataDealThread(usefulBody, remotePort, redisTemplate, analyseDataOperateService,
                //         syncWebsocketUrl, stationCode);
                // TaskExecutePool.getInstance().execute(dataDealThread);
//            handlerData(body); //单线程数据处理
            }
        } catch (Exception e) {
            log.error("任务失败: ", e);
        }

        ReferenceCountUtil.release(byteBuf);
    }

    public void ProcSend() {
        try {
            // 心跳报文(客户端,服务端均可发起测试);
            if (isTimeout(TYPET3, hisT3, false)) {
//                SendHeartBeat();
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
//        String fakeData="";
//        sendString(ctx,fakeData);
    }

    public void SendHeartBeat(Analysis analysis) throws InterruptedException {
        log.info("analysis-INFO-----:"+analysis);
        String instanceId = analysis.getInstanceId().toString();
        String taskId = analysis.getTaskId();
        Thread.sleep(3000);
        String registerMsg = "{\n\"msgType\": \"5\", \n\"msgData\": {\n\"instanceId\": " + "\"" + instanceId + "\", \n\"taskId\": \"" + taskId + "\"\n}\n}\n";
        sendString(ctx, registerMsg);
    }

    //发送请求数据
    public void sendDataReguest(JSONObject analysisObject) throws InterruptedException {
        String msg = analysisObject.toString();
        Thread.sleep(3000);//延迟发送数据
        sendString(ctx, msg);
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
