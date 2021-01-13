package com.yjh.accesstcp.netty.server;

import com.yjh.accesstcp.common.Constant;

/**
 * @author lqh
 * @since 2021/1/12
 */
@lombok.extern.slf4j.Slf4j
public class HeartBeatThead implements Runnable{
    private TCPClientHandler tCPClientHandler;

    private volatile boolean isThreadStart;

    public HeartBeatThead(TCPClientHandler tCPClientHandler, boolean isThreadStart) {
        this.isThreadStart = isThreadStart;
        this.tCPClientHandler = tCPClientHandler;
    }

    @Override
    public void run() {
        while (isThreadStart) {
            try {
                tCPClientHandler.ProcSend();
                if (!tCPClientHandler.getIsThreadStart()) {
                    isThreadStart = false;
                    log.info("Thread is " + Thread.currentThread().getName() + Thread.currentThread().getId());
                    log.info("Thread stop success!");
                }
                Thread.sleep(Long.valueOf(Constant.paramMap.get("heart_beat_interval"))*1000L);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
