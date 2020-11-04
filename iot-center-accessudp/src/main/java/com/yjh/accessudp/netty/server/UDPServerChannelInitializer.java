package com.yjh.accessudp.netty.server;

import com.yjh.accessudp.module.device.service.SysLogsService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

/**
 * Created by tt on 2019/7/31.
 */
@Slf4j
public class UDPServerChannelInitializer extends ChannelInitializer<NioDatagramChannel> {

    private RedisTemplate redisTemplate;
    private SysLogsService sysLogsService;
    private NioDatagramChannel nioDatagramChannel;

    public UDPServerChannelInitializer(RedisTemplate redisTemplate, SysLogsService sysLogsService) {
        this.redisTemplate = redisTemplate;
        this.sysLogsService = sysLogsService;
    }

    @Override
    protected void initChannel(NioDatagramChannel nioDatagramChannel) throws Exception {
        log.info("IEC104ServerChannelInitializer channelInit....");
        nioDatagramChannel.pipeline().addLast(new UDPServerHandler(sysLogsService, redisTemplate));
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
