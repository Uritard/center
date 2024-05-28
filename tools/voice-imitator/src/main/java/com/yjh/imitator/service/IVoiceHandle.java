/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.imitator.service;

import com.yjh.imitator.pojo.Analyse;
import com.yjh.imitator.pojo.Collect;
import com.yjh.imitator.pojo.DelayTask;
import com.yjh.imitator.pojo.VoiceRequest;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/27
 * @since [产品/模块版本] （可选）
 */
public interface IVoiceHandle {
    DelayQueue<DelayTask<Collect>> VOICE_COLLECT = new DelayQueue<>();
    DelayQueue<DelayTask<Analyse>> VOICE_ANALYSE = new DelayQueue<>();

    boolean addCollect(VoiceRequest<Collect> body);

    boolean addAnalyse(VoiceRequest<Analyse> body);


}
