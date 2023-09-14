package com.yjh.platform.module.user.dao;


import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.user.entity.TRobotDeviceConfig;
import com.yjh.platform.module.user.entity.TRobotMapNodeDeviceInfo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TRobotDeviceConfigDao {
    int deleteByPrimaryKey(Long deviceConfigId);

    int deleteByRobotId(@Param(value = "robotId") Long robotId);

    int insert(TRobotDeviceConfig record);

    int insertSelective(TRobotDeviceConfig record);

    TRobotDeviceConfig selectByPrimaryKey(Long deviceConfigId);

    int updateByPrimaryKeySelective(TRobotDeviceConfig record);

    int updateByPrimaryKey(TRobotDeviceConfig record);

    List<TRobotDeviceConfig> selectDeviceConfigByRobotId(@Param(value = "robotId") Long robotId);

    List<TRobotMapNodeDeviceInfo> selectMapNodeDeviceByRobotId(Long robotId);

    List<AreaInfo> selectDeviceTree();
}