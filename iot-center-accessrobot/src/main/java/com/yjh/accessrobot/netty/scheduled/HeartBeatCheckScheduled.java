package com.yjh.accessrobot.netty.scheduled;

import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.netty.server.StateGridAHandlerImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;

/**
 * @author hyh
 * @since 2022/3/3
 * 定时进行心跳检测
 **/
@Component("heartBeatCheckScheduled")
@Slf4j
public class HeartBeatCheckScheduled {

    private final RedisTemplate redisTemplate;
    private final RobotService robotService;
    private final TaskScheduler taskScheduler;
    private final RobotServerHandler robotServerHandler;

    private static MutablePair<Long, Future<?>> HEART_BEAT_INFO;

    public HeartBeatCheckScheduled(RedisTemplate redisTemplate, RobotService robotService, TaskScheduler taskScheduler) {
        this.redisTemplate = redisTemplate;
        this.robotService = robotService;
        this.taskScheduler = taskScheduler;
        this.robotServerHandler = new StateGridAHandlerImpl(robotService, redisTemplate);
    }

    public void renewHeartBeat() {
        String heartBeatInterval = (String)redisTemplate.opsForHash().get("systemConfigKey:intervalConfig", "heartBeatInterval");
        long interval = NumberUtils.toLong(heartBeatInterval, 60);

        if (HEART_BEAT_INFO != null && interval == HEART_BEAT_INFO.getLeft()) {
            log.info("心跳间隔没有变更，心跳检测不变，interval: {} -- {}", interval, HEART_BEAT_INFO.getLeft());
            return;
        }
        log.info("心跳间隔变更，更新心跳检测时间，interval: {} -- {}", interval,
            Optional.ofNullable(HEART_BEAT_INFO).map(MutablePair::getLeft).orElse(0L));
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(new InternalRunner(robotServerHandler, redisTemplate), Instant.now(),
            Duration.ofSeconds(interval));
        if (HEART_BEAT_INFO == null) {
            HEART_BEAT_INFO = MutablePair.of(interval, future);
        } else {
            Future<?> oldFuture = HEART_BEAT_INFO.getRight();
            HEART_BEAT_INFO.setLeft(interval);
            HEART_BEAT_INFO.setRight(future);
            if (!oldFuture.isCancelled()) {
                oldFuture.cancel(false);
            }
        }
    }

    private static class InternalRunner implements Runnable {
        private final RobotServerHandler robotServerHandler;
        private final RedisTemplate redisTemplate;

        public InternalRunner(RobotServerHandler robotServerHandler, RedisTemplate redisTemplate) {
            this.robotServerHandler = robotServerHandler;
            this.redisTemplate = redisTemplate;
        }

        @Override
        public void run() {
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
}
