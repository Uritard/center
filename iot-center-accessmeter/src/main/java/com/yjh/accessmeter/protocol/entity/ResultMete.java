/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class ResultMete {
    /**
     * 物联设备Id
     */
    private Long iotDeviceId;
    /**
     * 返回地址
     */
    private String address;
    /**
     * 通道号
     */
    private String channle;
    /**
     * 结果值
     */
    private String value;
    /**
     * 状态
     */
    private String status;
    /**
     * 原始数据
     */
    private byte[] data;
}
