/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.service;

import com.yjh.video.core.gb28181.bean.msg.StreamPushItemFromRedis;
import com.yjh.video.core.media.zlm.dto.MediaItem;
import com.yjh.video.core.media.zlm.dto.StreamPushItem;
import com.yjh.video.core.utils.DateUtil;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/28
 * @since [产品/模块版本] （可选）
 */
public interface IStreamPushEventService {

    default StreamPushItem transform(MediaItem item) {
        StreamPushItem streamPushItem = new StreamPushItem();
        streamPushItem.setApp(item.getApp());
        streamPushItem.setMediaServerId(item.getMediaServerId());
        streamPushItem.setStream(item.getStream());
        streamPushItem.setAliveSecond(item.getAliveSecond());
        streamPushItem.setOriginSock(item.getOriginSock());
        streamPushItem.setTotalReaderCount(item.getTotalReaderCount());
        streamPushItem.setOriginType(item.getOriginType());
        streamPushItem.setOriginTypeStr(item.getOriginTypeStr());
        streamPushItem.setOriginUrl(item.getOriginUrl());
        streamPushItem.setCreateTime(DateUtil.getNow());
        streamPushItem.setAliveSecond(item.getAliveSecond());
        streamPushItem.setStatus(true);
        streamPushItem.setStreamType("push");
        streamPushItem.setVhost(item.getVhost());
        streamPushItem.setServerId(item.getSeverId());
        return streamPushItem;
    }

    StreamPushItem getPush(String app, String streamId);


    /**
     * 新的节点加入
     */
    void zlmServerOnline(String mediaServerId);

    /**
     * 节点离线
     */
    void zlmServerOffline(String mediaServerId);
}
