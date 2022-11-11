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
        if (StringUtils.isEmpty(robotCode)) {
            log.error("机器人/无人机编码为空");
            throw new RuntimeException("机器人/无人机编码为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(robotCode, false))) {
            log.error("机器人/无人机未注册或未连接");
            throw new RuntimeException("机器人/无人机未注册或未连接");
        }

        // 给机器人响应
        String coordinateXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] coordinateProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, coordinateXmlString);
        RobotServerHandler.send(coordinateProtocol, robotCode);
        log.info("巡视主机给机器人/无人机{}响应了", robotCode);

        List<Map<String, String>> robotCoordinateList = new ArrayList<>();
        xmlBaseModel.getItems().forEach(res -> {
            Map<String, String> robotCoordinateMap = new HashMap<>(16);
            if (res.containsKey("file_path")){
                String filePath = String.valueOf(res.get("file_path"));
                robotCoordinateMap.put("filePath", filePath);
                robotService.uploadFile(filePath, filePath);
            }else {
                robotCoordinateMap.put("filePath", "");
            }
            // 2022过检 robot_name -> patroldevice_name
            robotCoordinateMap.put("patrolDeviceName", String.valueOf(res.get("patroldevice_name")));
            robotCoordinateMap.put("patrolDeviceCode", String.valueOf(res.get("patroldevice_code")));
            robotCoordinateMap.put("robotCode",robotCode);
            robotCoordinateMap.put("time", String.valueOf(res.get("time")));
            robotCoordinateMap.put("coordinatePixel", String.valueOf(res.get("coordinate_pixel")));
            robotCoordinateMap.put("coordinateGeography", String.valueOf(res.get("coordinate_geography")));
            robotCoordinateList.add(robotCoordinateMap);
        });

        for (int i = 0; i < robotCoordinateList.size(); i++) {
            String robotCoordinate = "RobotCoordinate:" + robotCode;
            redisTemplate.opsForHash().putAll(robotCoordinate, robotCoordinateList.get(i));
            redisTemplate.expire(robotCoordinate, 7, TimeUnit.DAYS);
        }

        // 国网要求
        robotService.upToCruise(xmlBaseModel);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_COORDINATE.getCode(), this);
    }
}
