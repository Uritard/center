package com.yjh.demo.task;

import com.yjh.commons.NamedThreadFactory;
import com.yjh.demo.config.ClientConfig;
import com.yjh.demo.handler.DemoBatchTaskHandler;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.MessageSender;
import com.yjh.protocol_a.OutboundMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author huyuhang
 * @date 2024/8/27
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class ServerHeartBeatThead {

    private static final Map<MessageSender, Future<?>> RUNNER_MAP = new HashMap<>();

    private ServerHeartBeatThead() {
        // do nothing
    }

    public static synchronized void addThread(MessageSender sender, DemoBatchTaskHandler demoBatchTaskHandler, long duration) {
        if (!RUNNER_MAP.containsKey(sender)) {
            Future<?> future =
                    new ScheduledThreadPoolExecutor(4, new NamedThreadFactory("interval-server-scheduled")).scheduleAtFixedRate(new InternalRunner(sender, demoBatchTaskHandler), 0L, duration, TimeUnit.SECONDS);
            RUNNER_MAP.put(sender, future);
        } else {
            log.warn("心跳定时器已添加");
        }
    }

    public static synchronized void terminate(MessageSender sender) {
        Optional.ofNullable(RUNNER_MAP.remove(sender)).ifPresent(f -> f.cancel(true));
    }

    static class InternalRunner implements Runnable {
        private final MessageSender sender;
        private final DemoBatchTaskHandler demoBatchTaskHandler;

        public InternalRunner(MessageSender sender, DemoBatchTaskHandler demoBatchTaskHandler) {
            this.sender = sender;
            this.demoBatchTaskHandler = demoBatchTaskHandler;
        }

        @Override
        public void run() {
            try {
                Message msg = new Message();
                msg.setType("251");
                msg.setCommand("2");
                OutboundMessage outboundMessage = new OutboundMessage(msg);
                outboundMessage.setSessionId(demoBatchTaskHandler.getSessionId());
                sender.send(outboundMessage);

                log.info("--心跳信息已发送--");
            } catch (Exception e) {
                log.error("心跳发送失败", e);
            }
        }

    }
}
