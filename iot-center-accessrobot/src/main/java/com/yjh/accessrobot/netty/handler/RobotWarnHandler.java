package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.netty.thread.RobotWarnThread;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class RobotWarnHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到机器人本体异常告警数据了+++++++++++++++++");
        // Deal with robot alarm data
        String robotCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(robotCode, false)) {
            Map<String, String> robotAlarmMap = new HashMap<>(8);
            // 2022过检 robot_name -> patroldevice_name
            robotAlarmMap.put("patrolDeviceName", String.valueOf(xmlBaseModel.getItems().get(0).get("patroldevice_name")));
            robotAlarmMap.put("patrolDeviceCode", String.valueOf(xmlBaseModel.getItems().get(0).get("patroldevice_code")));
            robotAlarmMap.put("robotCode", robotCode);
            robotAlarmMap.put("time", xmlBaseModel.getItems().get(0).get("time").toString());
            robotAlarmMap.put("content", xmlBaseModel.getItems().get(0).get("content").toString());

            // Start alarmResultDealThread
            RobotWarnThread alarmResultDealThread = new RobotWarnThread(robotAlarmMap);
            TaskExecutePool.getInstance().execute(alarmResultDealThread);

            String alarmXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
            byte[] alarmProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, alarmXmlString);
            RobotServerHandler.send(alarmProtocol, robotCode);
            log.info("巡视主机给机器人{}响应了", robotCode);
        }

        // 国网要求
        robotService.upToCruise(xmlBaseModel);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_WARN.getCode(), this);
    }
}
