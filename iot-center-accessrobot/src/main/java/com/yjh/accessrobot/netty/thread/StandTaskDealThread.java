package com.yjh.accessrobot.netty.thread;

import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.module.command.dao.TRobotInfoDao;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author YC
 * @date 2021/1/6 13:24
 */
@lombok.extern.slf4j.Slf4j
public class StandTaskDealThread implements Runnable {
    private RedisTemplate redisTemplate;
    private String taskCode;
    private String robotCode;

    public StandTaskDealThread(RedisTemplate redisTemplate, String taskCode, String robotCode) {
        this.taskCode = taskCode;
        this.redisTemplate = redisTemplate;
        this.robotCode = robotCode;
    }
    @Override
    public void run() {
        try {
            //超期时间
            Map<String,Object> mapForTaskAreTime  = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
            Float tasksAreTime = Float.valueOf((String) mapForTaskAreTime.get("content"));

            TimeUnit.HOURS.sleep(tasksAreTime.longValue());

            Map<String, Object> redisInfoMap = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskCode);
            Integer taskState = Integer.valueOf(redisInfoMap.get("taskState").toString());

            if (1 != taskState){
                StaticContextAccessor.getBean(TRobotInfoDao.class).updateStandTaskStatus(taskCode,244);
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
