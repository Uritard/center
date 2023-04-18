package com.yjh.accessrobot.threadpool;

import org.springframework.beans.factory.annotation.Value;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 自定义线程池
 * Created by tt on 2018/3/7.
 */

public class TaskExecutePool {

    static MyThreadFactory threadFactory = new MyThreadFactory(true);

    // 初始化类
    private final static TaskExecutePool taskExecutePool = new TaskExecutePool();
    public static TaskExecutePool getInstance() {
        return taskExecutePool;
    }

    private ExecutorService executorService = new ThreadPoolExecutor(10, 30, 20,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(32), // 使用有界队列，避免OOM
            threadFactory, new ThreadPoolExecutor.CallerRunsPolicy());

    public void execute(Runnable runnable) { this.executorService.execute(runnable); }

}
