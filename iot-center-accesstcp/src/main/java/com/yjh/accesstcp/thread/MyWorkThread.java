package com.yjh.accesstcp.thread;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator
 */
public class MyWorkThread extends Thread{
    private AtomicInteger atomicInteger;

    public MyWorkThread(AtomicInteger atomicInteger,Runnable runnable){
        super(runnable);
        this.atomicInteger = atomicInteger;
    }


    @Override
    public void run() {
        System.out.println(this.getName()+" test"+atomicInteger.getAndDecrement());
        super.run();
    }
}
