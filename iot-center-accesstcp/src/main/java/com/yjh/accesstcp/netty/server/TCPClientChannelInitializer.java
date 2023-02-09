package com.yjh.accesstcp.netty.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.module.device.service.AnalysisUnionTaskFileService;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import com.yjh.accesstcp.thread.RegisterManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class TCPClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RedisTemplate redisTemplate;
    private SendToUpSystemServices sendToUpSystemServices;
    private AnalysisUnionTaskFileService analysisUnionTaskFileService;
    private String server;
    private String cruise;
    private RegisterManager registerManager;
    private EventExecutorGroup group;
    public TCPClientChannelInitializer(RedisTemplate redisTemplate, SendToUpSystemServices sendToUpSystemServices, AnalysisUnionTaskFileService analysisUnionTaskFileService, String server,String cruise, RegisterManager registerManager) {
        this.redisTemplate =redisTemplate;
        this.sendToUpSystemServices = sendToUpSystemServices;
        this.analysisUnionTaskFileService = analysisUnionTaskFileService;
        this.server =server;
        this.cruise = cruise;
        this.registerManager = registerManager;
        this.group = new DefaultEventExecutorGroup(NettyRuntime.availableProcessors() * 2,
            new ThreadFactoryBuilder().setNameFormat("tcp-service-handler-group-%d").build());
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        if (Constant.handlerNew) {
            p.addLast(new StateGridADecoder());
            StateGridAHandlerImpl robotServerHandler = new StateGridAHandlerImpl(redisTemplate, sendToUpSystemServices, analysisUnionTaskFileService, server, cruise, registerManager);
            p.addLast(group, robotServerHandler);
        } else {
            TCPClientHandlerImpl robotServerHandler = new TCPClientHandlerImpl(redisTemplate, sendToUpSystemServices, analysisUnionTaskFileService, server, cruise, registerManager);
            p.addLast(robotServerHandler);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(null != ctx.pipeline().channel()){
            log.info("IEC104ServerChannelInitializer channelInactive is not null ,remove ");
        }else{
            log.info("IEC104ServerChannelInitializer channelInactive is null");
        }
        ctx.close().sync();
        ctx.flush();
    }

}
