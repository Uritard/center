package com.yjh.accesstcp.netty.server;

import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class TCPClientChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RedisTemplate redisTemplate;
    private SendToUpSystemServices sendToUpSystemServices;

    public TCPClientChannelInitializer(RedisTemplate redisTemplate, SendToUpSystemServices sendToUpSystemServices) {
        this.redisTemplate =redisTemplate;
        this.sendToUpSystemServices = sendToUpSystemServices;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline p = socketChannel.pipeline();
        //p.addLast("decoder", new StringDecoder(CharsetUtil.UTF_8));
        //p.addLast("encoder", new StringEncoder(CharsetUtil.UTF_8));
        p.addLast(new TCPClientHandler(redisTemplate, sendToUpSystemServices));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if(null != ctx.pipeline().channel()){
            log.info("IEC104ServerChannelInitializer channelInactive is not null ,remove ");
        }else{
            log.info("IEC104ServerChannelInitializer channelInactive is null");
        }
        ctx.close().sync();
        ctx.flush();
    }

}
