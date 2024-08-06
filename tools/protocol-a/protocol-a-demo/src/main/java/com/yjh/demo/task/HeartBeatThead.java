package com.yjh.demo.task;

import com.yjh.demo.service.IMessageSender;
import com.yjh.protocol_a.Message;
import com.yjh.protocol_a.OutboundMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.TaskScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/15
 * @since [产品/模块版本] （可选）
 */
@Slf4j
public class HeartBeatThead {
    private static final Map<IMessageSender, Future<?>> RUNNER_MAP = new HashMap<>();

    private HeartBeatThead() {
        // do nothing
    }

    public static synchronized void addThread(IMessageSender sender, long duration) {
        if (!RUNNER_MAP.containsKey(sender)) {
            Future<?> future =
                IMessageSender.SCHEDULED_THREAD_POOLS.scheduleAtFixedRate(new InternalRunner(sender), 0L, duration, TimeUnit.SECONDS);
            RUNNER_MAP.put(sender, future);
        } else {
            log.warn("心跳定时器已添加");
        }
    }

    public static synchronized void terminate(IMessageSender sender) {
        Optional.ofNullable(RUNNER_MAP.remove(sender)).ifPresent(f -> f.cancel(true));
    }

    static class InternalRunner implements Runnable {
        private final IMessageSender sender;

        public InternalRunner(IMessageSender sender) {
            this.sender = sender;
        }

        @Override
        public void run() {
            try {
                Message msg = new Message();
                msg.setType("251");
                msg.setCommand("2");
                OutboundMessage outboundMessage = new OutboundMessage(msg);
                sender.sendMessage(outboundMessage);

                log.info("--心跳信息已发送--");
            } catch (Exception e) {
                log.error("心跳发送失败", e);
            }
        }

    }
}
