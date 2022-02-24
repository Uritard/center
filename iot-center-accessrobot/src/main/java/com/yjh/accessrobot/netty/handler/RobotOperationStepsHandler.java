package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author hyh
 * @since 2022/2/14
 **/
@Slf4j
@Service
public class RobotOperationStepsHandler implements MessageHandlerStrategy, InitializingBean {

    @Resource
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("巡视主机收到机器人步骤消息了");
        String robotCode = xmlBaseModel.getSendCode();
        robotService.updateOperationStepsForRedis(xmlBaseModel);
        String roadXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] roadProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, roadXmlString);
        RobotServerHandler.send( roadProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.OPERATION_STEPS.getCode(), this);
    }
}
