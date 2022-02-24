package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.module.command.service.RobotService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.concurrent.TimeUnit;

/**
 * @author tt
 * @date 2019/7/31
 */
@Slf4j
public class RobotServerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private RedisTemplate redisTemplate;
    private RobotService robotService;

    public RobotServerChannelInitializer( RedisTemplate redisTemplate, RobotService robotService) {
        this.redisTemplate = redisTemplate;
        this.robotService = robotService;
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        RobotServerHandler robotServerHandler = new RobotServerHandler();
        robotServerHandler.setRedisTemplate(redisTemplate);
        robotServerHandler.setRobotService(robotService);
        // netty自带心跳检测
        /*channel.pipeline().addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));*/
        channel.pipeline().addLast(robotServerHandler);
        //添加心跳检查包
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast(new IdleStateHandler(5,0,0, TimeUnit.SECONDS));
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
