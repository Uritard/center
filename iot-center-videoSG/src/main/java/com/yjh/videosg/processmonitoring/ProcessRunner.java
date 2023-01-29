package com.yjh.videosg.processmonitoring;

import java.util.List;

/**
 * @author zilong
 * @date 11/26/21
 * Copyright (c) 2021 Yijiahe Technology Co., Ltd. All rights reserved.
 */
public interface ProcessRunner {

    void run(List<String> cmd) throws RuntimeException;

    void terminate();

    boolean isAlive();

}
