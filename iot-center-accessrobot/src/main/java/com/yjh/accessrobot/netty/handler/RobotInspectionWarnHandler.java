package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.RobotPatrolTaskAlarm;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/16
 */
@Slf4j
@Service
public class RobotInspectionWarnHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++收到下级的设备测点告警了+++++++++++++++++");
        // Deal with robot warn data
        String robotCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(robotCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(robotCode, false))) {
            log.error("下级唯一标识未注册或未连接");
            throw new RuntimeException("下级唯一标识未注册或未连接");
        }

        // 给下级响应
        String alarmXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] alarmProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, alarmXmlString);
        RobotServerHandler.send( alarmProtocol, robotCode);
        log.info("本级系统给下级{}响应了", robotCode);

        // 处理数据
        List<RobotPatrolTaskAlarm> alarmList = new ArrayList<>();
        for(Map<String, Object> item : xmlBaseModel.getItems()){
            RobotPatrolTaskAlarm robotPatrolTaskAlarm = new RobotPatrolTaskAlarm();
            robotPatrolTaskAlarm.setRobotCode(robotCode);
            robotPatrolTaskAlarm.setTaskName(String.valueOf(item.get("task_name")));
            robotPatrolTaskAlarm.setTaskCode(String.valueOf(item.get("task_code")));
            robotPatrolTaskAlarm.setDeviceName(String.valueOf(item.get("device_name")));
            robotPatrolTaskAlarm.setDeviceId(String.valueOf(item.get("device_id")));
            robotPatrolTaskAlarm.setAlarmLevel(String.valueOf(item.get("alarm_level")));
            robotPatrolTaskAlarm.setAlarmType(String.valueOf(item.get("alarm_type")));
            robotPatrolTaskAlarm.setRecognitionType(String.valueOf(item.get("recognition_type")));
            robotPatrolTaskAlarm.setValue(String.valueOf(item.get("value")));
            robotPatrolTaskAlarm.setValueUnit(String.valueOf(item.get("value_unit")));
            robotPatrolTaskAlarm.setUnit(String.valueOf(item.get("unit")));
            robotPatrolTaskAlarm.setTime(String.valueOf(item.get("time")));
            robotPatrolTaskAlarm.setTaskPatrolledId(String.valueOf(item.get("task_patrolled_id")));
            robotPatrolTaskAlarm.setContent(String.valueOf(item.get("content")));
            robotPatrolTaskAlarm.setDefectType(String.valueOf(item.get("defect_type")));
            alarmList.add(robotPatrolTaskAlarm);
        }

        log.info("The alarmList to platform is=={}", alarmList);

        try {
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.POINT_ALARM_PROCESS, alarmList, Result.class);
        }catch (Exception e){
            log.error("调用platform出错：{}", e.getMessage());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_INSPECTION_WARN.getCode(), this);
    }
}
