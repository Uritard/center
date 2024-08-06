package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.netty.TCPClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;

/**
 * @author lqh
 * @since 2021/1/12
 */
@Slf4j
public class HeartBeatThead {
    private static final Map<String, Future<?>> RUNNER_MAP = new HashMap<>();

    private HeartBeatThead() {
        // do nothing
    }

    public static synchronized void addThread(TCPClientHandler clientHandler, TaskScheduler taskScheduler, long duration) {
        if (!RUNNER_MAP.containsKey(clientHandler.getServer())) {
            Future<?> future =
                    taskScheduler.scheduleAtFixedRate(new InternalRunner(clientHandler), Instant.now(), Duration.ofSeconds(duration));
            RUNNER_MAP.put(clientHandler.getServer(), future);
        } else {
            log.warn(clientHandler.getServer() + "心跳定时器已添加");
        }
    }

    public static synchronized void terminate(String serverCode) {
        Optional.ofNullable(RUNNER_MAP.remove(serverCode)).ifPresent(f -> f.cancel(true));
    }

    static class InternalRunner implements Runnable {
        private final TCPClientHandler clientHandler;

        public InternalRunner(TCPClientHandler clientHandler) {
            this.clientHandler = clientHandler;
        }

        @Override
        public void run() {
            try {
                XMLBaseModel xmlBaseModel = new XMLBaseModel().setType("251").setCommand("2");
                clientHandler.send(xmlBaseModel, 0, true);

                log.info(clientHandler.getServer() + "--心跳信息已发送--");
            } catch (Exception e) {
                log.error(clientHandler.getServer() + "心跳发送失败", e);
            }
        }

    }
}
