package com.yjh.platform.module.maintain.service;

import com.yjh.platform.module.maintain.entity.DeviceMaintenanceInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
* @author YIJIAHE
* @description 针对表【device_maintenance_info(设备维护信息表)】的数据库操作Service
* @createDate 2024-04-25 11:22:27
*/
public interface DeviceMaintenanceInfoService extends IService<DeviceMaintenanceInfo> {

    /**
     * 查询设备最近一次维护信息
     * @param deviceId 设备Id
     * @return
     */
    Map<String, List<DeviceMaintenanceInfo>> selectLastTime(Long deviceId);
}
