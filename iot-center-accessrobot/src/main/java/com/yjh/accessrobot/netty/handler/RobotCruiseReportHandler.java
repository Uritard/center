package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;


/**
 * @author 丫C
 * @date 2022/6/2
 */
@Slf4j
@Service
public class RobotCruiseReportHandler implements MessageHandlerStrategy, InitializingBean {
    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到机器人巡视报告数据了+++++++++++++++++");
        // todo 暂时不用
        String robotCode = xmlBaseModel.getSendCode();
        String taskStatusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] taskStatusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, taskStatusXmlString);
        RobotServerHandler.send(taskStatusProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_CRUISE_REPORT.getCode(), this);
    }
}
