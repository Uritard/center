/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.simple.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.simple.entity.SimpleDeviceModel;

import java.util.List;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2025-06-03
 * @since [产品/模块版本] （可选）
 */
public interface SimpleDeviceMapper extends BaseMapper<TStdDevice> {

    /**
     * 查询简易设备模型
     * @param regionId 区域ID
     * @param isDevice 是否初始设备任务，true 表示初始化设备任务， false 表示标定完成，设备测点模型
     * @return 模型
     */
    List<SimpleDeviceModel> selectDeviceModel(Long regionId, boolean isDevice);

}
