/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core.service;

import com.yjh.video.core.gb28181.bean.Device;
import com.yjh.video.core.utils.DateUtil;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/28
 * @since [产品/模块版本] （可选）
 */
public interface IDeviceEventService {

    /**
     * 根据IP和端口获取设备信息
     * @param host IP
     * @param port 端口
     * @return 设备信息
     */
    Device getDeviceByHostAndPort(String host, int port);

    /**
     * 获取所有在线设备
     * @return 设备列表
     */
    List<Device> getAllOnlineDevice();

    /**
     * 设备上线
     * @param device 设备信息
     */
    void online(Device device);

    /**
     * 设备下线
     * @param deviceId 设备编号
     */
    void offline(String deviceId);

    default boolean expire(Device device) {
        Instant registerTimeDate = Instant.from(DateUtil.formatter.parse(device.getRegisterTime()));
        Instant expireInstant = registerTimeDate.plusMillis(TimeUnit.SECONDS.toMillis(device.getExpires()));
        return expireInstant.isBefore(Instant.now());
    }
}
