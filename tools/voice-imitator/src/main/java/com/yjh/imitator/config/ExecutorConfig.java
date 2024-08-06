package com.yjh.imitator.config;

import com.yjh.imitator.constant.Constant;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * "@Scheduled"是Spring框架提供的一种定时任务执行机制，默认情况下它是单线程的，在同时执行多个定时任务时可能会出现阻塞和性能问题。
 * 为了解决这种单线程瓶颈问题，可以将定时任务的执行机制改为支持多线程
 */
@Configuration
public class ExecutorConfig implements SchedulingConfigurer {

    private static final int CORE_POOL_SIZE = Constant.CPU_NUM;

    private static final int MAX_POOL_SIZE = Constant.CPU_NUM * 2;

    private static final String SCHEDULED_NAME_PREFIX = "scheduled-task-pool-%d";

    private static final String THREAD_NAME_PREFIX = "spring-task-pool-%d";

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(new ScheduledThreadPoolExecutor(ThreadPoolConfig.getScheduledPoolSize(),
            new BasicThreadFactory.Builder().namingPattern(SCHEDULED_NAME_PREFIX).daemon(true).build(),
            new ThreadPoolExecutor.CallerRunsPolicy()));
    }

    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        // 设置线程池大小
        scheduler.setPoolSize(5);
        // 设置线程名称前缀
        scheduler.setThreadNamePrefix(SCHEDULED_NAME_PREFIX);
        // 设置取消的任务是否从队列中移除
        scheduler.setRemoveOnCancelPolicy(true);
        return scheduler;
    }

    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(ThreadPoolConfig.getQueueSizeDefault());
        executor.setKeepAliveSeconds(ThreadPoolConfig.getKeepAliveTime());
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);

        // 线程池对拒绝任务的处理策略
        // CallerRunsPolicy：由调用线程（提交任务的线程）处理该任务
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        // 初始化
        executor.initialize();
        return executor;
    }
}
