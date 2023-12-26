package com.yjh.platform.module.iot.service;

import com.yjh.platform.module.iot.entity.TIotDeviceWarn;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yjh.platform.module.task.entity.TWarnInfoDetail;

import java.util.Map;

/**
* @author YIJIAHE
* @description 针对表【t_iot_device_warn(物联设备告警表)】的数据库操作Service
* @createDate 2023-12-19 17:58:29
*/
public interface TIotDeviceWarnService extends IService<TIotDeviceWarn> {

    Boolean robotIotWarn(Map<String, String> iotWarn);

    TWarnInfoDetail selectIotDeviceWarn(Long warnId);

    Boolean update(TIotDeviceWarn tIotDeviceWarn);
}
