package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.module.command.service.RobotService;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.AdaptiveRecvByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.InetSocketAddress;

/**
 * Created by tt on 2019/7/31
 */
@lombok.extern.slf4j.Slf4j
public class NettyServer {

    public void start(InetSocketAddress address, RedisTemplate redisTemplate, RobotService robotService){
        // 1.创建两个事件组,boss用于处理请求的accept事件,work用于请求的read和write事件
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            // 2.创建辅助工具类ServerBootstrap,用于服务器通道的一系列配置
            ServerBootstrap bootstrap = new ServerBootstrap()
            // 绑定两个线程组
            .group(bossGroup,workerGroup)
            // 指定NIO的模式,NioServerSocketChannel对应TCP,NioDatagramChannel对应UDP
            .channel(NioServerSocketChannel.class)
            .localAddress(address)
            .childHandler(new RobotServerChannelInitializer(redisTemplate, robotService))
            // 指定此套接口排队的最大连接个数(缓冲区)
            .option(ChannelOption.SO_BACKLOG, 2048)
            // 保持连接生命，不因空闲而断开
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            // 防止数据传输延迟 如果false的话会缓冲数据达到一定量在flush,降低系统网络调用（具体场景）
            .childOption(ChannelOption.TCP_NODELAY, true)
            // 容量动态调整的接收缓冲区分配器 以节约内存
            .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
            // Netty实现了一个Java版的Jemalloc内存管理库 ByteBuf内存池 要搭配ReferenceCountUtil.release(msg);不然造成内存泄漏;
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
            // 4.绑定端口，开始接收进来的连接.绑定地址,bind返回future(异步),加上sync阻塞在获取连接处
            ChannelFuture future = bootstrap.bind(address).sync();
            log.info("Server start listen at " + address.getPort());
            // 等待关闭,加上sync阻塞在关闭请求处
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage());
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
