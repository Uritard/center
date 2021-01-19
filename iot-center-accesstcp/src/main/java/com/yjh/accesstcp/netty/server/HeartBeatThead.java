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

                if (!tCPClientHandler.getIsThreadStart()) {
                    isThreadStart = false;
                    log.info("Thread is " + Thread.currentThread().getName() + Thread.currentThread().getId());
                    log.info("Thread stop success!");
                }
                tCPClientHandler.ProcSend();
                log.info("--心跳信息已发送--");
                String s = Constant.paramMap.get("heart_beat_interval");
                if(s == null){
                    s= "30";
                }
                //todo 记得改
                break;
                //Thread.sleep(Long.valueOf(s)*1000L);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
