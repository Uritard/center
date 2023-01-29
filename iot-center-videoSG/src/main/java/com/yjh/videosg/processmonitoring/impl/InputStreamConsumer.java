package com.yjh.videosg.processmonitoring.impl;

import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author zilong
 * @date 11/26/21
 * Copyright (c) 2021 Yijiahe Technology Co., Ltd. All rights reserved.
 */
public class InputStreamConsumer extends Thread {
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final InputStream inputStream;
    private final Consumer<String> consumer;

    public InputStreamConsumer(InputStream inputStream, Consumer<String> consumer) {
        this.inputStream = inputStream;
        this.consumer = consumer;
    }

    @Override
    public void run() {
        Scanner scanner = new Scanner(inputStream);
        while (running.get()) {
            if (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                consumer.accept(line);
            }
        }
        scanner.close();
    }

    void shutdown() {
        running.set(false);
    }
}
