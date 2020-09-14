package com.yjh.access.netty.server;

@lombok.extern.slf4j.Slf4j
public class DataDealThread implements Runnable {

    private IEC104ServerHandler iec104ServerHandler;

    private volatile boolean isThreadStart;

    public DataDealThread(IEC104ServerHandler iec104ServerHandler, boolean isThreadStart) {
        this.isThreadStart = isThreadStart;
        this.iec104ServerHandler = iec104ServerHandler;
    }

    @Override
    public void run() {
        while (isThreadStart) {
            try {
                iec104ServerHandler.ProcSend();
                if (!iec104ServerHandler.getIsThreadStart()) {
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
