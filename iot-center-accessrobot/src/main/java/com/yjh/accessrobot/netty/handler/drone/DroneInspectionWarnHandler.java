package com.yjh.accessrobot.netty.handler.drone;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.netty.entiy.DroneHandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.netty.thread.RobotInspectionWarnThread;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/16
 */
@Slf4j
@Service
public class DroneInspectionWarnHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Value("${other.webSocketUrl}")
    private String websocketUrl;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到无人机设备测点告警了+++++++++++++++++");
        // Deal with drone warn data
        Map<String, String> warnResultMap = new HashMap<>(16);
        String droneCode = xmlBaseModel.getSendCode();
        warnResultMap.put("droneCode", droneCode);
        warnResultMap.put("taskName", xmlBaseModel.getItems().get(0).get("task_name").toString());
        warnResultMap.put("taskCode", xmlBaseModel.getItems().get(0).get("task_code").toString());
        warnResultMap.put("deviceName", xmlBaseModel.getItems().get(0).get("device_name").toString());
        warnResultMap.put("deviceId", xmlBaseModel.getItems().get(0).get("device_id").toString());
        warnResultMap.put("alarmLevel", xmlBaseModel.getItems().get(0).get("alarm_level").toString());
        warnResultMap.put("alarmType", xmlBaseModel.getItems().get(0).get("alarm_type").toString());
        warnResultMap.put("recognitionType", xmlBaseModel.getItems().get(0).get("recognition_type").toString());
        warnResultMap.put("value", xmlBaseModel.getItems().get(0).get("value").toString());
        warnResultMap.put("valueUnit", xmlBaseModel.getItems().get(0).get("value_unit").toString());
        warnResultMap.put("unit", xmlBaseModel.getItems().get(0).get("unit").toString());
        warnResultMap.put("time", xmlBaseModel.getItems().get(0).get("time").toString());
        warnResultMap.put("taskPatrolledId", xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());
        warnResultMap.put("content", xmlBaseModel.getItems().get(0).get("content").toString());

        log.info("无人机设备测点告警数据是：{}", warnResultMap);
        RobotInspectionWarnThread droneWarnThread = new RobotInspectionWarnThread(warnResultMap, redisTemplate, websocketUrl);
        TaskExecutePool.getInstance().execute(droneWarnThread);

        String alarmXmlString = PlatformXMLUtil.generateXml2(RobotServerHandler.sendMessageForCommandThree(true, droneCode), PlatformXMLUtil.DRONEROOTNAME);
        byte[] alarmProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, alarmXmlString);
        RobotServerHandler.send(alarmProtocol, droneCode);
        log.info("巡视主机给无人机{}响应了", droneCode);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.DRONE_INSPECTION_WARN.getCode(), this);
    }
}
