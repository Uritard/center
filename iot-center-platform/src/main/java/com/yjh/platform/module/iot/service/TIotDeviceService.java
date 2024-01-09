package com.yjh.platform.module.iot.service;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.iot.entity.TIotDevice;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_iot_device(物联设备表)】的数据库操作Service
* @createDate 2023-11-28 14:48:41
*/
public interface TIotDeviceService extends IService<TIotDevice> {

    List<AreaInfo> selectDevTree(String level, Long id, String name);

    void deviceUpload();
}
