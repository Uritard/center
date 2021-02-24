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
    private long sendSessionId;
    private long receiveSessionId;


    public HeartBreakDealThread(RobotServerHandler robotServerHandler, String robotCode,RedisTemplate redisTemplate,boolean isThreadStart,long sendSessionId,long receiveSessionId) {
        this.robotServerHandler = robotServerHandler;
        this.robotCode = robotCode;
        this.redisTemplate = redisTemplate;
        this.isThreadStart = isThreadStart;
        this.sendSessionId = sendSessionId;
        this.receiveSessionId = receiveSessionId;
    }

    @Override
    public void run() {
        try {
            while (isThreadStart){

                Map<String, String> heartbeatIntervalMap = redisTemplate.opsForHash().entries("t_sys_param:heartbeatInterval");
                long sleepTime = Long.valueOf(heartbeatIntervalMap.get("content")) * 1000;
                Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");
                Thread.sleep(sleepTime);
                log.info("Thread Wait"+sleepTime+"ms......");
                robotServerHandler.procSend(robotCode, robotStatusMap,sendSessionId,receiveSessionId);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
