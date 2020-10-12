package com.yjh.accessvideo.netty.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by tt on 2019/7/31.
 */
public class NettyClient {

    private static final Logger log = LoggerFactory.getLogger(NettyClient.class);
    private volatile boolean exit = false;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public void start(InetSocketAddress remoteAddress, String gatewayName, int changeDataNum) throws InterruptedException{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap()
            .group(group)
            .channel(NioSocketChannel.class)
            .option(ChannelOption.SO_KEEPALIVE, true)
            .option(ChannelOption.TCP_NODELAY, true)
            .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
            .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
            .handler(new IEC104ClientChannelInitializer(gatewayName, changeDataNum) {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new IEC104ClientHandler(gatewayName, changeDataNum));
                }
            });
            //ChannelFuture future = b.connect(remoteAddress).sync();
            //future.channel().closeFuture().sync();

            //多网关连接
            List<Channel> channels = new ArrayList<>();
            for(int i=1; i<=500; i++) {
                ChannelFuture future = b.connect(remoteAddress).sync();
                channels.add(future.channel());
            }

        } finally {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (!exit) {
                        log.info("连接服务端失败，5s重连1次，第"+"次");
                    }
                }
            });
        }
    }

}
