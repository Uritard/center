package com.yjh.accessmeter.protocol.impl.transport;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yjh.accessmeter.common.utils.ByteUtil;
import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.netty.DLT645Decoder;
import com.yjh.accessmeter.netty.DLT645Encoder;
import com.yjh.accessmeter.netty.DLT645Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/9/12
 * @since [产品/模块版本] （可选）
 */
public enum MeterTcpClientManager {

    /**
     * 初始化
     */
    INSTANSE;

    private final Logger LOGGER = LoggerFactory.getLogger(TcpServerManager.class);
    public static final AttributeKey<String> METER_ATTRIBUTE_KEY =AttributeKey.valueOf("meterResponseKey");
    private final AtomicBoolean stopCollect = new AtomicBoolean(false);
    private final Map<String, Object> map = new ConcurrentHashMap<>();

    private Bootstrap bootstrap;
    private final EventExecutorGroup group;

    MeterTcpClientManager() {
        LOGGER.info("加载电表配置开始");
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        this.group = new DefaultEventExecutorGroup(NettyRuntime.availableProcessors() * 2,
                new ThreadFactoryBuilder().setNameFormat("meter-client-handler-group-%d").build());
        try {
            bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).
                    option(ChannelOption.SO_KEEPALIVE, true).
                    option(ChannelOption.TCP_NODELAY, true).
                    option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT).
                    option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .handler(new MeterClientChannelInitializer());
        }catch (Exception e) {
            LOGGER.error("启动异常", e);
            eventLoopGroup.shutdownGracefully();
        }
    }

    public ChannelFuture connect(IotDevice device) {
        try {
            return bootstrap.connect(device.getIp(), device.getPort()).sync();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    public String sendAndGet(DLT645Message dlt645Message, Channel channel) {
        String response = null;
        try {
            ChannelFuture future = null;
            SocketAddress remoteAdds = channel.remoteAddress();
            LOGGER.info("channel: {} 报文发送: {}", remoteAdds, dlt645Message);
            Object write = new Object();
            String key = ByteUtil.toHexString(dlt645Message.getDataType());
            map.put(key, write);
            //尝试向电表要10次数据
            int count = 10;
            for (int i = 0; i < count; i++) {
                synchronized (write) {
                    future = channel.writeAndFlush(dlt645Message);
                    write.wait(2000);
                }
                if (stopCollect.get()){
                    break;
                }
            }
            map.remove(key);
            stopCollect.set(false);
            response = future.channel().attr(METER_ATTRIBUTE_KEY).getAndSet(null);

            LOGGER.info("channel: {} 收到报文: {}", remoteAdds, response);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return Optional.ofNullable(response).orElse("0");
    }

    private class MeterClientChannelInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        protected void initChannel(SocketChannel channel) throws Exception {
            channel.pipeline()
                    .addLast(new DLT645Decoder())
                    .addLast(new DLT645Encoder())
                    .addLast(group, new DLT645MessgeHandler());
        }
    }

    private class DLT645MessgeHandler extends SimpleChannelInboundHandler<DLT645Message> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, DLT645Message dlt645Message) throws Exception {
            try {
                LOGGER.info("收到返回消息[{}]: {}", ctx.channel().remoteAddress(), dlt645Message);
                Channel channel = ctx.channel();
                channel.attr(METER_ATTRIBUTE_KEY).set(dlt645Message.getValue());
                stopCollect.set(true);
                Object write = map.remove(ByteUtil.toHexString(dlt645Message.getDataType()));
                synchronized (write) {
                    write.notifyAll();
                }
            } catch (Exception e) {
                LOGGER.error("消息处理错误", e);
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            LOGGER.info("电表客户端注册成功: {}", ctx.channel());
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            LOGGER.error("发生异常", cause);
        }
    }
}
