package com.yjh.platform.scheduled;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * @Author jinyujiang
 * @Description 用于缓存动态生成的定时任务
 * @Date create in 2023/4/19 16:41
 */
public class ScheduledMapConfig {
    private static Map<String, ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap<>();
    private static Map<String, Integer> scheduledRunTimesMap = new ConcurrentHashMap<>();
    private static final Integer MAX_RUN_TIMES = 8;
    private static Logger log = LoggerFactory.getLogger(ScheduledMapConfig.class);

    public static void add(String key, ScheduledFuture scheduledFuture) {
        log.info("创建定时任务，key: {}", key);
        if (StringUtils.isNotEmpty(key) && scheduledFuture != null) {
            scheduledFutureMap.put(key, scheduledFuture);
        }
    }

    public static ScheduledFuture get(String key) {
        if (StringUtils.isNotEmpty(key)) {
            return scheduledFutureMap.get(key);
        }

        return null;
    }

    public static void remove(String key) {
        log.info("移除定时任务，key: {}", key);
        if (StringUtils.isEmpty(key)) {
            return;
        }

        if (scheduledFutureMap.containsKey(key)) {
            ScheduledFuture scheduledFuture = get(key);
            boolean result = scheduledFuture.cancel(false);
            if (result) {
                scheduledFutureMap.remove(key);

                if (scheduledRunTimesMap.containsKey(key)) {
                    scheduledRunTimesMap.remove(key);
                }
            }
        }
    }

    /**
     * 记录定时任务已执行次数，并判断是否需要移除
     *
     * @param key key
     */
    public static void countAndClean(String key) {
        log.info("计算定时任务执行次数，并判断是否需要移除，key: {}", key);
        Integer scheduledRunTimes = scheduledRunTimesMap.get(key);
        if (scheduledRunTimes == null) {
            scheduledRunTimes = 1;
        } else {
            scheduledRunTimes++;
        }

        if (scheduledRunTimes > MAX_RUN_TIMES) {
            log.info("定时任务超过最大执行次数，需要移除，key: {}", key);
            remove(key);
        } else {
            scheduledRunTimesMap.put(key, scheduledRunTimes);
            log.info("定时任务未超过最大执行次数，key: {}， 已执行次数:{}", key, scheduledRunTimes);
        }
    }
}
