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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        log.info("+++++++++++++++++巡视主机收到机器人状态数据了+++++++++++++++++");
        // Deal with robot status data
        String robotCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(robotCode, false)){
            List<Map<String, String>> robotStatusList = new ArrayList<>();
            xmlBaseModel.getItems().forEach(res -> {
                Map<String, String> robotStatusMap = new HashMap<>(16);
                // 2022过检 robot_name修改为patroldevice_name
                robotStatusMap.put("patrolDeviceName", res.get("patroldevice_name").toString());
                robotStatusMap.put("patrolDeviceCode", res.get("patroldevice_code").toString());
                robotStatusMap.put("robotCode",robotCode);
                robotStatusMap.put("time", res.get("time").toString());
                robotStatusMap.put("type", res.get("type").toString());
                robotStatusMap.put("value", res.get("value").toString());
                robotStatusMap.put("valueUnit", res.get("value_unit").toString());
                robotStatusMap.put("unit", res.get("unit").toString());
                robotStatusList.add(robotStatusMap);

                // 国网要求
                robotService.upToCruise(xmlBaseModel);
            });
            log.info("机器人状态数据是：" + robotStatusList);

            for (int i = 0; i < robotStatusList.size(); i++) {
                redisTemplate.opsForHash().putAll("RobotStatus:" + robotCode + ":" + robotStatusList.get(i).get("type"), robotStatusList.get(i));
            }

            String statusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
            byte[] statusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, statusXmlString);
            RobotServerHandler.send(statusProtocol, robotCode);
            log.info("巡视主机给机器人{}响应了", robotCode);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_STATUS.getCode(), this);
    }
}
