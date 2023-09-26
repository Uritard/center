/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.common.utils;

import com.yjh.commons.NamedThreadFactory;
import com.yjh.commons.core.thread.TaskQueue;
import com.yjh.commons.core.thread.ThreadPoolExecutor;
import com.yjh.platform.configuration.ThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/12
 * @since [产品/模块版本] （可选）
 */
public enum ThreadPoolUtil {
    /**
     * 通用线程池
     */
    COMMON_POOL("Common-pool-thread", ThreadPoolConfig.getCorePoolSizeDefault(), ThreadPoolConfig.getMaxPoolSizeDefault(), true),
    /**
     * 任务线程池
     */
    PATROL_POOL("Patrol-pool-thread", ThreadPoolConfig.getPatrolCorePoolSize(), ThreadPoolConfig.getPatrolMaxPoolSize(), false),
    /**
     * 本地任务执行线程，仅相机和录音任务可用
     */
    LOCAL_TASK_POOL("Local_task-pool-thread", ThreadPoolConfig.getTaskCorePoolSize(), ThreadPoolConfig.getTaskMaxPoolSize(), false);

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolUtil.class);
    /**
     * 线程池对象
     */
    private final ThreadPoolExecutor threadPools;

    ThreadPoolUtil(String namePre, int coreSize, int maxSize, boolean common) {
        // 阻塞队列
        TaskQueue queue;
        // 拒绝策略
        ThreadPoolExecutor.RejectedExecutionHandler rejectedExec;
        if (common) {
            queue = new TaskQueue(ThreadPoolConfig.getQueueSizeDefault());
            rejectedExec = new ThreadPoolExecutor.AbortPolicy();
        } else {
            queue = new TaskQueue(ThreadPoolConfig.getPatrolQueueSize());
            rejectedExec = new ThreadPoolExecutor.CallerRunsPolicy();
        }

        threadPools = new ThreadPoolExecutor(coreSize, maxSize, ThreadPoolConfig.getKeepAliveTime(), TimeUnit.SECONDS, queue,
            new NamedThreadFactory(namePre), rejectedExec);
    }

    /**
     * 添加任务线程
     */
    public void addThread(Runnable runnable) {
        threadPools.execute(runnable);
    }

    public ThreadPoolExecutor getThreadPools() {
        return this.threadPools;
    }

    /**
     * 释放线程池资源
     */
    public void shutdown() {
        if (threadPools != null) {
            threadPools.shutdown();
        }
    }

    /**
     * 线程池当前工作线程数
     */
    public int getActiveCount() {
        return threadPools.getActiveCount();
    }

    /**
     * 线程池当前线程数
     */
    public int getPoolSize() {
        return threadPools.getPoolSize();
    }

    /**
     * 线程池已完成任务数(或线程数)
     */
    public long getCompletedTaskCount() {
        return threadPools.getCompletedTaskCount();
    }

    /**
     * 线程池已添加任务数(或线程数)
     */
    public long getTaskCount() {
        return threadPools.getTaskCount();
    }

    /**
     * 阻塞队列缓存任务数(或线程数)
     */
    public int getQueueSize() {
        return threadPools.getQueue().size();
    }

    /**
     * 返回当前线程池状态信息
     */
    public String getMessage() {
        StringBuilder message = new StringBuilder(256);
        message.append("当前线程数:").append(getPoolSize()).append(',').append("当前工作线程数:").append(getActiveCount()).append(',')
            .append("已添加线程数:").append(getTaskCount()).append(',').append("已完成线程数:").append(getCompletedTaskCount()).append(',')
            .append("阻塞队列缓存线程数:").append(getQueueSize()).append(',');
        return message.toString();
    }
}
