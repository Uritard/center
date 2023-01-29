/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.service;

import com.yjh.video.core.gb28181.bean.ParentPlatform;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/28
 * @since [产品/模块版本] （可选）
 */
public interface IPlatformEventService {

    /**
     * 平台上线
     * @param parentPlatform 平台信息
     */
    void online(ParentPlatform parentPlatform);

    /**
     * 平台离线
     * @param parentPlatform 平台信息
     */
    void offline(ParentPlatform parentPlatform);

}
