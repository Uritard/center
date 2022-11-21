package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
        String robotCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(robotCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(robotCode, false))) {
            log.error("下级唯一标识未注册或未连接");
            throw new RuntimeException("下级唯一标识未注册或未连接");
        }
        // todo 暂时不用

        // 给下级响应
        String cruiseReportString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] cruiseReportProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, cruiseReportString);
        RobotServerHandler.send( cruiseReportProtocol, robotCode);
        log.info("巡视主机给下级{}响应了", robotCode);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_CRUISE_REPORT.getCode(), this);
    }
}
