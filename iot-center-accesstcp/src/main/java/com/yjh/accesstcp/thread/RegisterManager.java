package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.netty.server.TCPClientHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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


    private final ConcurrentHashMap<String, InternalRunner> runnerMap = new ConcurrentHashMap<String, InternalRunner>();

    public void register(TCPClientHandler clientHandler) {
        if (Optional.ofNullable(runnerMap.get("register")).isPresent()) {
            InternalRunner runner = runnerMap.get("register");
            runner.clientHandler = clientHandler;
            return;
        }
        InternalRunner internalRunner = new InternalRunner(clientHandler, 10000);
        TaskExecutePool.getInstance().execute(internalRunner);
        runnerMap.put("register", internalRunner);
    }

    public void terminate() {
        Optional.ofNullable(runnerMap.get("register")).ifPresent(InternalRunner::terminate);
        runnerMap.remove("register");
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
