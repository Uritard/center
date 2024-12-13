/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service;

import com.yjh.accesstcp.module.device.service.impl.UpType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * 向上级上送数据接口工厂类，根据传入类型获取对应接口实现
 *
 * @author Chenfei
 * @date 2024/12/3
 * @since [产品/模块版本] （可选）
 */
@Service
@RequiredArgsConstructor
public class UpSystemServiceFactory {
    /**
     * 所有协议实现接口集合
     */
    private final Map<String, IUpSystemService> upSystemServiceMap;

    /**
     * 获取对应协议
     */
    public IUpSystemService getService(UpType type) {
        return upSystemServiceMap.get(type.getImplName());
    }

}
