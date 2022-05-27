package com.yjh.accessvideo.netty.client;

import com.yjh.accessvideo.common.Constant;
import com.yjh.accessvideo.module.device.service.AnalyseDataOperateService;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by tt on 2019/7/31.
 */
public class NettyClient {

    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);

    public void start(InetSocketAddress remoteAddress1, InetSocketAddress remoteAddress2, RedisTemplate redisTemplate, AnalyseDataOperateService analyseDataOperateService,
                      String syncWebsocketUrl,String stationCode) throws InterruptedException{

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new AnalysisClientChannelInitializer(redisTemplate,analyseDataOperateService,syncWebsocketUrl, stationCode) {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new AnalysisClientHandler(redisTemplate,analyseDataOperateService,syncWebsocketUrl, stationCode));
                        }
                    });

            Constant.bootstrapHashMap.put(1, bootstrap);
            bootstrap.connect(remoteAddress1).addListener((ChannelFuture futureListener) -> {
                log.info("连接服务端1");
                final EventLoop eventLoop = futureListener.channel().eventLoop();
                if (!futureListener.isSuccess()) {
                    log.info("和服务端1："+remoteAddress1+"连接失败!");
                    //10秒后重连
                    eventLoop.schedule(() -> doConnect(remoteAddress1, bootstrap), 10, TimeUnit.SECONDS);
                } else { log.info("和服务端1："+remoteAddress1+"连接成功!"); }
            });
            bootstrap.connect(remoteAddress2).addListener((ChannelFuture futureListener) -> {
                log.info("连接服务端2");
                final EventLoop eventLoop = futureListener.channel().eventLoop();
                if (!futureListener.isSuccess()) {
                    log.info("和服务端2："+remoteAddress2+"连接失败!");
                    //10秒后重连
                    eventLoop.schedule(() -> doConnect(remoteAddress2, bootstrap), 10, TimeUnit.SECONDS);
                } else { log.info("和服务端2："+remoteAddress2+"连接成功!"); }
            });
        } catch (Exception e) {e.getMessage();}
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
                        log.info("与服务端"+remoteAddress + "连接失败!准备尝试重连!");
                        eventLoop.schedule(() -> doConnect(remoteAddress, bootstrap), 60, TimeUnit.SECONDS);
                    }
                });
            }
        } catch (Exception e) { log.info("------连接服务端连接失败------" + e.getMessage()); }
    }

}
