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
    private volatile boolean exit = false;

    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public void start(InetSocketAddress remoteAddress1, InetSocketAddress remoteAddress2) throws InterruptedException{
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap()
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
            ChannelFuture future = b.connect(remoteAddress1).sync();
            future.channel().closeFuture().sync();

//            ChannelFuture future2 = b.connect(remoteAddress2).sync();
//            future2.channel().closeFuture().sync();



        } finally {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    while (!exit) {
                        Constant.connectTimeCounts = Constant.connectTimeCounts+1;
                        if (Constant.connectTimeCounts<=5) {
                            try {
                                log.info("连接服务端失败，5s重连1次，第"+Constant.connectTimeCounts+"次");
                                TimeUnit.SECONDS.sleep(5);
                                start(remoteAddress1, remoteAddress2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else if (5<Constant.connectTimeCounts && Constant.connectTimeCounts<=10){
                            try {
                                log.info("连接服务端失败，60s重连1次，第"+Constant.connectTimeCounts+"次");
                                TimeUnit.SECONDS.sleep(60);
                                start(remoteAddress1, remoteAddress2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        } else if (Constant.connectTimeCounts>10){
                            log.error("连接服务端失败！");
                            exit = true;
                            group.shutdownGracefully();
                            break;
                        }
                    }
                }
            });
        }
    }

}
