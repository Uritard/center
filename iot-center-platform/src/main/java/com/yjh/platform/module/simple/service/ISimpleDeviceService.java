/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.simple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yjh.platform.module.config.entity.SysDiskCleanup;
import com.yjh.platform.module.device.entity.TStdDevice;
import org.springframework.web.multipart.MultipartFile;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2025-06-03
 * @since [产品/模块版本] （可选）
 */
public interface ISimpleDeviceService extends IService<TStdDevice> {

    /**
     * 模型导入
     * @param file
     * @return
     */
    boolean importModel(MultipartFile file);
}
