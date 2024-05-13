package com.yjh.accesstcp.netty;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yjh.accesstcp.thread.ReContentManager;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/12
 * @since [产品/模块版本] （可选）
 */
@Slf4j
@RequiredArgsConstructor
public class NettyClient {
    private final Class<? extends ChannelInboundHandler> decodeHandlerClass;
    private final ChannelInboundHandler messageHandler;

    public static final Map<SocketAddress, Bootstrap> BOOTSTRAP_MAP = new ConcurrentHashMap<>();

    public void start(InetSocketAddress address) {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap().group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .handler(new TCPClientChannelInitializer());

            bootstrap.connect(address).addListener((ChannelFuture futureListener) -> {
                log.info("连接服务端1");
                final EventLoop eventLoop = futureListener.channel().eventLoop();
                if (!futureListener.isSuccess()) {
                    log.info("和服务端1：" + address + "连接失败!");

                    //10秒后重连
                    eventLoop.schedule(() -> ReContentManager.reContent(address, bootstrap), 10, TimeUnit.SECONDS);
                } else {
                    log.info("和服务端1：" + address + "连接成功!");
                }
            });

            BOOTSTRAP_MAP.put(address, bootstrap);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private class TCPClientChannelInitializer extends ChannelInitializer<SocketChannel> {

        private EventExecutorGroup group;

        public TCPClientChannelInitializer() {

            this.group = new DefaultEventExecutorGroup(NettyRuntime.availableProcessors() * 2,
                new ThreadFactoryBuilder().setNameFormat("tcp-service-handler-group-%d").build());
        }

        @Override
        protected void initChannel(SocketChannel socketChannel) throws Exception {
            ChannelPipeline p = socketChannel.pipeline();
            p.addLast(decodeHandlerClass.getDeclaredConstructor().newInstance());
            p.addLast(group, messageHandler);
        }
    }
}
