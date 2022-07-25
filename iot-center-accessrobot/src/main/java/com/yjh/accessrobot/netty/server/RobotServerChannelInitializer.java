package com.yjh.accessrobot.netty.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.module.command.service.RobotService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;
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
    private EventExecutorGroup group;

    public RobotServerChannelInitializer( RedisTemplate redisTemplate, RobotService robotService) {
        this.redisTemplate = redisTemplate;
        this.robotService = robotService;
        this.group = new DefaultEventExecutorGroup(NettyRuntime.availableProcessors() * 2,
                new ThreadFactoryBuilder().setNameFormat("service-handler-group-%d").build());
    }

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        // netty自带心跳检测
        /*channel.pipeline().addLast(new IdleStateHandler(30,0,0, TimeUnit.SECONDS));*/
        if (Constant.handlerNew) {
            channel.pipeline().addLast(new StateGridADecoder());
            StateGridAHandlerImpl robotServerHandler = new StateGridAHandlerImpl();
            robotServerHandler.setRedisTemplate(redisTemplate);
            robotServerHandler.setRobotService(robotService);
            channel.pipeline().addLast(group, robotServerHandler);
        } else {
            RobotServerHandlerImpl robotServerHandler = new RobotServerHandlerImpl();
            robotServerHandler.setRedisTemplate(redisTemplate);
            robotServerHandler.setRobotService(robotService);
            channel.pipeline().addLast(group, robotServerHandler);
        }
        //添加心跳检查包
//        ChannelPipeline pipeline = channel.pipeline();
//        pipeline.addLast(new IdleStateHandler(5,0,0, TimeUnit.SECONDS));
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
