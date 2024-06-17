/*
 * Copyright (c) 2024 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.accessrobot.module.command.dao;

import com.yjh.accessrobot.module.command.entity.DeviceMaintenanceInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2024/6/14
 * @since [产品/模块版本] （可选）
 */
@Repository
public interface DeviceMaintenanceInfoDao {
    List<DeviceMaintenanceInfo> selectAll(@Param("edgeCode") String edgeCode);

    int batchUpdate(@Param("infoList") List<DeviceMaintenanceInfo> infoList);

    int deleteByCodes(@Param("edgeCode") String edgeCode, @Param("deleteIds") Set<String> deleteIds);

    int insertBatch(@Param("infoList") List<DeviceMaintenanceInfo> infoList);
}
