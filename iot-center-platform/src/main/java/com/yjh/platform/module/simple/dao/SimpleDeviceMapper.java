/*
 * Copyright (c) 2025 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.simple.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yjh.platform.module.device.entity.TStdDevice;
import com.yjh.platform.module.simple.entity.SimpleDeviceModel;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <功能描述>
 * @author Chenfei
 * @date 2025-06-03
 * @since [产品/模块版本] （可选）
 */
public interface SimpleDeviceMapper extends BaseMapper<TStdDevice> {

    /**
     * 查询简易设备模型，初始设备
     * @param regionId 区域ID
     * @return 模型
     */
    List<SimpleDeviceModel> selectDeviceModel(@Param("regionId") Long regionId);

    /**
     * 查询简易设备点位模型
     * @param regionId 区域ID
     * @param robotId 机器人ID
     * @return 模型
     */
    List<SimpleDeviceModel> selectDevicePointModel(@Param("regionId") Long regionId, @Param("robotId") Long robotId);
}
