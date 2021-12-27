package com.yjh.accessrobot.netty.thread;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.yjh.accessrobot.common.Constant.robotRemoveCounts;

/**
 * @author YC
 * @date 2021/1/6 13:24
 */
@lombok.extern.slf4j.Slf4j
public class HeartBreakDealThread implements Runnable {

    private RobotServerHandler robotServerHandler;
    private String robotCode;
    private RedisTemplate redisTemplate;
    private String heartBeatInterval;

    public HeartBreakDealThread(RobotServerHandler robotServerHandler, String robotCode,RedisTemplate redisTemplate,String heartBeatInterval) {
        this.robotServerHandler = robotServerHandler;
        this.robotCode = robotCode;
        this.redisTemplate = redisTemplate;
        this.heartBeatInterval = heartBeatInterval;
    }

    @Override
    public void run() {
        try {
            while (true){

                long sleepTime = Long.parseLong(heartBeatInterval);
                Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
                TimeUnit.SECONDS.sleep(sleepTime);
                log.info("Thread wait "+sleepTime+" s......");
                Integer heartNum = robotRemoveCounts.getOrDefault(robotCode, 0);
                heartNum++;
                log.info("robotCode:{},heartNum:{}", robotCode, heartNum);
                robotRemoveCounts.put(robotCode,heartNum);
                if (heartNum > 3){
                    robotServerHandler.heartBeatFailAfter(robotCode,robotStatusMap);
                }
                if (!Constant.robotThreadFlag.getOrDefault(robotCode, true)) {
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
