package com.yjh.accesstcp.thread;

import com.yjh.accesstcp.common.utils.ThreadPoolUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
public class ReContentManager {

    private static final ConcurrentHashMap<SocketAddress, InternalRunner> RUNNER_MAP = new ConcurrentHashMap<>();

    public static void reContent(SocketAddress remoteAddress, Bootstrap bootstrap) {
        if (Optional.ofNullable(RUNNER_MAP.get(remoteAddress)).isPresent()) {
            InternalRunner runner = RUNNER_MAP.get(remoteAddress);
            runner.bootstrap = bootstrap;
            return;
        }
        InternalRunner internalRunner = new InternalRunner(remoteAddress, bootstrap, 60000);
        ThreadPoolUtil.PATROL_POOL.execute(internalRunner);
        RUNNER_MAP.put(remoteAddress, internalRunner);
    }

    public static void terminate(SocketAddress remoteAddress) {
        Optional.ofNullable(RUNNER_MAP.remove(remoteAddress)).ifPresent(InternalRunner::terminate);
    }

    static class InternalRunner implements Runnable {
        private final AtomicBoolean keepRunning = new AtomicBoolean(true);
        private final long recoverDuration;
        private final Object waiter = new Object();
        private final SocketAddress remoteAddress;
        private Bootstrap bootstrap;

        public InternalRunner(SocketAddress remoteAddress, Bootstrap bootstrap, long recoverDuration) {
            this.remoteAddress = remoteAddress;
            this.bootstrap = bootstrap;
            this.recoverDuration = recoverDuration;
        }

        @Override
        public void run() {
            while (keepRunning.get()) {
                bootstrap.remoteAddress(remoteAddress);
                ChannelFuture f = bootstrap.connect().addListener((ChannelFuture futureListener) -> {
                    if (futureListener.isSuccess()) {
                        terminate();
                    }else {
                        log.info("与服务端" + remoteAddress + "连接失败!准备尝试重连!");
                    }
                });
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
            ReContentManager.terminate(remoteAddress);
            keepRunning.set(false);
            synchronized (waiter) {
                waiter.notifyAll();
            }
        }
    }
}
