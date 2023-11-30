package com.yjh.platform.module.iot.dao;

import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.iot.entity.TIotDevice;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* @author YIJIAHE
* @description 针对表【t_iot_device(物联设备表)】的数据库操作Mapper
* @createDate 2023-11-28 14:48:41
* @Entity com.yjh.platform.module.iot.entity.TIotDevice
*/
public interface TIotDeviceMapper extends BaseMapper<TIotDevice> {

    List<AreaInfo> selectDeviceByRegionId(@Param(value = "upRegionId") Long upRegionId);

    List<Long> selectIotDeviceByName(@Param(value = "name") String name);

    List<Long> selectRegionByIotDeviceList(@Param(value = "list") List<Long> iotDeviceList);

    List<AreaInfo> selectIotDeviceByNameTree(@Param(value = "list") List<Long> iotDeviceList,
                                             @Param(value = "regionList") List<Long> regionList);
}




