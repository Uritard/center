package com.yjh.accessvideo.netty.client;

@lombok.extern.slf4j.Slf4j
public class HeartBeatThread implements Runnable {

    private IEC104ClientHandler iec104ClientHandler;

    private volatile boolean isThreadStart;

    public HeartBeatThread(IEC104ClientHandler iec104ClientHandler, boolean isThreadStart) {
        this.isThreadStart = isThreadStart;
        this.iec104ClientHandler = iec104ClientHandler;
    }

    @Override
    public void run() {
        while (isThreadStart) {
            try {
                iec104ClientHandler.ProcSend();
                if (!iec104ClientHandler.isThreadStart) {
                    isThreadStart = false;
                    log.info("Thread is " + Thread.currentThread().getName() + Thread.currentThread().getId());
                    log.info("Thread stop success!");
                }
                Thread.sleep(5000);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }

}
