package com.yjh.accessrobot.netty.server;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.service.RobotService;
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
                Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:"+robotCode+":2");

                long sleepTime = Long.valueOf(heartbeatIntervalMap.get("content")) * 1000;
                Thread.sleep(sleepTime);
                log.info("线程等待了"+sleepTime+"ms了");
                Constant.heartNum.addAndGet(1);
                int res = Constant.heartNum.intValue();
                if (res > 3){
                    robotServerHandler.procSend(robotCode);
                    StaticContextAccessor.getBean(RobotService.class).updateRobotInfo(robotCode,"离线");//更新机器人表信息
                    robotStatusMap.put("value","1");//异常
                    redisTemplate.opsForHash().putAll("RobotStatus:"+robotCode+":2",robotStatusMap);//更新缓存机器人的网络状态
                    isThreadStart = false;
                    break;
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    /*public synchronized int processMethod(long sleepTime) throws InterruptedException{
        Thread.sleep(sleepTime);
        log.info("线程等待了"+sleepTime+"ms了");
        Constant.heartNum++;
        int res = Constant.heartNum;
        log.info("判断条件是==="+res);
        return res;
    }*/
}
