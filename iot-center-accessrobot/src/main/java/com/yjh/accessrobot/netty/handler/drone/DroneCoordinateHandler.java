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
public class DroneCoordinateHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DroneService droneService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到无人机坐标数据了+++++++++++++++++");
        // Deal with drone coordinates data
        String droneCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(droneCode, false)) {
            List<Map<String, String>> droneCoordinateList = new ArrayList<>();
            xmlBaseModel.getItems().forEach(res -> {
                Map<String, String> droneCoordinateMap = new HashMap<>(16);
                String filePath = res.get("file_path").toString();
                droneCoordinateMap.put("patrolDeviceName", res.get("patroldevice_name").toString());
                droneCoordinateMap.put("filePath", filePath);
                droneService.uploadFile(filePath, filePath);
                droneCoordinateMap.put("patrolDeviceCode", droneCode);
                droneCoordinateMap.put("time", res.get("time").toString());
                droneCoordinateMap.put("coordinatePixel", res.get("coordinate_pixel").toString());
                droneCoordinateMap.put("coordinateGeography", res.get("coordinate_geography").toString());
                droneCoordinateList.add(droneCoordinateMap);
            });

            for (int i = 0; i < droneCoordinateList.size(); i++) {
                redisTemplate.opsForHash().putAll("DroneCoordinate:" + droneCode, droneCoordinateList.get(i));
            }

            String coordinateXmlString = PlatformXMLUtil.generateXml2(RobotServerHandler.sendMessageForCommandThree(true, droneCode), PlatformXMLUtil.DRONEROOTNAME);
            byte[] coordinateProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, coordinateXmlString);
            RobotServerHandler.send(coordinateProtocol, droneCode);
            log.info("巡视主机给无人机{}响应了", droneCode);
            // 国网要求
            droneService.upToCruise(xmlBaseModel);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.DRONE_COORDINATE.getCode(), this);
    }
}
