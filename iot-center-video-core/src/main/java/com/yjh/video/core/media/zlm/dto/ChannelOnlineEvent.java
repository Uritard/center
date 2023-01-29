/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.media.zlm.dto;

import java.text.ParseException;

/**
 * @author lin
 */
public interface ChannelOnlineEvent {

    void run(String app, String stream, String serverId) throws ParseException;
}
