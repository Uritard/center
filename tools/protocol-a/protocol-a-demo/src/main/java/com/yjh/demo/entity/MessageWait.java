/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.demo.entity;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/4/30
 * @since [产品/模块版本] （可选）
 */
@Data
@Accessors(chain = true)
public class MessageWait {
    private long msgId;
    private String file;

    public MessageWait(long msgId, String file) {
        this.msgId = msgId;
        this.file = file;
    }
}
