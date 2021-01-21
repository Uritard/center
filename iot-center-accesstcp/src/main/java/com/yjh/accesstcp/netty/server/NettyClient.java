package com.yjh.accesstcp.netty.server;

import com.yjh.accesstcp.common.Constant;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Created by tt on 2019/7/31
 */
@lombok.extern.slf4j.Slf4j
public class NettyClient {

    public void start(InetSocketAddress address, RedisTemplate redisTemplate, SendToUpSystemServices sendToUpSystemServices,String server,String cruise) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new TCPClientChannelInitializer(redisTemplate, sendToUpSystemServices,server, cruise) {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new TCPClientHandler(redisTemplate, sendToUpSystemServices, server, cruise));
                        }
                    });

            Constant.bootstrapHashMap.put(1, bootstrap);
            bootstrap.connect(address).addListener((ChannelFuture futureListener) -> {
                log.info("连接服务端1");
                final EventLoop eventLoop = futureListener.channel().eventLoop();
                if (!futureListener.isSuccess()) {
                    log.info("和服务端1：" + address + "连接失败!");
                    //10秒后重连
                    eventLoop.schedule(() -> doConnect(address, bootstrap), 10, TimeUnit.SECONDS);
                } else {
                    log.info("和服务端1：" + address + "连接成功!");
                }
            });
        }catch (Exception e) {e.getMessage();}
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
