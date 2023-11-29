/*
 * Copyright (c) 2023 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessmeter.module.dao;

import com.yjh.accessmeter.module.device.entity.IotDevice;
import com.yjh.accessmeter.module.device.entity.IotDeviceData;
import com.yjh.accessmeter.module.device.entity.IotDevicePoint;
import com.yjh.accessmeter.module.device.entity.TMeter;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2023/11/28
 * @since [产品/模块版本] （可选）
 */
@Repository
public interface TIotDeviceDao {

    List<IotDevice> selectAll();

    IotDevice selectByPrimaryKey(Long id);

    List<IotDevicePoint> selectPointByDeviceId(@Param(value = "deviceId") Long deviceId);

    int insertData(IotDeviceData deviceData);

    int batchInsertData(@Param("list") List<IotDeviceData> deviceDataList);
}
