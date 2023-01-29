/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.service;

import com.alibaba.fastjson.JSONObject;
import com.yjh.video.core.gb28181.bean.SSRCInfo;
import com.yjh.video.core.media.zlm.dto.MediaServerItem;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/28
 * @since [产品/模块版本] （可选）
 */
public interface IMediaServerEventService {
    void updateVmServer(List<MediaServerItem> mediaServerItemList);

    List<MediaServerItem> getAll();

    List<MediaServerItem> getAllOnline();

    MediaServerItem getOne(String generalMediaServerId);

    void clearMediaServerForOnline();

    void releaseSsrc(String mediaServerItemId, String ssrc);

    SSRCInfo openRTPServer(MediaServerItem mediaServerItem, String streamId, boolean ssrcCheck, boolean isPlayback);

    SSRCInfo openRTPServer(MediaServerItem mediaServerItem, String streamId, String ssrc, boolean ssrcCheck, boolean isPlayback);

    SSRCInfo openRTPServer(MediaServerItem mediaServerItem, String streamId, String ssrc, boolean ssrcCheck, boolean isPlayback, Integer port);

    void closeRTPServer(MediaServerItem mediaServerItem, String streamId);

    void closeRTPServer(String mediaServerId, String streamId);

    void clearRTPServer(MediaServerItem mediaServerItem);

    boolean checkRtpServer(MediaServerItem mediaServerItem, String app, String stream);

    void zlmServerOffline(String mediaServerId);

    void delete(String id);

    void resetOnlineServerItem(MediaServerItem serverItem);

    void addCount(String mediaServerId);

    void removeCount(String mediaServerId);

    MediaServerItem getMediaServerForMinimumLoad();

    void setZLMConfig(MediaServerItem mediaServerItem, boolean restart);

    boolean checkMediaRecordServer(String ip, int port);

    void updateMediaServerKeepalive(String mediaServerId, JSONObject data);
}
