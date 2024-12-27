/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service.impl;

import com.yjh.accesstcp.module.device.entity.XMLBaseModel;
import com.yjh.accesstcp.module.device.service.IUpSystemService;
import com.yjh.accesstcp.module.device.service.uphandler.IUpHandler;
import com.yjh.accesstcp.module.device.service.uphandler.UpHandlerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 科大上级系统接口实现
 * @author Chenfei
 * @date 2024/12/3
 * @since [产品/模块版本] （可选）
 */
@Service(UpType.Name.TEK)
@Slf4j
public class TekUpSystemServiceImpl implements IUpSystemService {

    @Override
    public int sendUp(XMLBaseModel xmlBaseModel) {
        try {
            String type = StringUtils.isEmpty(xmlBaseModel.getCommand()) ? xmlBaseModel.getType() :
                xmlBaseModel.getType() + "_" + xmlBaseModel.getCommand();
            IUpHandler messageHandler = UpHandlerFactory.getService(UpType.TEK, type);
            if (Optional.ofNullable(messageHandler).isPresent()) {
                return messageHandler.sendUpHandler(xmlBaseModel);
            } else {
                log.warn("消息处理未定义，type: {}", type);
                return -1;
            }
        } catch (Exception e) {
            log.error("消息处理失败, {}", xmlBaseModel, e);
        }
        return -1;
    }
}
