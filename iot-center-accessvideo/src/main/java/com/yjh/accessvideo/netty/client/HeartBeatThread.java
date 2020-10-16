package com.yjh.accessvideo.netty.client;

@lombok.extern.slf4j.Slf4j
public class HeartBeatThread implements Runnable {

    private AnalysisClientHandler analysisClientHandler;

    private volatile boolean isThreadStart;

    public HeartBeatThread(AnalysisClientHandler analysisClientHandler, boolean isThreadStart) {
        this.isThreadStart = isThreadStart;
        this.analysisClientHandler = analysisClientHandler;
    }

    @Override
    public void run() {
        while (isThreadStart) {
            try {
                analysisClientHandler.ProcSend();
                if (!analysisClientHandler.isThreadStart) {
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
