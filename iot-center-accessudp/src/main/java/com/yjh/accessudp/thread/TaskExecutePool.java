package com.yjh.accessudp.thread;

import org.springframework.beans.factory.annotation.Value;
import java.util.concurrent.*;

/**
 * 自定义线程池
 * Created by tt on 2018/3/7.
 */

public class TaskExecutePool {

    @Value("${thread.corePoolSize}")
    private int corePoolSize;//线程池维护线程的最少数量

    @Value("${thread.maxPoolSize}")
    private int maxPoolSize;//线程池维护线程的最大数量

    @Value("${thread.queueCapacity}")
    private int queueCapacity; //缓存队列

    @Value("${thread.keepAliveTime}")
    private long keepAliveTime;//允许的空闲时间

    static MyThreadFactory threadFactory = new MyThreadFactory(true);

    //初始化类
    private final static TaskExecutePool taskExecutePool = new TaskExecutePool();
    public static TaskExecutePool getInstance() {
        return taskExecutePool;
    }

    private ExecutorService executorService = new ThreadPoolExecutor(10, 30, 20,
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(32), // 使用有界队列，避免OOM
            threadFactory, new ThreadPoolExecutor.DiscardPolicy());

    public void execute(Runnable runnable) { this.executorService.execute(runnable); }

}
