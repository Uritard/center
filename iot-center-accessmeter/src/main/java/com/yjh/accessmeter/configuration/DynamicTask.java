package com.yjh.accessmeter.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2024/1/16
 * @since [产品/模块版本] （可选）
 */
@Component
public class DynamicTask {

    private final Logger logger = LoggerFactory.getLogger(DynamicTask.class);

    private ThreadPoolTaskScheduler threadPoolTaskScheduler;

    private final Map<String, ScheduledFuture<?>> futureMap = new ConcurrentHashMap<>();
    private final Map<String, Runnable> runnableMap = new ConcurrentHashMap<>();

    @PostConstruct
    public void dynamicTask() {
        threadPoolTaskScheduler = new ThreadPoolTaskScheduler();
        threadPoolTaskScheduler.setPoolSize(300);
        threadPoolTaskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        threadPoolTaskScheduler.setAwaitTerminationSeconds(10);
        threadPoolTaskScheduler.initialize();
    }

    /**
     * 延时任务
     * @param key 任务ID
     * @param task 任务
     * @param delay 延时 /毫秒
     * @return
     */
    public void startDelay(String key, Runnable task, int delay) {
        stop(key);

        // 获取执行的时刻
        Instant startInstant = Instant.now().plusMillis(TimeUnit.MILLISECONDS.toMillis(delay));

        ScheduledFuture<?> future = futureMap.computeIfAbsent(key, k -> {
            logger.debug("延时任务【{}】启动成功！！！", key);
            runnableMap.put(key, task);
            return threadPoolTaskScheduler.schedule(task, startInstant);
        });
        if (future.isCancelled()) {
            logger.debug("延时任务【{}】已存在但是关闭状态！！！", key);
        }
    }

    public boolean stop(String key) {
        boolean result = false;
        ScheduledFuture<?> future = futureMap.get(key);
        if (future != null && !future.isCancelled()) {
            result = future.cancel(false);
            futureMap.remove(key);
            runnableMap.remove(key);
        }
        return result;
    }

}
