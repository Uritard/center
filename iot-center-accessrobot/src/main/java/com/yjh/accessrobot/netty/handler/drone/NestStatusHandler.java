package com.yjh.accessrobot.netty.handler.drone;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.DroneService;
import com.yjh.accessrobot.netty.entiy.DroneHandlerEnum;
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
public class NestStatusHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DroneService droneService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到无人机机巢状态数据了+++++++++++++++++");
        // Deal with drone status data
        String droneCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(droneCode, false)) {
            List<Map<String, String>> droneStatusList = new ArrayList<>();
            xmlBaseModel.getItems().forEach(res -> {
                Map<String, String> droneStatusMap = new HashMap<>(16);
                droneStatusMap.put("nestName", res.get("nest_name").toString());
                droneStatusMap.put("nestCode", res.get("nest_code").toString());
                droneStatusMap.put("time", res.get("time").toString());
                droneStatusMap.put("type", res.get("type").toString());
                droneStatusMap.put("value", res.get("value").toString());
                droneStatusMap.put("valueUnit", res.get("value_unit").toString());
                droneStatusMap.put("unit", res.get("unit").toString());
                droneStatusList.add(droneStatusMap);

                // 国网要求
                droneService.upToCruise(xmlBaseModel);
            });
            log.info("无人机机巢状态数据是：" + droneStatusList);

            for (int i = 0; i < droneStatusList.size(); i++) {
                redisTemplate.opsForHash().putAll("NestStatus:" + droneCode + ":" + droneStatusList.get(i).get("type"), droneStatusList.get(i));
            }

            String statusXmlString = PlatformXMLUtil.generateXml2(RobotServerHandler.sendMessageForCommandThree(true, droneCode), PlatformXMLUtil.DRONEROOTNAME);
            byte[] statusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, statusXmlString);
            RobotServerHandler.send(statusProtocol, droneCode);
            log.info("巡视主机给无人机{}响应了", droneCode);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.DRONE_STATUS.getCode(), this);
    }
}
