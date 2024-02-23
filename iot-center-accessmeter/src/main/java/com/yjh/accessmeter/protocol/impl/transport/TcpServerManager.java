/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.impl.transport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/19
 * @since [产品/模块版本] （可选）
 */
public enum TcpServerManager {
    /**
     * 初始化
     */
    INSTANSE;

    private final Logger LOGGER = LoggerFactory.getLogger(TcpServerManager.class);
    private ServerBootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private EventExecutorGroup group;

    private static final Map<SocketAddress, ServerListener> SERVER_LISTENER_MAP = new ConcurrentHashMap<>(16);

    TcpServerManager() {
        this.eventLoopGroup = new NioEventLoopGroup();
        this.group = new DefaultEventExecutorGroup(NettyRuntime.availableProcessors() * 2,
            new ThreadFactoryBuilder().setNameFormat("service-handler-group-%d").build());
        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(eventLoopGroup).channel(NioServerSocketChannel.class)
                     // 指定此套接口排队的最大连接个数(缓冲区)
                     .option(ChannelOption.SO_BACKLOG, 2048)
                     // 保持连接生命，不因空闲而断开
                     .childOption(ChannelOption.SO_KEEPALIVE, true)
                     // 防止数据传输延迟 如果false的话会缓冲数据达到一定量在flush,降低系统网络调用（具体场景）
                     .childOption(ChannelOption.TCP_NODELAY, true).childHandler(new EnvTerminalServerChannelInitializer());

        } catch (Exception e) {
            LOGGER.error("启动异常", e);
            eventLoopGroup.shutdownGracefully();
        }
    }

    public ChannelFuture bind(String ip, int port, ServerListener listener) {
        String key = ip + ":" + port;
        try {
            InetSocketAddress socketAddress = StringUtils.isNotEmpty(ip) ? new InetSocketAddress(ip, port) : new InetSocketAddress(port);
            SERVER_LISTENER_MAP.put(socketAddress, listener);

            ChannelFuture future = bootstrap.bind(socketAddress).sync();
            LOGGER.info("netty 绑定地址：{}:{}", ip, port);
            return future;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private class EnvTerminalServerChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            channel.pipeline()
                   .addLast(new StringDecoder(StandardCharsets.UTF_8))
                   .addLast(new StringEncoder(StandardCharsets.UTF_8))
                   .addLast(group, new ServerInboundHandler());
        }
    }

    private class ServerInboundHandler extends SimpleChannelInboundHandler<String> {
        StringBuffer buffer = new StringBuffer();

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            try {
                if (!StringUtils.contains(msg, TcpShortManager.END_WORDS)) {
                    buffer.append(msg);
                    return;
                }

                int chip = StringUtils.indexOf(msg, TcpShortManager.END_WORDS);
                String msgSuff = StringUtils.substring(msg, 0, chip + TcpShortManager.END_WORDS.length());
                String msgLeft = StringUtils.substring(msg, chip + TcpShortManager.END_WORDS.length());
                buffer.append(msgSuff);

                String message = buffer.toString();
                LOGGER.info("收到上送消息: {}", message);
                InetSocketAddress address = (InetSocketAddress)ctx.channel().localAddress();
                ServerListener listener = SERVER_LISTENER_MAP.get(address);
                if (listener == null) {
                    address = new InetSocketAddress(address.getPort());
                    listener = SERVER_LISTENER_MAP.get(address);
                }
                if (listener != null) {
                    ctx.executor().execute(new MessageHandler(ctx, listener, message));
                } else {
                    LOGGER.error("消息未注册处理方式，不处理, {}", address);
                }

                buffer = new StringBuffer(msgLeft);
            } catch (Exception e) {
                LOGGER.error("消息处理错误", e);
            }
        }

        /*
                @Override
                public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
                    LOGGER.info("Connection is Added. {}", ctx.channel());
                }

                @Override
                public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
                    LOGGER.info("Connection is Removed. {}", ctx.channel());
                }
                */
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            //channel在线处理，都会触发这个方法
            String remoteAdds = ctx.channel().remoteAddress().toString();

            LOGGER.info("客户端注册上线: {}", remoteAdds);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            // channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
            LOGGER.info("客户端连接断开！{}", ctx.channel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            LOGGER.error("exceptionCaught:", cause);
        }

    }

    private static class MessageHandler implements Runnable {

        private final ChannelHandlerContext ctx;
        private final ServerListener listener;
        private final String message;


        private MessageHandler(ChannelHandlerContext ctx, ServerListener listener, String message) {
            this.ctx = ctx;
            this.listener = listener;
            this.message = message;
        }

        @Override
        public void run() {
            String result = listener.dispatch(ctx, message);
            ctx.channel().writeAndFlush(result);
        }
    }
}
