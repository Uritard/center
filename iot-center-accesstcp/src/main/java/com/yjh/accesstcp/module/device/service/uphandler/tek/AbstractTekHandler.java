/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler.tek;

import com.yjh.accesstcp.module.device.service.impl.UpType;
import com.yjh.accesstcp.module.device.service.uphandler.IUpHandler;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/12/4
 * @since [产品/模块版本] （可选）
 */
public abstract class AbstractTekHandler implements IUpHandler {

    @Override
    public UpType upType() {
        return UpType.TEK;
    }
}
