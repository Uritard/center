/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.common.utils;

import com.yjh.platform.configuration.ThreadPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

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
    PATROL_POOL("Patrol-pool-thread", ThreadPoolConfig.getPatrolCorePoolSize(), ThreadPoolConfig.getPatrolMaxPoolSize(), false);

    private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolUtil.class);
    /**
     * 线程池对象
     */
    private final ThreadPoolExecutor threadPools;

    ThreadPoolUtil(String namePre, int coreSize, int maxSize, boolean common) {
        // 阻塞队列
        BlockingQueue<Runnable> queue;
        // 拒绝策略
        RejectedExecutionHandler rejectedExec;
        if (common) {
            queue = new ArrayBlockingQueue<>(ThreadPoolConfig.getQueueSizeDefault());
            rejectedExec = new ThreadPoolExecutor.AbortPolicy();
        } else {
            queue = new ArrayBlockingQueue<>(ThreadPoolConfig.getPatrolQueueSize());
            rejectedExec = new DelayRunsPolicy(100000);

        }

        threadPools = new ThreadPoolExecutor(coreSize, maxSize, ThreadPoolConfig.getKeepAliveTime(), TimeUnit.SECONDS, queue,
            new CommonThreadFactory(namePre), rejectedExec);
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
        message.append("当前工作线程数:").append(getActiveCount()).append(',').append("已添加线程数:").append(getTaskCount()).append(',')
            .append("已完成线程数:").append(getCompletedTaskCount()).append(',').append("阻塞队列缓存线程数:").append(getQueueSize()).append(',');
        return message.toString();
    }

    /**
     * 给线程池线程默认的线程名
     */
    private class CommonThreadFactory implements ThreadFactory {
        private final AtomicInteger count = new AtomicInteger();

        private String namePrefix;

        public CommonThreadFactory() {
            this.namePrefix = "Common-pool-Thread";
        }

        public CommonThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(@NonNull Runnable r) {
            int c = count.incrementAndGet();
            Thread t = new Thread(r);
            t.setName(namePrefix + "-" + c);
            return t;
        }
    }

    private class DelayRunsPolicy implements RejectedExecutionHandler {
        private final AtomicBoolean carry = new AtomicBoolean(false);

        private final BlockingQueue<Runnable> queue;

        public DelayRunsPolicy(int delayCount){
            queue = new ArrayBlockingQueue<>(delayCount);
        }

        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 多余线程加入队列
            if (queue.offer(r)) {
                // 启动额外队列线程处理
                run(executor);
            } else {
                LOGGER.error("DelayRunsPolicy queue is full, please dilatation your queue or revise your code! queue remaining capacity: {}", queue.remainingCapacity());
            }
        }

        private void run(ThreadPoolExecutor executor){
            if (!carry.get() && !carry.getAndSet(true)) {
                new Thread(() -> {
                    try {
                        LOGGER.info("new DelayRunsThread running...");
                        while (true) {
                            runExec(executor);
                        }
                    } catch (InterruptedException e) {
                        LOGGER.info(e.getMessage());
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    carry.set(false);
                }).start();
            }
        }

        private void runExec(ThreadPoolExecutor executor) throws InterruptedException {
            BlockingQueue<Runnable> runnables = executor.getQueue();
            int free = runnables.remainingCapacity();
            if (free > 8) {
                Runnable r = queue.poll();
                if (r != null) {
                    executor.execute(r);
                } else {
                    throw new InterruptedException("DelayRunsThread queue is empty，thread interrupt！");
                }
            } else {
                TimeUnit.MILLISECONDS.sleep(50);
            }
        }
    }
}
