/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * 使用队列和单独线程批量 pipline 插入 Redis
 *
 * @author Chenfei
 * @date 2022/10/28
 * @since [产品/模块版本] （可选）
 */
public class CruiseRedisStorage {
    private static final Logger log = LoggerFactory.getLogger(CruiseRedisStorage.class);

    private static final BlockingQueue<Map<String, String>> PATROL_INFO_Q = new ArrayBlockingQueue<>(1024);
    private static final int PIPLINE_SIZE = 512;
    private volatile static boolean running = true;

    private static RedisTemplate redisTemplate;

    public static void offer(Map<String, String> inspectionMap) {
        boolean ined = false;
        try {
            ined = PATROL_INFO_Q.offer(inspectionMap, 5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        if (!ined) {
            // 插入队列失败，使用当前线程插入
            putRedis(inspectionMap, redisTemplate);
        }
    }

    public static void piplinePutPatrolDetail(List<Map<String, String>> storageList) {
        try {
            redisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(RedisOperations operations) throws DataAccessException {
                    storageList.forEach(sm -> {
                        putRedis(sm, operations);
                    });
                    return null;
                }
            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void putRedis(Map<String, String> m, RedisOperations operations) {
        try {
            if (MapUtils.isEmpty(m)) {
                log.warn("detail map is empty.");
                return;
            }
            String taskId = m.get("taskId");
            String insId = m.get("instanceId");

            if (StringUtils.isAnyEmpty(taskId, insId)) {
                log.error("【任务未正确初始化】patrol_task_detail error, taskId: {}, instance: {}, {}", taskId, insId, JSON.toJSONString(m));
                return;
            }

            String key = UPatrolTaskService.PATROL_TASK_PREFIX + taskId + ":" + insId;
            operations.opsForHash().putAll(key, m);
            operations.expire(key, 3, TimeUnit.DAYS);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public static void stop() {
        log.info("CruiseRedisStorage Thread stoping...");
        running = false;
        PATROL_INFO_Q.offer(Collections.EMPTY_MAP);
    }

    public static void start(RedisTemplate redisTemplate) {
        log.info("CruiseRedisStorage Thread starting...");
        running = true;
        CruiseRedisStorage.redisTemplate = redisTemplate;

        // 使用单独线程处理任务信息插入 redis，永久执行，不使用线程池
        new Thread(new StorageThread()).start();
    }

    private static List<Map<String, String>> takes() {
        List<Map<String, String>> takeList = new ArrayList<>();
        try {
            Map<String, String> m = PATROL_INFO_Q.take();
            takeList.add(m);
            PATROL_INFO_Q.drainTo(takeList, PIPLINE_SIZE - 1);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        return takeList;
    }

    static class StorageThread implements Runnable {

        @Override
        public void run() {
            log.info("StorageThread started, running: {}", running);
            while (running) {
                List<Map<String, String>> storageList = takes();
                int size = storageList.size();
                piplinePutPatrolDetail(storageList);
                // if (size < (PIPLINE_SIZE * 0.75)) {
                //     try {
                //         Thread.sleep(10000);
                //     } catch (InterruptedException e) {
                //         log.error(e.getMessage(), e);
                //     }
                // }
            }

            log.info("StorageThread stoped, running: {}", running);
        }
    }
}
