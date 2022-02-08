package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class RobotControlAndIssueTaskHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("机器人收到下发任务指令/控制指令了,这是机器人的响应");
        robotService.receivingResponse(xmlBaseModel, receiveSessionId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.TASK_CONTROLLER.getCode(), this);
    }
}
