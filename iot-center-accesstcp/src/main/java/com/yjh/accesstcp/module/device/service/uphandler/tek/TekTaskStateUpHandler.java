/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.uphandler.tek;

import com.yjh.accesstcp.commons.result.Result;
import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.uphandler.UpHandlerEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2024/12/5
 * @since [产品/模块版本] （可选）
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TekTaskStateUpHandler extends AbstractTekHandler {
    @Override
    public UpHandlerEnum subType() {
        return UpHandlerEnum.TASK_STATE;
    }

    @Override
    public int sendUpHandler(XMLBaseModel xmlBaseModel) {

        return Result.SUCCESS;
    }

}
