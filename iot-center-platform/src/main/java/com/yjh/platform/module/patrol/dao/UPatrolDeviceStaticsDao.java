package com.yjh.platform.module.patrol.dao;

import com.yjh.platform.module.patrol.entity.DeviceStaticsInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Repository
public interface UPatrolDeviceStaticsDao {

    /**
     * 通过deviceCode查找数据
     * @param deviceCode
     * @return
     */
     Map<String,Object> selectByDeviceCode(String deviceCode);

    void insert(DeviceStaticsInfo staticsInfo);

    void updateByDeviceCode(DeviceStaticsInfo staticsInfo);

    List<Map<String, Object>> selectStatisticsRobot(Long robotId,String type);
    Map<String, Object> selectRobotInfo(Long robotId);
    Map<String, Object>  selectCommissionDays(Long robotId);
    Map<String, Object>  selectCommissionDaysForCamera(Long robotId);
    Integer selectNormalDays(String deviceCode);

    List<Map<String, Object>> selectStatisticsDrone(Long robotId);

    Integer batchInsert(@Param("list") List<DeviceStaticsInfo> deviceStaticsInfos);
    Integer batchDelete(@Param("list") Set<String> idList);

    List<Map<String, Object>> selectRobotStaticsInfo(@Param("startIndex")int startIndex, @Param("endIndex")int endIndex);
}
