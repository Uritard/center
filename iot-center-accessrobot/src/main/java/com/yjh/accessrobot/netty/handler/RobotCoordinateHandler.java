package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import com.yjh.accessrobot.module.command.entity.XMLBaseModel;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
    @Autowired
    private TRobotInfoDao tRobotInfoDao;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到机器人坐标数据了+++++++++++++++++");
        // Deal with robot coordinates data

        String sendCode = xmlBaseModel.getSendCode();
        if (StringUtils.isEmpty(sendCode)) {
            log.error("下级唯一标识为空");
            throw new RuntimeException("下级唯一标识为空");
        }
        if (Boolean.FALSE.equals(Constant.robotRegisterFlag.getOrDefault(sendCode, false))) {
            log.error("下级唯一标识未注册或未连接");
            throw new RuntimeException("下级唯一标识未注册或未连接");
        }

        // 给下级响应
        String coordinateXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, sendCode));
        byte[] coordinateProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, coordinateXmlString);
        RobotServerHandler.send(coordinateProtocol, sendCode);
        log.info("本级系统给下级{}响应了", sendCode);

        int num = tRobotInfoDao.checkDroneByRobotCode(sendCode);
        if (num <= 0) {
            // 处理机器人上报的坐标信息
            processRobotCoordinate(xmlBaseModel, sendCode);
        } else {
            // 处理无人机上报的坐标信息
            processDroneCoordinate(xmlBaseModel, sendCode);
        }

        // 国网要求
        //上报 Code 为 变电站编码
        String stationCode = (String)redisTemplate.opsForHash().get("t_sys_param:edgeId","content");
        xmlBaseModel.setCode(stationCode);
        robotService.upToCruise(xmlBaseModel);
    }

    /**
     * 处理机器人坐标信息
     *
     * @param xmlBaseModel xmlBaseModel
     * @param sendCode sendCode
     */
    private void processRobotCoordinate(XMLBaseModel xmlBaseModel, String sendCode) {
        String robotCode = robotService.selectRobotOrEdgeRobot(xmlBaseModel, sendCode);
        List<Map<String, String>> robotCoordinateList = new ArrayList<>();
        xmlBaseModel.getItems().forEach(res -> {
            Map<String, String> robotCoordinateMap = getCoordinateMap(res, robotCode);
            if (res.containsKey("file_path")){
                String filePath = String.valueOf(res.get("file_path"));
                robotCoordinateMap.put("filePath", filePath);
                robotService.uploadFile(filePath, filePath);
            } else {
                robotCoordinateMap.put("filePath", "");
            }

            // 2022过检 robot_name -> patroldevice_name
            robotCoordinateList.add(robotCoordinateMap);
        });

        for (int i = 0; i < robotCoordinateList.size(); i++) {
            String robotCoordinate = "RobotCoordinate:" + robotCode;
            redisTemplate.opsForHash().putAll(robotCoordinate, robotCoordinateList.get(i));
            redisTemplate.expire(robotCoordinate, 7, TimeUnit.DAYS);
        }
    }

    /**
     * 处理无人机坐标信息
     *
     * @param xmlBaseModel xmlBaseModel
     * @param sendCode sendCode
     */
    private void processDroneCoordinate(XMLBaseModel xmlBaseModel, String sendCode) {
        try {
            String robotCode = robotService.selectRobotOrEdgeRobot(xmlBaseModel, sendCode);
            List<Map<String, String>> robotCoordinateList = new ArrayList<>();
            xmlBaseModel.getItems().forEach(res -> {
                Map<String, String> robotCoordinateMap = getCoordinateMap(res, robotCode);
                if (robotCoordinateMap != null && robotCoordinateMap.size() > 0) {
                    robotCoordinateList.add(robotCoordinateMap);
                }
            });

            if (CollectionUtils.isNotEmpty(robotCoordinateList)) {
                // 将无人机坐标信息
                String redisKey = String.format("DroneCoordinate:%s", robotCode);
                redisTemplate.opsForList().leftPushAll(redisKey, robotCoordinateList);
            }
        } catch (Exception e) {
            log.error("处理无人机坐标信息报错， err: ", e);
        }
    }

    private Map<String, String> getCoordinateMap(Map<String,Object> res, String robotCode) {
        Map<String, String> robotCoordinateMap = new HashMap<>(16);
        String coordinateGeography = String.valueOf(res.get("coordinate_geography"));
        if (StringUtils.isNotBlank(coordinateGeography) && !coordinateGeography.contains("0E-7,0E-7")) {
            robotCoordinateMap.put("patrolDeviceName", String.valueOf(res.get("patroldevice_name")));
            robotCoordinateMap.put("patrolDeviceCode", String.valueOf(res.get("patroldevice_code")));
            robotCoordinateMap.put("robotCode",robotCode);
            robotCoordinateMap.put("time", String.valueOf(res.get("time")));
            robotCoordinateMap.put("coordinatePixel", String.valueOf(res.get("coordinate_pixel")));
            robotCoordinateMap.put("coordinateGeography", coordinateGeography);
        }

        return robotCoordinateMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_COORDINATE.getCode(), this);
    }
}
