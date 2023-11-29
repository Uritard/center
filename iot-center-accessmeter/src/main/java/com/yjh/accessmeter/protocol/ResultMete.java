/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.protocol;

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
     * 返回地址
     */
    private String address;
    /**
     * 通道号
     */
    private int channle;
    /**
     * 结果值
     */
    private Double value;
    /**
     * 原始数据
     */
    private byte[] data;
}
