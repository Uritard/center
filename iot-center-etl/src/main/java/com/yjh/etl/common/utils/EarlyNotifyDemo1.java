package com.yjh.etl.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EarlyNotifyDemo1 {

    private static String lockObject = "";

    public static void main(String[] args) {
        WaitThread waitThread = new WaitThread(lockObject);
        NotifyThread notifyThread = new NotifyThread(lockObject);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        waitThread.start();
        notifyThread.start();
    }

    static class WaitThread extends Thread {
        private String lock;

        public WaitThread(String lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            synchronized (lock) {
                System.out.println(Thread.currentThread().getName() + "  进去代码块");
                System.out.println(Thread.currentThread().getName() + "  开始wait");
                for (int i=0;i<15;i++) {
                    lock = "ss";
                    try {
                        Thread.sleep(1000);
                        System.out.println("lockWait: "+lock);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                System.out.println(Thread.currentThread().getName() + "   结束wait");
            }
        }
    }

    static class NotifyThread extends Thread {
        private String lock;

        public NotifyThread(String lock) {
            this.lock = lock;
        }

        @Override
        public void run() {
            synchronized (lock) {
                System.out.println(Thread.currentThread().getName() + "  进去代码块");
                System.out.println(Thread.currentThread().getName() + "  开始notify");
                lock = "ss1";
                System.out.println("lockNotify; "+lock);
                System.out.println(Thread.currentThread().getName() + "   结束开始notify");
            }
        }
    }
}