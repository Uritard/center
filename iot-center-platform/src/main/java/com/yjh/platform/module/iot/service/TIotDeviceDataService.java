package com.yjh.platform.module.iot.service;

import com.yjh.platform.module.iot.entity.IotDeviceDataEx;
import com.yjh.platform.module.iot.entity.TIotDeviceData;
import com.baomidou.mybatisplus.extension.service.IService;
import com.yjh.platform.module.task.entity.EnvDeviceStatus;

import java.util.List;
import java.util.Map;

/**
* @author YIJIAHE
* @description 针对表【t_iot_device_data(物联设备结果表)】的数据库操作Service
* @createDate 2023-11-28 14:48:41
*/
public interface TIotDeviceDataService extends IService<TIotDeviceData> {

    List<Map<String, Object>> selectIotData(Long upRegionId, Boolean meterFlag);

    List<List<String>> selectIotLine(Long iotDeviceId, String startTime, String endTime, Boolean meterFlag, Boolean powerFlag);

    List<IotDeviceDataEx> selectIotDataEx(List<Long> regionList, Boolean meterFlag);

    Boolean insertEnvData(List<EnvDeviceStatus> envDeviceStatusList);

    Boolean addToRedis(List<IotDeviceDataEx> dataList);

    void deviceConverted();

    void insertDataFromMeterLog();

    String exportMeterReport(Integer year, Integer month);
}
