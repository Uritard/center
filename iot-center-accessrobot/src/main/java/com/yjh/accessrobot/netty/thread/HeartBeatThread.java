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
 * 心跳处理线程
 */
@lombok.extern.slf4j.Slf4j
public class HeartBeatThread implements Runnable {

    private RobotServerHandler robotServerHandler;
    private String robotCode;
    private RedisTemplate redisTemplate;
    private String heartBeatInterval;

    public HeartBeatThread(RobotServerHandler robotServerHandler, String robotCode, RedisTemplate redisTemplate, String heartBeatInterval) {
        this.robotServerHandler = robotServerHandler;
        this.robotCode = robotCode;
        this.redisTemplate = redisTemplate;
        this.heartBeatInterval = heartBeatInterval;
    }

    @Override
    public void run() {
        while (true){
            long sleepTime = Long.parseLong(heartBeatInterval);
            Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":2");
            try {
                TimeUnit.SECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.info("Thread wait " + sleepTime + " s......");
            Integer heartNum = robotRemoveCounts.getOrDefault(robotCode, 0);
            heartNum++;
            log.info("robotCode:{},heartNum:{}", robotCode, heartNum);
            robotRemoveCounts.put(robotCode, heartNum);
            if (heartNum > 3){
                robotServerHandler.heartBeatFailAfter(robotCode, robotStatusMap);
            }
            if (!Constant.robotThreadFlag.getOrDefault(robotCode, true)) {
                break;
            }
        }
    }
}
