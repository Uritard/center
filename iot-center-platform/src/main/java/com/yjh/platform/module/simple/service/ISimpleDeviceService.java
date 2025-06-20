/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.simple.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.simple.entity.ModelCommand;
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

    /**
     * 模型下发
     * @param modelSend 模型指令
     */
    void modelSend(ModelCommand modelSend);

    /**
     * 模型导出
     * @param modelSend 模型指令
     * @return 模型路径
     */
    String modelExport(ModelCommand modelSend);
}
