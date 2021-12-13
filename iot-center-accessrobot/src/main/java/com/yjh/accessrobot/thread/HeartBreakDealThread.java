package com.yjh.accessrobot.thread;

import com.yjh.accessrobot.netty.server.RobotServerHandler;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author YC
 * @date 2021/1/6 13:24
 */
@lombok.extern.slf4j.Slf4j
public class HeartBreakDealThread implements Runnable {

    private RobotServerHandler robotServerHandler;
    private String robotCode;
    private RedisTemplate redisTemplate;
    private volatile boolean isThreadStart;


    public HeartBreakDealThread(RobotServerHandler robotServerHandler, String robotCode,RedisTemplate redisTemplate,boolean isThreadStart) {
        this.robotServerHandler = robotServerHandler;
        this.robotCode = robotCode;
        this.redisTemplate = redisTemplate;
        this.isThreadStart = isThreadStart;
    }

    @Override
    public void run() {
        try {
            while (isThreadStart){

                Map<String, String> heartbeatIntervalMap = redisTemplate.opsForHash().entries("t_sys_param:heartbeatInterval");
                long sleepTime = Long.valueOf(heartbeatIntervalMap.get("content"));
                Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");
                Thread.sleep(sleepTime);
                TimeUnit.SECONDS.sleep(Long.valueOf(heartbeatIntervalMap.get("content")));
                log.info("Thread wait "+sleepTime+" s......");
                robotServerHandler.procSend(robotCode, robotStatusMap);
                if (!robotServerHandler.getIsThreadStart()) {
                    isThreadStart = false;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
