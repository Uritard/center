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

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class RobotRunDataHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;
    @Value("${stationCode}")
    private String stationCode;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到机器人运行数据了+++++++++++++++++");
        // Deal with robot operation data
        if(StringUtils.isBlank(xmlBaseModel.getCode())){
            xmlBaseModel.setCode(stationCode);
        }
        String robotCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(robotCode, false)) {
            List<Map<String, String>> robotOperationList = new ArrayList<>();
            xmlBaseModel.getItems().forEach(res -> {
                Map<String, String> robotOperationMap = new HashMap<>(16);
                // 2022过检 修改robot_name为patroldevice_name
                robotOperationMap.put("patrolDeviceName", String.valueOf(res.get("patroldevice_name")));
                robotOperationMap.put("patrolDeviceCode", String.valueOf(res.get("patroldevice_code")));
                robotOperationMap.put("robotCode", robotCode);
                robotOperationMap.put("time", String.valueOf(res.get("time")));
                robotOperationMap.put("type", String.valueOf(res.get("type")));
                robotOperationMap.put("value", String.valueOf(res.get("value")));
                robotOperationMap.put("valueUnit", String.valueOf(res.get("value_unit")));
                robotOperationMap.put("unit", String.valueOf(res.get("unit")));
                robotOperationList.add(robotOperationMap);
            });

            for (int i = 0; i < robotOperationList.size(); i++) {
                redisTemplate.opsForHash().putAll("RobotOperation:" + robotCode + ":" + robotOperationList.get(i).get("type"), robotOperationList.get(i));
            }

            String operationXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
            byte[] operationProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, operationXmlString);
            RobotServerHandler.send( operationProtocol, robotCode);
            log.info("巡视主机给机器人{}响应了", robotCode);
            // 国网要求
            robotService.upToCruise(xmlBaseModel);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_RUN_DATA.getCode(), this);
    }
}
