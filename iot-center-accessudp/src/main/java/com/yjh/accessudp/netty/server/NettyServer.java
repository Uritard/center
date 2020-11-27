package com.yjh.accessudp.netty.server;

import com.yjh.accessudp.module.device.service.TCfgMeteService;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.springframework.data.redis.core.RedisTemplate;

import java.net.InetSocketAddress;

/**
 * Created by tt on 2019/7/31
 */
@lombok.extern.slf4j.Slf4j
public class NettyServer {

    public void start(InetSocketAddress address, RedisTemplate redisTemplate, TCfgMeteService tCfgMeteService,String UNION_URL){
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap()
            .group(group)
            .channel(NioDatagramChannel.class)
            .localAddress(address)
            .option(ChannelOption.SO_BROADCAST, true)
            .option(ChannelOption.SO_RCVBUF, 1024 * 1024 * 100)
            .option(ChannelOption.SO_SNDBUF, 1024 * 1024)
            .handler(new UDPServerChannelInitializer(redisTemplate, tCfgMeteService,UNION_URL));
//            .option(ChannelOption.SO_BACKLOG, 2048)  //指定此套接口排队的最大连接个数
//            .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT); //容量动态调整的接收缓冲区分配器 以节约内存
            // 绑定端口，开始接收进来的连接
            ChannelFuture future = bootstrap.bind(address).sync();
            log.info("Server start listen at " + address.getPort());
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

}
