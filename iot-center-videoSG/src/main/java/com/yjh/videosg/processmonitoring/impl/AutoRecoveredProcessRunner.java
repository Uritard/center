package com.yjh.videosg.processmonitoring.impl;

import com.yjh.videosg.processmonitoring.ProcessRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zilong
 * @date 11/26/21
 * Copyright (c) 2021 Yijiahe Technology Co., Ltd. All rights reserved.
 */
public class AutoRecoveredProcessRunner implements ProcessRunner {
    private static final Logger log = LoggerFactory.getLogger(AutoRecoveredProcessRunner.class);
    private Thread runningThread;
    private final AtomicReference<InternalRunner> internalRunnerRef = new AtomicReference<>(null);
    private final long recoverDuration;
    //开始运行时间
    private long startTime = 0;

    public AutoRecoveredProcessRunner(long recoverDuration) {
        this.recoverDuration = recoverDuration;
    }

    @Override
    public void run(List<String> cmd) throws RuntimeException {
        internalRunnerRef.set(new InternalRunner(cmd, recoverDuration));
        runningThread = new Thread(internalRunnerRef.get());
        runningThread.start();
        startTime = System.currentTimeMillis();
    }

    public void setStartTime(long newTime) {
         this.startTime = newTime;
    }
    public long getStartTime() {
        return startTime;
    }

    @Override
    public void terminate() {
        Optional.ofNullable(internalRunnerRef.get()).ifPresent(InternalRunner::terminate);
        internalRunnerRef.set(null);
    }

    @Override
    public boolean isAlive() {
        return Optional.ofNullable(internalRunnerRef.get()).map(InternalRunner::isAlive).orElse(false);
    }

    static class InternalRunner implements Runnable {
        List<String> cmd;
        private final AtomicBoolean keepRunning = new AtomicBoolean(true);
        private final AtomicReference<BlockedProcessRunner> runnerRef = new AtomicReference<>(null);
        private final long recoverDuration;
        private final Object waiter = new Object();

        public InternalRunner(List<String> cmd, long recoverDuration) {
            this.cmd = cmd;
            this.recoverDuration = recoverDuration;
        }

        @Override
        public void run() {
            while (keepRunning.get()) {
                BlockedProcessRunner runner = new BlockedProcessRunner(recoverDuration);
                runnerRef.set(runner);
                try {
                    runner.run(cmd);
                } catch (RuntimeException e) {
                    log.warn("Exception occurred while running", e);
                }

                synchronized (waiter) {
                    try {
                        waiter.wait(recoverDuration);
                    } catch (InterruptedException e) {
                        keepRunning.set(true);
                        log.warn("InternalRunner interrupted", e);
                    }
                }
            }
            log.info("InternalRunner exited.");
        }

        void terminate() {
            keepRunning.set(false);
            synchronized (waiter) {
                waiter.notifyAll();
            }
            Optional.ofNullable(runnerRef.get()).ifPresent(BlockedProcessRunner::terminate);
            runnerRef.set(null);
        }

        boolean isAlive() {
            return keepRunning.get();
        }
    }
}
