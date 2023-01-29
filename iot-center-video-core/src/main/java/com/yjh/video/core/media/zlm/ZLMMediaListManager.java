/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.media.zlm;

import com.yjh.video.core.media.zlm.dto.ChannelOnlineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lin
 */
@Component
public class ZLMMediaListManager {

    private Logger logger = LoggerFactory.getLogger(ZLMMediaListManager.class);

    private Map<String, ChannelOnlineEvent> channelOnPublishEvents = new ConcurrentHashMap<>();

    public void addChannelOnlineEventLister(String app, String stream, ChannelOnlineEvent callback) {
        this.channelOnPublishEvents.put(app + "_" + stream, callback);
    }

    public void removedChannelOnlineEventLister(String app, String stream) {
        this.channelOnPublishEvents.remove(app + "_" + stream);
    }

    public ChannelOnlineEvent getChannelOnlineEventLister(String app, String stream) {
        return this.channelOnPublishEvents.get(app + "_" + stream);
    }

}
