package com.yjh.accessvideo.commons.utils.threadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorsUtil {
    private final static ExecutorService sThreadPoolExecutor = Executors.newFixedThreadPool(20,
            new NamedThreadFactory("ThreadPoolExecutor"));

    public static ExecutorService getsThreadPoolExecutor() {
        return sThreadPoolExecutor;
    }
}
