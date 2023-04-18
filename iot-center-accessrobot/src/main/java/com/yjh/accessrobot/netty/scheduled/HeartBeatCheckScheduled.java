package com.yjh.accessrobot.netty.scheduled;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.netty.server.StateGridAHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.Map;

/**
 * @author hyh
 * @since 2022/3/3
 *  定时进行心跳检测
 **/
@Component("heartBeatCheckScheduled")
@Slf4j
public class HeartBeatCheckScheduled {

    @Resource
    private RedisTemplate redisTemplate;

    private RobotServerHandler robotServerHandler;

    @Resource
    private RobotService robotService;

    @PostConstruct
    public void init(){
        StateGridAHandlerImpl robotServerHandler = new StateGridAHandlerImpl();
        robotServerHandler.setRedisTemplate(redisTemplate);
        robotServerHandler.setRobotService(robotService);
        this.robotServerHandler = robotServerHandler;
    }

    @Scheduled(fixedDelayString = "#{@getIntervalValue}", initialDelayString = "#{@getIntervalValue}")
    public void checkHeartBeat() {
        try {
            Constant.robotRemoveCounts.forEach((robotCode, heartNum) -> {
                Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
                heartNum++;
                log.info("robotCode:{},heartNum:{}", robotCode, heartNum);
                Constant.robotRemoveCounts.put(robotCode, heartNum);
                if (heartNum > 3) {
                    robotServerHandler.heartBeatFailAfter(robotCode, robotStatusMap);
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
