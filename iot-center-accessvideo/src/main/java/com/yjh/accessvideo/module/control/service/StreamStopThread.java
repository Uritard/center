package com.yjh.accessvideo.module.control.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author YIJIAHE
 */
public class StreamStopThread implements Runnable {

    public StreamStopThread() {

    }

    private static final Logger log = LoggerFactory.getLogger(StreamStopThread.class);

    @Override
    public void run() {
        killProcess();
    }

    /**
     * 历史流如果播放完毕 无法用进程方法  process.destroy() 关闭 调用 kill 强行关闭
     */
    private void killProcess() {
        try {
            String url = "ps -ef | grep history | grep -v 'grep'";
            Process processForId = Runtime.getRuntime().exec(new String[]{"sh", "-c", url});
            processForId.waitFor();
            Thread.sleep(1000);
            BufferedReader readerForId = new BufferedReader(new InputStreamReader(processForId.getInputStream(), StandardCharsets.UTF_8));
            String lineForId = null;
            StringBuilder dataBackForId = new StringBuilder();
            while ((lineForId = readerForId.readLine()) != null) {
                dataBackForId.append(lineForId).append('\n');
            }
            log.info("流进程：" + dataBackForId);
            if (dataBackForId.length() > 0) {
                int processNum = Integer.parseInt(dataBackForId.substring(9, 16).replace(" ", ""));
                log.info("进程号{}", processNum);
                String urlStop = "kill -9 " + processNum;
                Runtime.getRuntime().exec(urlStop);
            }
        } catch (Exception e) {
            log.error("killProcess history error", e);
        }
    }
}
