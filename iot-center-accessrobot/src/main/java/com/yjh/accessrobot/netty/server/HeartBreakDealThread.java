package com.yjh.accessrobot.netty.server;

import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;

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
                long sleepTime = Long.valueOf(heartbeatIntervalMap.get("content")) * 1000;
                Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");
                Thread.sleep(sleepTime);
                log.info("线程等待了"+sleepTime+"ms了");
                robotServerHandler.procSend(robotCode, robotStatusMap);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
