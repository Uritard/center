package com.yjh.accessrobot.netty.handler.drone;

import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.DroneService;
import com.yjh.accessrobot.netty.entiy.DroneHandlerEnum;
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
public class DroneControlAndIssueTaskHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Autowired
    private DroneService droneService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("无人机收到下发任务指令/控制指令了,这是无人机的响应");
        droneService.receivingResponse(xmlBaseModel, receiveSessionId);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.TASK_CONTROLLER.getCode(), this);
    }
}
