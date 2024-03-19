/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.impl.transport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/2/1
 * @since [产品/模块版本] （可选）
 */
public enum TcpShortManager {
    /**
     * 初始化
     */
    INSTANSE;

    private final Logger LOGGER = LoggerFactory.getLogger(TcpShortManager.class);
    private static final Map<String, ChannelFuture> CONNECT_MAP = new ConcurrentHashMap<>(16);
    public static final AttributeKey<String> RESPONSE_KEY = AttributeKey.valueOf("shortResponseKey");
    public static final String END_WORDS = "</root>";

    private Bootstrap bootstrap;
    private EventLoopGroup eventLoopGroup;
    private EventExecutorGroup group;

    TcpShortManager() {
        this.eventLoopGroup = new NioEventLoopGroup();
        this.group = new DefaultEventExecutorGroup(NettyRuntime.availableProcessors() * 2,
            new ThreadFactoryBuilder().setNameFormat("client-handler-group-%d").build());
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class)
                     // .option(ChannelOption.SO_KEEPALIVE, false)
                     // .option(ChannelOption.TCP_NODELAY, true)
                     // .option(ChannelOption.SO_TIMEOUT, 5000)
                     .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15000)
                     .handler(new EnvTerminalClientChannelInitializer());

        } catch (Exception e) {
            LOGGER.error("启动异常", e);
            eventLoopGroup.shutdownGracefully();
        }
    }

    public ChannelFuture connect(IotDevice device) {
        // String key = device.getIp() + ":" + device.getPort() + ":" + device.getProtocolModel();
        try {
            return bootstrap.connect(device.getIp(), device.getPort()).sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public String sendAndGet(String msgXml, IotDevice device) {
        String response = null;
        try {
            Channel channel = connect(device).channel();
            ChannelFuture future = null;

            SocketAddress remoteAdds = channel.remoteAddress();
            LOGGER.info("channel: {} 报文发送: {}", remoteAdds, msgXml);
            synchronized (channel) {
                future = channel.writeAndFlush(msgXml);
                channel.wait(3500);
            }
            future.channel().closeFuture().sync();

            response = future.channel().attr(TcpShortManager.RESPONSE_KEY).get();
            LOGGER.info("channel: {} 收到报文: {}", remoteAdds, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return response;
    }

    private class EnvTerminalClientChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            channel.pipeline()
                   .addLast(new StringDecoder(StandardCharsets.UTF_8))
                   .addLast(new StringEncoder(StandardCharsets.UTF_8))
                   .addLast(group, new ShortInboundHandler());
        }
    }

    private class ShortInboundHandler extends SimpleChannelInboundHandler<String> {
        StringBuffer buffer = new StringBuffer();

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
            try {
                buffer.append(msg);
                if(!StringUtils.contains(msg, END_WORDS)){
                    return;
                }

                LOGGER.info("收到返回消息[{}]: {}", ctx.channel().remoteAddress(), buffer);
                Channel channel = ctx.channel();
                channel.attr(RESPONSE_KEY).set(buffer.toString());
                buffer = new StringBuffer();
                synchronized (channel) {
                    channel.notifyAll();
                }
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

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            // channel失效处理,客户端下线或者强制退出等任何情况都触发这个方法
            LOGGER.error("连接断开！{}", ctx.channel());
        }
        */

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            //channel在线处理，都会触发这个方法
            // String remoteAdds = ctx.channel().remoteAddress().toString();

            LOGGER.info("客户端注册成功: {}", ctx.channel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            LOGGER.error("exceptionCaught:", cause);
        }

    }
}
