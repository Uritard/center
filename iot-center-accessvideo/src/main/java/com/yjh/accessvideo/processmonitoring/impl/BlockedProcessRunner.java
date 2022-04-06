package com.yjh.accessvideo.processmonitoring.impl;

import com.yjh.accessvideo.processmonitoring.ProcessRunner;
import com.yjh.accessvideo.videostreamer.ProcessManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author zilong
 * @date 11/26/21
 * Copyright (c) 2021 Yijiahe Technology Co., Ltd. All rights reserved.
 */
public class BlockedProcessRunner implements ProcessRunner {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final long aliveCheckingDuration;
    private final Object waiter = new Object();
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private final AtomicReference<Process> processRef = new AtomicReference<>(null);
    private final AtomicReference<InputStreamConsumer> errorConsumerRef = new AtomicReference<>(null);

    public BlockedProcessRunner(long aliveCheckingMillis) {
        if (aliveCheckingMillis < 1) {
            aliveCheckingMillis = 1000;
        } else if (aliveCheckingMillis > TimeUnit.HOURS.toMillis(1)) {
            aliveCheckingMillis = TimeUnit.HOURS.toMillis(1);
        }

        aliveCheckingDuration = aliveCheckingMillis;
    }

//    private String getProcessInfoString(Process process) {
//        ProcessHandle.Info info = process.info();
//        if (info == null) {
//            return "";
//        }
//        return process.pid() + " - cmdLine: " + info.commandLine().orElse("");
//    }

    @Override
    public void run(List<String> cmd) throws RuntimeException {
        ProcessBuilder processBuilder = new ProcessBuilder(cmd);
        try {
            Process process = processBuilder.start();
            processRef.set(process);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Process process = processRef.get();
        log.info("-------Process({}) started", cmd.get(4)); //getProcessInfoString(process)

        try (InputStream errorStream = process.getErrorStream()) {
            InputStreamConsumer errorsConsumer = new InputStreamConsumer(errorStream, log::debug);
            errorConsumerRef.set(errorsConsumer);
            errorsConsumer.start();

            while (keepRunning.get() && process.isAlive()) {
                if (log.isDebugEnabled()) {
                    log.debug("-------Process({}) is alive", cmd.get(4)); //getProcessInfoString(process)
                }

                synchronized (waiter) {
                    try {
                        waiter.wait(aliveCheckingDuration);
                    } catch (InterruptedException e) {
                        doTerminate();
                        throw new RuntimeException(e);
                    }
                }
            }

            errorsConsumer.shutdown();
        } catch (IOException eio) {
            throw new RuntimeException(eio);
        }
        log.warn("-------Process({}) exited.", cmd.get(4)); //getProcessInfoString(process)
    }

    @Override
    public void terminate() {
        doTerminate();
        synchronized (waiter) {
            waiter.notifyAll();
        }
    }

    @Override
    public boolean isAlive() {
        Process process = processRef.get();
        return process != null && process.isAlive();
    }

    private void doTerminate() {
        keepRunning.set(false);
        Process process = processRef.get();
        if (process != null && process.isAlive()) {
            process.destroy();
            processRef.set(null);
        }

        Optional.ofNullable(errorConsumerRef.get()).ifPresent(InputStreamConsumer::shutdown);
        errorConsumerRef.set(null);
    }
}
