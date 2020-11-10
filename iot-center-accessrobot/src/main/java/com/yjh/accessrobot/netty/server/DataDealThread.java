package com.yjh.accessrobot.netty.server;

@lombok.extern.slf4j.Slf4j
public class DataDealThread implements Runnable {

    private RobotServerHandler robotServerHandler;

    private volatile boolean isThreadStart;

    public DataDealThread(RobotServerHandler robotServerHandler, boolean isThreadStart) {
        this.isThreadStart = isThreadStart;
        this.robotServerHandler = robotServerHandler;
    }

    @Override
    public void run() {
        while (isThreadStart) {
            try {
                robotServerHandler.ProcSend();
                if (!robotServerHandler.getIsThreadStart()) {
                    isThreadStart = false;
                    log.info("Thread is " + Thread.currentThread().getName() + Thread.currentThread().getId());
                    log.info("stop Thread success!");
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
