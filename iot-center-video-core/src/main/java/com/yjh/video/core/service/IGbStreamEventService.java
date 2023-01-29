/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.service;

import com.yjh.video.core.gb28181.bean.DeviceChannel;
import com.yjh.video.core.gb28181.bean.GbStream;
import com.yjh.video.core.gb28181.bean.ParentPlatform;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/29
 * @since [产品/模块版本] （可选）
 */
public interface IGbStreamEventService {

    DeviceChannel getDeviceChannelListByStream(GbStream gbStream, String catalogId, ParentPlatform platform);

    DeviceChannel getDeviceChannelListByStreamWithStatus(GbStream gbStream, String catalogId, ParentPlatform platform);
}
