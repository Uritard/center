package com.yjh.accessrobot.netty.handler;

import com.yjh.accessrobot.common.Constant;
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

import java.util.List;
import java.util.Map;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class HeartBeatHandler implements MessageHandlerStrategy, InitializingBean {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到心跳指令了+++++++++++++++++");
        String robotCode = xmlBaseModel.getSendCode();
        Constant.robotRemoveCounts.put(robotCode, 0);
        Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");

        Map<String, String> allRobotCodeMap = redisTemplate.opsForHash().entries("AllRobotCode");
        log.info("Registered robot is：{}", Constant.robotRegisterFlag);
        if (Constant.robotRegisterFlag.getOrDefault(robotCode, false) &&  allRobotCodeMap.containsValue(robotCode)){
            log.info("缓存有,机器人{}发送心跳响应", robotCode);
            robotServerHandler.heartBeatSuccessAfter( ctx, robotCode, sendSessionId, robotStatusMap);
        }else {
            List<String> robotCodeList = robotService.selectAllRobotCode();
            if (Constant.robotRegisterFlag.getOrDefault(robotCode, false) && robotCodeList.contains(robotCode)) {
                log.info("缓存无,表中有,机器人{}发送心跳相应", robotCode);
                robotServerHandler.heartBeatSuccessAfter(ctx,robotCode, sendSessionId, robotStatusMap);
            } else {
                log.info("缓存无,表中无,机器人{}断开连接", robotCode);
                robotServerHandler.heartBeatFailAfter(robotCode, robotStatusMap);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.HEART_BEAT.getCode(), this);
    }
}
