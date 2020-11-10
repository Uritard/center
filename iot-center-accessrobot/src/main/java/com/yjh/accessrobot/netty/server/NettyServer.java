package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.module.device.service.SysLogsService;
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

    public void start(InetSocketAddress address, String serverName, RedisTemplate redisTemplate, SysLogsService sysLogsService){
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap()
            .group(bossGroup,workerGroup)
            .channel(NioServerSocketChannel.class)
            .localAddress(address)
            .childHandler(new RobotServerChannelInitializer(serverName, redisTemplate, sysLogsService))
            .option(ChannelOption.SO_BACKLOG, 2048)  //指定此套接口排队的最大连接个数
            .childOption(ChannelOption.SO_KEEPALIVE, true)  //保持连接生命，不因空闲而断开
            .childOption(ChannelOption.TCP_NODELAY, true)  //防止数据传输延迟 如果false的话会缓冲数据达到一定量在flush,降低系统网络调用（具体场景）
            .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT) //容量动态调整的接收缓冲区分配器 以节约内存
            .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);//Netty实现了一个Java版的Jemalloc内存管理库 ByteBuf内存池 要搭配ReferenceCountUtil.release(msg);不然造成内存泄漏;
            // 绑定端口，开始接收进来的连接
            ChannelFuture future = bootstrap.bind(address).sync();
            log.info("Server start listen at " + address.getPort());
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage());
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
