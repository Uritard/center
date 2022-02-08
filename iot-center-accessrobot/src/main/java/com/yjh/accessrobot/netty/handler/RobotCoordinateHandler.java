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
public class RobotCoordinateHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到机器人坐标数据了+++++++++++++++++");
        // Deal with robot coordinates data
        String robotCode = xmlBaseModel.getSendCode();
        if (Constant.robotRegisterFlag.getOrDefault(robotCode, false)) {
            List<Map<String, String>> robotCoordinateList = new ArrayList<>();
            xmlBaseModel.getItems().forEach(res -> {
                Map<String, String> robotCoordinateMap = new HashMap<>(16);
                String filePath = res.get("file_path").toString();
                robotCoordinateMap.put("robotName", res.get("robot_name").toString());
                robotCoordinateMap.put("filePath", filePath);
                robotService.uploadFile(filePath, filePath);
                robotCoordinateMap.put("robotCode",robotCode);
                robotCoordinateMap.put("time", res.get("time").toString());
                robotCoordinateMap.put("coordinatePixel", res.get("coordinate_pixel").toString());
                robotCoordinateMap.put("coordinateGeography", res.get("coordinate_geography").toString());
                robotCoordinateList.add(robotCoordinateMap);
            });

            for (int i = 0; i < robotCoordinateList.size(); i++) {
                redisTemplate.opsForHash().putAll("RobotCoordinate:" + robotCode, robotCoordinateList.get(i));
            }

            String coordinateXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
            byte[] coordinateProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, coordinateXmlString);
            RobotServerHandler.send(coordinateProtocol, robotCode);
            log.info("巡视主机给机器人{}响应了", robotCode);
            // 国网要求
            robotService.upToCruise(xmlBaseModel);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_COORDINATE.getCode(), this);
    }
}
