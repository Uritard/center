package com.yjh.accessrobot.netty.handler.drone;

import com.yjh.accessrobot.common.Constant;
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

import java.util.List;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class DroneHeartBeatHandler implements DroneMessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DroneService droneService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler droneServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到心跳指令了+++++++++++++++++");
        String droneCode = xmlBaseModel.getSendCode();
        Constant.robotRemoveCounts.put(droneCode, 0);
        Map<String, String> droneStatusMap = redisTemplate.opsForHash().entries("DroneStatus:" + droneCode + ":2");

        Map<String, String> allDroneCodeMap = redisTemplate.opsForHash().entries("AllDroneCode");
        if (Constant.robotRegisterFlag.getOrDefault(droneCode, false) && allDroneCodeMap.containsValue(droneCode)) {
            log.info("缓存有,发送心跳响应");
            droneServerHandler.heartBeatSuccessAfter(ctx, droneCode, sendSessionId, droneStatusMap);
        } else {
            List<String> droneCodeList = droneService.selectAllDroneCode();
            if (Constant.robotRegisterFlag.getOrDefault(droneCode, false) && droneCodeList.contains(droneCode)) {
                log.info("缓存无,表中有,发送心跳相应");
                droneServerHandler.heartBeatSuccessAfter(ctx, droneCode, sendSessionId, droneStatusMap);
            } else {
                log.info("缓存无,表中无,断开连接");
                droneServerHandler.heartBeatFailAfter(droneCode, droneStatusMap);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        DroneMessageHandlerStrategyFactory.register(DroneHandlerEnum.HEART_BEAT.getCode(), this);
    }
}
