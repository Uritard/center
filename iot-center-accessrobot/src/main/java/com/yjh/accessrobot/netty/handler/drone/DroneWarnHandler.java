package com.yjh.accessrobot.netty.handler.drone;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.DroneService;
import com.yjh.accessrobot.netty.entiy.DroneHandlerEnum;
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
public class DroneWarnHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Autowired
    private DroneService droneService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到无人机本体异常告警数据了+++++++++++++++++");
        // Deal with drone alarm data
        String droneCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(droneCode, false)) {
            Map<String, String> droneAlarmMap = new HashMap<>(8);
            droneAlarmMap.put("droneName", xmlBaseModel.getItems().get(0).get("drone_name").toString());
            droneAlarmMap.put("droneCode", droneCode);
            droneAlarmMap.put("time", xmlBaseModel.getItems().get(0).get("time").toString());
            droneAlarmMap.put("content", xmlBaseModel.getItems().get(0).get("content").toString());

            // Start alarmResultDealThread
            RobotWarnThread alarmResultDealThread = new RobotWarnThread(droneAlarmMap);
            TaskExecutePool.getInstance().execute(alarmResultDealThread);

            String alarmXmlString = PlatformXMLUtil.generateXml2(RobotServerHandler.sendMessageForCommandThree(true, droneCode), PlatformXMLUtil.DRONEROOTNAME);
            byte[] alarmProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, alarmXmlString);
            RobotServerHandler.send(alarmProtocol, droneCode);
            log.info("巡视主机给无人机{}响应了", droneCode);
        }

        // 国网要求
        droneService.upToCruise(xmlBaseModel);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.DRONE_WARN.getCode(), this);
    }
}
