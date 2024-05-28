/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.imitator.pojo;

import java.time.Duration;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/5/27
 * @since [产品/模块版本] （可选）
 */
public class DelayTask<T> implements Delayed {
    private final RespSource respSource;
    private final T content;
    private final long delay;

    public DelayTask(RespSource respSource, T content, Duration duration) {
        this.respSource = respSource;
        this.content = content;
        //延时时间加上当前时间
        this.delay = System.currentTimeMillis() + duration.toMillis();
    }

    public T getContent() {
        return content;
    }

    public RespSource getRespSource() {
        return respSource;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        long diff = delay - System.currentTimeMillis();
        return unit.convert(diff, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed o) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
    }
}
