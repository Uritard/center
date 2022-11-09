/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.configuration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/12
 * @since [产品/模块版本] （可选）
 */
@Component
@ConfigurationProperties(prefix = "thread.pool")
@Slf4j
public class ThreadPoolConfig {
    /**
     * 核心线程数
     */
    private static int corePoolSizeDefault = 32;
    /**
     * 最大线程数
     */
    private static int maxPoolSizeDefault = 32;
    /**
     * 线程队列大小
     */
    private static int queueSizeDefault = 1000;
    /**
     * 空闲线程存活时间
     */
    private static long keepAliveTime = 30;

    /**
     * 核心线程数
     */
    private static int patrolCorePoolSize = 100;
    /**
     * 最大线程数
     */
    private static int patrolMaxPoolSize = 800;
    /**
     * 线程队列大小
     */
    private static int patrolQueueSize = 64;

    public static int getCorePoolSizeDefault() {
        return corePoolSizeDefault;
    }

    public void setCorePoolSizeDefault(int corePoolSizeDefault) {
        ThreadPoolConfig.corePoolSizeDefault = corePoolSizeDefault;
    }

    public static int getMaxPoolSizeDefault() {
        return maxPoolSizeDefault;
    }

    public void setMaxPoolSizeDefault(int maxPoolSizeDefault) {
        ThreadPoolConfig.maxPoolSizeDefault = maxPoolSizeDefault;
    }

    public static long getKeepAliveTime() {
        return keepAliveTime;
    }

    public void setKeepAliveTime(long keepAliveTime) {
        ThreadPoolConfig.keepAliveTime = keepAliveTime;
    }

    public static int getQueueSizeDefault() {
        return queueSizeDefault;
    }

    public void setQueueSizeDefault(int queueSizeDefault) {
        ThreadPoolConfig.queueSizeDefault = queueSizeDefault;
    }

    public static int getPatrolCorePoolSize() {
        return patrolCorePoolSize;
    }

    public void setPatrolCorePoolSize(int patrolCorePoolSize) {
        ThreadPoolConfig.patrolCorePoolSize = patrolCorePoolSize;
    }

    public static int getPatrolMaxPoolSize() {
        return patrolMaxPoolSize;
    }

    public void setPatrolMaxPoolSize(int patrolMaxPoolSize) {
        ThreadPoolConfig.patrolMaxPoolSize = patrolMaxPoolSize;
    }

    public static int getPatrolQueueSize() {
        return patrolQueueSize;
    }

    public void setPatrolQueueSizeDefault(int patrolQueueSize) {
        ThreadPoolConfig.patrolQueueSize = patrolQueueSize;
    }
}
