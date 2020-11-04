package com.yjh.accessudp.netty.server;

@lombok.extern.slf4j.Slf4j
public class DataDealThread implements Runnable {

    private UDPServerHandler udpServerHandler;

    private volatile boolean isThreadStart;

    public DataDealThread(UDPServerHandler udpServerHandler, boolean isThreadStart) {
        this.isThreadStart = isThreadStart;
        this.udpServerHandler = udpServerHandler;
    }

    @Override
    public void run() {
        while (isThreadStart) {
            try {
                udpServerHandler.ProcSend();
                if (!udpServerHandler.getIsThreadStart()) {
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
