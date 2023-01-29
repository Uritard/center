/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.service;

import com.yjh.video.core.media.zlm.dto.StreamProxyItem;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/28
 * @since [产品/模块版本] （可选）
 */
public interface IStreamProxyEventService {

    /**
     * 根据app与stream获取streamProxy
     * @return
     */
    StreamProxyItem getStreamProxyByAppAndStream(String app, String streamId);

    /**
     * 启用视频代理
     * @param app
     * @param stream
     * @return
     */
    boolean start(String app, String stream);

    /**
     * 新的节点加入
     * @param mediaServerId
     * @return
     */
    void zlmServerOnline(String mediaServerId);

    /**
     * 节点离线
     * @param mediaServerId
     * @return
     */
    void zlmServerOffline(String mediaServerId);
}
