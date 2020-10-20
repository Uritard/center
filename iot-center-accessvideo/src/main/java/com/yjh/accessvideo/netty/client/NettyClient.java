package com.yjh.accessvideo.netty.client;

import com.yjh.accessvideo.common.Constant;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tt on 2019/7/31.
 */
public class NettyClient {

    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);

    public void start(InetSocketAddress remoteAddress1, InetSocketAddress remoteAddress2) throws InterruptedException{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new AnalysisClientChannelInitializer() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new AnalysisClientHandler());
                        }
                    });

            bootstrap.connect(remoteAddress1).addListener((ChannelFuture futureListener) -> {
                log.info("连接客户端1");
                final EventLoop eventLoop = futureListener.channel().eventLoop();
                if (!futureListener.isSuccess()) {
                    log.info("与"+remoteAddress1+"连接失败!");
                    //10秒后重连
                    eventLoop.schedule(() -> doConnect(bootstrap, remoteAddress1), 10, TimeUnit.SECONDS);
                } else { log.info("与"+remoteAddress1+"连接成功!"); }
            });
            bootstrap.connect(remoteAddress2).addListener((ChannelFuture futureListener) -> {
                log.info("连接客户端2");
                final EventLoop eventLoop = futureListener.channel().eventLoop();
                if (!futureListener.isSuccess()) {
                    log.info("和"+remoteAddress2+"连接失败!");
                    //10秒后重连
                    eventLoop.schedule(() -> doConnect(bootstrap, remoteAddress2), 10, TimeUnit.SECONDS);
                } else { log.info("与"+remoteAddress2+"连接成功!"); }
            });

//            ChannelFuture future = bootstrap.connect(remoteAddress1).sync();
//            ChannelFuture future2 = bootstrap.connect(remoteAddress2).sync();
//            future.channel().closeFuture().sync();
//            future2.channel().closeFuture().sync();
        } catch (Exception e) {e.getMessage();}
    }

    /**
     * 重新连接tcp服务端
     */
    private static void doConnect(Bootstrap bootstrap, InetSocketAddress remoteAddress) {
        try {
            if (bootstrap != null) {
                bootstrap.remoteAddress(remoteAddress);
                ChannelFuture f = bootstrap.connect().addListener((ChannelFuture futureListener) -> {
                    final EventLoop eventLoop = futureListener.channel().eventLoop();
                    if (!futureListener.isSuccess()) {
                        //连接tcp服务器不成功 10后重连
                        log.info(remoteAddress + "服务器断线-----与服务端断开连接!在30s之后准备尝试重连!");
                        eventLoop.schedule(() -> doConnect(bootstrap, remoteAddress), 30, TimeUnit.SECONDS);
                    }
                });
            }
        } catch (Exception e) { log.info("客户端连接失败!" + e.getMessage()); }
    }

}
