package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.utils.ThreadPoolUtil;
import com.yjh.accesstcp.netty.TCPClientHandler;
import com.yjh.accesstcp.netty.handler.iot.IotHandlerEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <功能描述>
 *
 * @author YIJIAHE
 * @date 2023/2/9
 * @since [产品/模块版本] （可选）
 */
@Component
@Slf4j
public class RegisterManager {

    private final ConcurrentHashMap<String, InternalRunner> runnerMap = new ConcurrentHashMap<>();

    public void register(TCPClientHandler clientHandler) {
        String serverCode = clientHandler.getServer();
        if (Optional.ofNullable(runnerMap.get(serverCode)).isPresent()) {
            InternalRunner runner = runnerMap.get(serverCode);
            runner.clientHandler = clientHandler;
            return;
        }
        InternalRunner internalRunner = new InternalRunner(clientHandler, 10000);
        ThreadPoolUtil.PATROL_POOL.execute(internalRunner);
        runnerMap.put(serverCode, internalRunner);
    }

    public void terminate(String serverCode) {
        Optional.ofNullable(runnerMap.remove(serverCode)).ifPresent(InternalRunner::terminate);
    }

    static class InternalRunner implements Runnable {
        private final AtomicBoolean keepRunning = new AtomicBoolean(true);
        private final long recoverDuration;
        private final Object waiter = new Object();
        private TCPClientHandler clientHandler;

        public InternalRunner(TCPClientHandler clientHandler, long recoverDuration) {
            this.clientHandler = clientHandler;
            this.recoverDuration = recoverDuration;
        }

        @Override
        public void run() {
            while (keepRunning.get()) {
                clientHandler.sendRegister();
                synchronized (waiter) {
                    try {
                        waiter.wait(recoverDuration);
                    } catch (InterruptedException e) {
                        keepRunning.set(true);
                        log.warn("clientHandler sendRegister", e);
                    }
                }
            }
            log.info("clientHandler sendRegister exited.");
        }

        void terminate() {
            keepRunning.set(false);
            synchronized (waiter) {
                waiter.notifyAll();
            }
        }
    }
}
