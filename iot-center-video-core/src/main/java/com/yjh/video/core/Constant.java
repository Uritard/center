/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.video.core;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/1/4
 * @since [产品/模块版本] （可选）
 */
public interface Constant {
    /**
     * 播流最大并发个数
     */
    Integer MAX_STRTEAM_COUNT = 10000;

    String  REGISTER_EXPIRE_TASK_KEY_PREFIX = "device-register-expire-";

    String ZLM_KEEPALIVE_KEY_PREFIX = "zlm-keepalive_";

    String REGISTER_KEY_PREFIX = "platform_register_";

    String KEEPALIVE_KEY_PREFIX = "platform_keepalive_";
}
