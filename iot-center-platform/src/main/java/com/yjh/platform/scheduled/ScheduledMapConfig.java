package com.yjh.platform.scheduled;

import com.yjh.commons.NamedThreadFactory;
import com.yjh.platform.configuration.ThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/**
 * @Author jinyujiang
 * @Description 用于缓存动态生成的定时任务
 * @Date create in 2023/4/19 16:41
 */
public class ScheduledMapConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduledMapConfig.class);

    private static final ScheduledThreadPoolExecutor
        SCHEDULED_THREAD_POOLS = new ScheduledThreadPoolExecutor(ThreadPoolConfig.getScheduledPoolSize(), new NamedThreadFactory("plartform-scheduled"));

    private ScheduledMapConfig(){
        // nothing to do
    }

    /**
     * 指定多长时间执行一次，一共执行多少次，若执行方法返回true或者达到执行次数，则不再执行
     * @param seconds 多长时间执行一次
     * @param numRetries 执行次数
     * @param taskFunction 执行内容
     */
    public static void schedule(int seconds, int numRetries, BooleanSupplier taskFunction) {
        int nextRetries = numRetries - 1;
        SCHEDULED_THREAD_POOLS.schedule(() -> {
            boolean succ = false;
            try {
                succ = taskFunction.getAsBoolean();
            } catch (Exception e) {
                LOGGER.error(e.getMessage(), e);
            }
            if (!succ && nextRetries > 0) {
                schedule(seconds, nextRetries, taskFunction);
            }  else {
                LOGGER.error("schedule ended, leave retries: {}, return: {}", nextRetries, succ);
            }

        }, seconds, TimeUnit.SECONDS);
    }

    /**
     * 指定多长时间执行一次，一共执行多少次，若执行方法返回true或者达到执行次数，则不再执行
     * 会向执行方法传入一个参数表示已经执行了多少次
     * @param seconds 多长时间执行一次
     * @param numRetries 执行次数
     * @param taskFunction 执行内容
     */
    public static void schedule(int seconds, int numRetries, Predicate<Integer> taskFunction) {
        schedule(seconds, numRetries, 0, taskFunction);
    }

    /**
     * 内部实现，指定多长时间执行一次，一共执行多少次，若执行方法返回true或者达到执行次数，则不再执行
     * 会向执行方法传入一个参数表示已经执行了多少次
     * @param seconds 多长时间执行一次
     * @param numRetries 执行次数
     * @param times 已经执行次数
     * @param taskFunction 执行内容
     */
    private static void schedule(int seconds, int numRetries, int times, Predicate<Integer> taskFunction) {
        SCHEDULED_THREAD_POOLS.schedule(() -> {
            int currTimes = times + 1;
            boolean succ = taskFunction.test(currTimes);
            if (!succ && numRetries - currTimes > 0) {
                LOGGER.info("schedule running times {}", currTimes);
                schedule(seconds, numRetries, currTimes, taskFunction);
            } else {
                LOGGER.info("schedule ended, times: {}, return: {}", currTimes, succ);
            }

        }, seconds, TimeUnit.SECONDS);
    }

    /**
     * 指定时间段后执行一次
     * @param seconds 多长时间后执行
     * @param taskFunction 执行内容
     */
    public static <T> void schedule(int seconds, T param, Consumer<T> taskFunction) {
        SCHEDULED_THREAD_POOLS.schedule(() -> taskFunction.accept(param), seconds, TimeUnit.SECONDS);
    }
}
