package com.yjh.platform.module.iot.service;

import com.yjh.platform.module.iot.entity.TIotDevicePoint;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_iot_device_point(物联设备测点表)】的数据库操作Service
* @createDate 2023-11-28 14:48:41
*/
public interface TIotDevicePointService extends IService<TIotDevicePoint> {

    List<TIotDevicePoint> listByDeviceId(Long deviceId);
}
