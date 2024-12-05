/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accesstcp.module.device.service;

import com.yjh.accesstcp.module.device.entity.XMLBaseModel;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/12/3
 * @since [产品/模块版本] （可选）
 */
public interface IUpSystemService {

    /**
     * 上送报文给上级系统，根据上级不同使用不同接口实现
     * @param xmlBaseModel 接口内容
     * @return 200 成功  1 成功 -1 失败
     */
    int sendUp(XMLBaseModel xmlBaseModel);
}
