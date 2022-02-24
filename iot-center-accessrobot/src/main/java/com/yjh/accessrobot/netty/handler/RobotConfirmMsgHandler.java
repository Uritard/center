package com.yjh.accessrobot.netty.handler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author hyh
 * @since 2022/2/8
 **/
@Slf4j
@Service
public class RobotConfirmMsgHandler implements MessageHandlerStrategy, InitializingBean {

    @Resource
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("巡视主机收到机器人确认消息了");
        String robotCode = xmlBaseModel.getSendCode();
        robotService.updateConfirmMsgForRedis(xmlBaseModel);
        String operationXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, operationXmlString);
        RobotServerHandler.send( operationProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_CONFIRM_MSG.getCode(), this);
    }
}
