package com.yjh.accessrobot.netty.handler;

import com.google.common.collect.Maps;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.AlarmShield;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;


/**
 * 环境设备告警数据处理程序
 */

@Service
@Slf4j
public class EnvWarningHandler implements MessageHandlerStrategy, InitializingBean {
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("巡视主机收到机器人站端的环境设备告警数据");
        String sendCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(sendCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(sendCode, false))) {
            log.error("下级唯一标识未注册或未连接");
            throw new RuntimeException("下级唯一标识未注册或未连接");
        }

        String envWarningXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, sendCode));
        byte[] envWarningProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, envWarningXmlString);
        RobotServerHandler.send(envWarningProtocol, sendCode);
        log.info("巡视主机给机器人{}响应了", sendCode);

        Map<String, String> envWarn = new HashMap<>(13);
        String robotCode = robotService.selectRobotOrEdgeRobot(xmlBaseModel, sendCode);
        Map<String, Object> envWarnMap = xmlBaseModel.getItems().get(0);
        envWarn.put("patrolDeviceName", MapUtils.getString(envWarnMap, "patroldevice_name"));
        envWarn.put("patrolDeviceCode", MapUtils.getString(envWarnMap, "patroldevice_code"));
        envWarn.put("robotCode", robotCode);
        envWarn.put("time", MapUtils.getString(envWarnMap, "time"));
        String value = MapUtils.getString(envWarnMap, "value");
        String valueType = MapUtils.getString(envWarnMap, "value_type");
        envWarn.put("value", value);
        envWarn.put("valueType", valueType);
        envWarn.put("type", MapUtils.getString(envWarnMap, "type"));
        envWarn.put("valueUnit", MapUtils.getString(envWarnMap, "value_unit"));
        envWarn.put("unit", MapUtils.getString(envWarnMap, "unit"));
        envWarn.put("sn", MapUtils.getString(envWarnMap, "sn"));
        envWarn.put("device_num", MapUtils.getString(envWarnMap, "device_num"));
        envWarn.put("type_device_num", MapUtils.getString(envWarnMap, "type_device_num"));
        envWarn.put("alarmTime", MapUtils.getString(envWarnMap, "alarm_time"));
        envWarn.put("deviceName", MapUtils.getString(envWarnMap, "device_name"));
        envWarn.put("deleteFlag", MapUtils.getString(envWarnMap, "delete_flag"));
        envWarn.put("deleteTime", MapUtils.getString(envWarnMap, "delete_time"));
        Long robotId = robotService.selectRobotIdByCode(robotCode);

        //告警屏蔽处理
        AtomicReference<Boolean> isWarn = new AtomicReference<>(true);
        robotService.needPopAlarm(robotId, value, isWarn);
        if (isWarn.get()) {
            try {
                StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.ENV_ALARM_PROCESS, envWarn, Result.class);
            } catch (Exception e) {
                log.error("调用platform出错：{}", e.getMessage(), e);
            }
        }

        if (Constant.upEnvDevice() && Constant.upSystemFlag()) {
            log.info("向上级推送环控告警" + xmlBaseModel.getItems());
            Map<String, List<XMLBaseModel>> map = Maps.newHashMap();
            map.put("list", Collections.singletonList(xmlBaseModel));
            Constant.mapToOtherServer(map, Constant.TCP_URL);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ENV_WARN.getCode(), this);
    }

}
