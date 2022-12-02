package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.DeviceStaticsInfo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface UPatrolDeviceStaticsDao {

    /**
     * 通过deviceCode查找数据
     * @param deviceCode
     * @return
     */
     Map<String,String> selectByDeviceCode(String deviceCode);

    void insert(DeviceStaticsInfo staticsInfo);

    void updateByDeviceCode(DeviceStaticsInfo staticsInfo);

    List<Map<String, Object>> selectStatisticsRobot(Long robotId);

    List<Map<String, Object>> selectStatisticsDrone(Long robotId);
}
