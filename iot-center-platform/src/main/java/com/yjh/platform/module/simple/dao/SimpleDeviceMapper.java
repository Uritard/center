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
     * 查询简易设备模型，若点位已标定，使用点位ID做deviceId，若未标定，使用设备ID做deviceId
     * @param regionId 区域ID
     * @return 模型
     */
    List<SimpleDeviceModel> selectDeviceModel(Long regionId);

}
