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
public class DroneRunDataHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DroneService droneService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到无人机运行数据了+++++++++++++++++");
        // Deal with drone operation data
        String droneCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(droneCode, false)) {
            List<Map<String, String>> droneOperationList = new ArrayList<>();
            xmlBaseModel.getItems().forEach(res -> {
                Map<String, String> droneOperationMap = new HashMap<>(16);
                droneOperationMap.put("patrolDeviceName", res.get("patroldevice_name").toString());
                droneOperationMap.put("patrolDeviceCode", droneCode);
                droneOperationMap.put("time", res.get("time").toString());
                droneOperationMap.put("type", res.get("type").toString());
                droneOperationMap.put("value", res.get("value").toString());
                droneOperationMap.put("valueUnit", res.get("value_unit").toString());
                droneOperationMap.put("unit", res.get("unit").toString());
                droneOperationList.add(droneOperationMap);
            });

            for (int i = 0; i < droneOperationList.size(); i++) {
                redisTemplate.opsForHash().putAll("RobotOperation:" + droneCode + ":" + droneOperationList.get(i).get("type"), droneOperationList.get(i));
            }

            String operationXmlString = PlatformXMLUtil.generateXml2(RobotServerHandler.sendMessageForCommandThree(true, droneCode), PlatformXMLUtil.DRONEROOTNAME);
            byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, operationXmlString);
            RobotServerHandler.send(operationProtocol, droneCode);
            log.info("巡视主机给无人机{}响应了", droneCode);
            // 国网要求
            droneService.upToCruise(xmlBaseModel);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.DRONE_RUN_DATA.getCode(), this);
    }
}
