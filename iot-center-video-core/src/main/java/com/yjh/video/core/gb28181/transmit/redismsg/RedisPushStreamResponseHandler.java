/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.gb28181.transmit.redismsg;

import com.yjh.video.core.gb28181.bean.msg.MessageForPushChannelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/28
 * @since [产品/模块版本] （可选）
 */
@Component
public class RedisPushStreamResponseHandler {
    private final static Logger logger = LoggerFactory.getLogger(RedisPushStreamResponseHandler.class);

    private Map<String, PushStreamResponseEvent> responseEvents = new ConcurrentHashMap<>();

    public interface PushStreamResponseEvent{
        void run(MessageForPushChannelResponse response);
    }


    public void addEvent(String app, String stream, PushStreamResponseEvent callback) {
        responseEvents.put(app + stream, callback);
    }

    public void removeEvent(String app, String stream) {
        responseEvents.remove(app + stream);
    }
}
