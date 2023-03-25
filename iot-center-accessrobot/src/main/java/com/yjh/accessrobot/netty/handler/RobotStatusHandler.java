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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class RobotStatusHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++收到下级的状态数据了+++++++++++++++++");
        // Deal with robot status data
        String stationCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
        xmlBaseModel.setCode(stationCode);
        String sendCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(sendCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(sendCode, false))) {
            log.error("下级唯一标识未注册或未连接");
            throw new RuntimeException("下级唯一标识未注册或未连接");
        }

        boolean changeOnline = false;
        String robotCode = robotService.selectRobotOrEdgeRobot(xmlBaseModel, sendCode);
        String onlineStatus = String.valueOf(redisTemplate.opsForValue().get("onlineStatus:"+ robotCode));
        List<Map<String, String>> robotStatusList = new ArrayList<>();
        for (Map<String, Object> res : xmlBaseModel.getItems()) {
            Map<String, String> robotStatusMap = new HashMap<>(16);
            // 2022过检 robot_name修改为patroldevice_name
            robotStatusMap.put("patrolDeviceName", String.valueOf(res.get("patroldevice_name")));
            robotStatusMap.put("patrolDeviceCode", String.valueOf(res.get("patroldevice_code")));
            robotStatusMap.put("robotCode", robotCode);
            robotStatusMap.put("time", res.get("time").toString());
            robotStatusMap.put("type", res.get("type").toString());
            robotStatusMap.put("value", res.get("value").toString());
            robotStatusMap.put("valueUnit", res.containsKey("value_unit") ? String.valueOf(res.get("value_unit")) : "");
            robotStatusMap.put("unit", res.containsKey("unit") ? String.valueOf(res.get("unit")) : "");
            robotStatusList.add(robotStatusMap);
            if ("2".equals(res.get("type").toString())) {
                if ("0".equals(res.get("value").toString())) {
                    robotService.updateRobotInfo(robotCode, "在线");
                    if (!"0".equals(onlineStatus)) {
                        redisTemplate.opsForValue().set("onlineStatus:" + robotCode, "0");
                        changeOnline = !changeOnline;
                        onlineStatus = "0";
                    }
                } else if ("1".equals(res.get("value").toString())) {
                    robotService.updateRobotInfo(robotCode, "离线");
                    if (!"1".equals(onlineStatus)) {
                        redisTemplate.opsForValue().set("onlineStatus:" + robotCode, "1");
                        onlineStatus = "1";
                        changeOnline = !changeOnline;
                    }
                }
            }
            //若设备故障报警，记录设备状态及正常运行时长
            if ("21".equals(res.get("type").toString())) {
                robotService.changeStatistic(String.valueOf(res.get("patroldevice_code")), res.get("value").toString(), robotCode);
            }
        }
        log.info("下级的状态数据是：" + robotStatusList);
        if (!changeOnline && "1".equals(onlineStatus)) {
            // 给下级响应
            String statusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThreeFlase(sendCode));
            byte[] statusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, statusXmlString);
            RobotServerHandler.send(statusProtocol, sendCode);
            log.info("本级系统给下级{}响应了", sendCode);

            return;
        } else if (changeOnline && "1".equals(onlineStatus)){
            for (int i = 0; i < robotStatusList.size(); i++) {
                if ("2".equals(robotStatusList.get(i).get("type"))) {
                    String robotStatus = "RobotStatus:" + robotCode + ":" + robotStatusList.get(i).get("type");
                    redisTemplate.opsForHash().putAll(robotStatus, robotStatusList.get(i));
                    redisTemplate.expire(robotStatus, 7, TimeUnit.DAYS);
                }
            }
        }
        else {
            for (int i = 0; i < robotStatusList.size(); i++) {
                String robotStatus = "RobotStatus:" + robotCode + ":" + robotStatusList.get(i).get("type");
                redisTemplate.opsForHash().putAll(robotStatus, robotStatusList.get(i));
                redisTemplate.expire(robotStatus, 7, TimeUnit.DAYS);
            }
        }

        // 给下级响应
        String statusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, sendCode));
        byte[] statusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, statusXmlString);
        RobotServerHandler.send(statusProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        // 国网要求
        robotService.upToCruise(xmlBaseModel);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_STATUS.getCode(), this);
    }
}
