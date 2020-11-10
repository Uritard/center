package com.yjh.accessrobot.thread;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator
 */
public class MyThreadFactory implements ThreadFactory{

    private AtomicInteger atomicInteger = new AtomicInteger();

    private boolean isDaemon;

    public MyThreadFactory(boolean isDaemon){
        this.isDaemon = isDaemon;
    }

    @Override
    public Thread newThread(Runnable r) {
        atomicInteger.incrementAndGet();
        Thread thread =  new MyWorkThread(atomicInteger,r);
        thread.setDaemon(isDaemon);
        return thread;
    }
}
