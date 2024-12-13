package com.yjh.platform.module.iot.dao;

import com.yjh.platform.module.iot.entity.IotDeviceDataEx;
import com.yjh.platform.module.iot.entity.TIotDeviceData;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
* @author YIJIAHE
* @description 针对表【t_iot_device_data(物联设备结果表)】的数据库操作Mapper
* @createDate 2023-11-28 14:48:41
* @Entity com.yjh.platform.module.iot.entity.TIotDeviceData
*/
public interface TIotDeviceDataMapper extends BaseMapper<TIotDeviceData> {

    List<IotDeviceDataEx> selectInfoByIp(@Param(value = "ip") String ip);

    List<IotDeviceDataEx> selectIotData(@Param(value = "list")List<Long> list, @Param(value = "iotDeviceType") Integer iotDeviceType);

    List<TIotDeviceData> selectMeterData(@Param(value = "list") List<Long> list);

    List<TIotDeviceData> selectMeterLine(@Param(value = "iotDeviceId") Long iotDeviceId,
                                         @Param(value = "startTime") String startTime,
                                         @Param(value = "endTime") String endTime);

    List<Map<String,String>> countPowerTotalByEdge(@Param(value = "startTime") LocalDateTime startTime,
                                                   @Param(value = "endTime")LocalDateTime endTime);

    List<TIotDeviceData> selectMasterMeterList(@Param(value = "startTime") Date startTime,
                                               @Param(value = "endTime") Date endTime);
}




