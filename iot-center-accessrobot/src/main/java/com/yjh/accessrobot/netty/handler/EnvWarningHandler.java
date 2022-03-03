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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;


/**
*环境设备告警数据处理程序
*
*/

@Service
@Slf4j
public class EnvWarningHandler implements MessageHandlerStrategy, InitializingBean  {
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("巡视主机收到机器人站端的环境设备告警数据");
        String robotCode = xmlBaseModel.getSendCode();
            Map<String, String> envWarn = new HashMap<>();
           envWarn.put("robotName",xmlBaseModel.getItems().get(0).get("robot_name").toString());
           envWarn.put("robotCode",xmlBaseModel.getItems().get(0).get("robot_code").toString());
           envWarn.put("time",xmlBaseModel.getItems().get(0).get("time").toString());
           envWarn.put("type",xmlBaseModel.getItems().get(0).get("type").toString());
           envWarn.put("value",xmlBaseModel.getItems().get(0).get("vlaue").toString());
           envWarn.put("valueUnit",xmlBaseModel.getItems().get(0).get("value_unit").toString());
           envWarn.put("unit",xmlBaseModel.getItems().get(0).get("unit").toString());
           envWarn.put("sn","0000"+xmlBaseModel.getItems().get(0).get("sn").toString());
           envWarn.put("valueType",xmlBaseModel.getItems().get(0).get("value_type").toString());
           envWarn.put("alarmTime",xmlBaseModel.getItems().get(0).get("alarm_time").toString());
           envWarn.put("deviceName",xmlBaseModel.getItems().get(0).get("device_name").toString());


            robotService.addEnvWarning(envWarn);

            String envWarningXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
            byte[] envWarningProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, envWarningXmlString);
            RobotServerHandler.send( envWarningProtocol, robotCode);
            log.info("巡视主机给机器人{}响应了");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ENV_WARN.getCode(), this);
    }

}
