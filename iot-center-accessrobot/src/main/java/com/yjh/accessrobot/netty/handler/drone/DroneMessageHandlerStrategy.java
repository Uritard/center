package com.yjh.accessrobot.netty.handler.drone;

import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author YChen
 * @date 2021/12/14
 */
public interface DroneMessageHandlerStrategy {

    /**
     * 根据type区别不同的消息进行处理
     * @param ctx 通道
     * @param droneServerHandler 接收消息处理类
     * @param xmlBaseModel xml格式的内容
     * @param sendSessionId 发送会话序列号
     * @param receiveSessionId 接收会话序列号
     * @return void
     * @throws Exception
     */
    void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception;
}
