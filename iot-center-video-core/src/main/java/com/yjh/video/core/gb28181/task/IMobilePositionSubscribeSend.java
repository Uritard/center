/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.gb28181.task;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/28
 * @since [产品/模块版本] （可选）
 */
public interface IMobilePositionSubscribeSend {

    /**
     * 向上级平台发送位置订阅
     * @param platformId 平台
     */
    void sendNotifyMobilePosition(String platformId);
}
