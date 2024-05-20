package com.yjh.platform.module.maintain.dao;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.maintain.entity.DeviceMaintenanceInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【device_maintenance_info(设备维护信息表)】的数据库操作Mapper
* @createDate 2024-04-25 11:22:27
* @Entity com.yjh.platform.module.maintain.entity.DeviceMaintenanceInfo
*/
public interface DeviceMaintenanceInfoMapper extends BaseMapper<DeviceMaintenanceInfo> {

    List<AreaInfo> selectCruiseDeviceTree();
}




