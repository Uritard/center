/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.impl;

import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.IUpSystemService;
import com.yjh.accesstcp.module.device.service.SendToUpSystemServices;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 电网上级系统接口实现
 *
 * @author Chenfei
 * @date 2024/12/3
 * @since [产品/模块版本] （可选）
 */
@Service(UpType.Name.STATE_GRID)
@RequiredArgsConstructor
public class StateGridUpSystemServiceImpl implements IUpSystemService {
    private final SendToUpSystemServices sendToUpSystemServices;

    @Override
    public int sendUp(XMLBaseModel xmlBaseModel) {
        return sendToUpSystemServices.sendXML(xmlBaseModel);
    }
}
