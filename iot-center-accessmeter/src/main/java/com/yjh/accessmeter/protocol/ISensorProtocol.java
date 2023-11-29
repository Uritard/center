/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol;

import com.yjh.accessmeter.module.device.entity.IotDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/27
 * @since [产品/模块版本] （可选）
 */
public interface ISensorProtocol {
    Logger LOGGER = LoggerFactory.getLogger(ISensorProtocol.class);

    ISensorProtocol init(List<IotDevice> devices);

    List<ResultMete> send(IotDevice device);

    void sendAsync(IotDevice device, ProtocolListener listener);
}
