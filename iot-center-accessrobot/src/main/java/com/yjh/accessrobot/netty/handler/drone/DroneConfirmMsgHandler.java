package com.yjh.accessrobot.netty.handler.drone;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.DroneService;
import com.yjh.accessrobot.netty.entiy.DroneHandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author hyh
 * @since 2022/2/8
 **/
@Slf4j
@Service
public class DroneConfirmMsgHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Resource
    private DroneService droneService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("巡视主机收到无人机确认消息了");
        String droneCode = xmlBaseModel.getSendCode();
        droneService.updateConfirmMsgForRedis(xmlBaseModel);
        String operationXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, droneCode));
        byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, operationXmlString);
        RobotServerHandler.send(operationProtocol, droneCode);
        log.info("巡视主机给无人机{}响应了", droneCode);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.DRONE_CONFIRM_MSG.getCode(), this);
    }
}
