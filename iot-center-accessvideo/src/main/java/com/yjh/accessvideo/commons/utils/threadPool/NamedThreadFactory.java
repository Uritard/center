package com.yjh.accessvideo.commons.utils.threadPool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedThreadFactory implements ThreadFactory {
    private AtomicInteger threadNo = new AtomicInteger(1);
    private final String nameStart;

    public NamedThreadFactory(String poolName) {
        this.nameStart = "[" + poolName + "-";
    }

    public Thread newThread(Runnable r) {
        String threadName = this.nameStart + this.threadNo.getAndIncrement() + "]";
        Thread newThread = new Thread(r, threadName);
        newThread.setDaemon(true);
        if (newThread.getPriority() != 5) {
            newThread.setPriority(5);
        }

        return newThread;
    }
}
