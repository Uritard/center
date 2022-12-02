package com.yjh.platform.module.patrol.service;

import com.yjh.platform.module.patrol.dao.UPatrolDeviceStaticsDao;
import com.yjh.platform.module.patrol.entity.DeviceStaticsInfo;
import com.yjh.platform.module.task.dao.StatisticsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

@Service
public class UPatrolDeviceStaticsService {

    @Autowired
    private UPatrolDeviceStaticsDao uPatrolDeviceStaticsDao;

    @Autowired
    private StatisticsDao statisticsDao;

    /**
     * 表中存在则更新，否则插入
     * @param statusList
     */
    public void insertOrUpdate(List<Map<String, String>> statusList) {
        statusList.forEach(staticsInfo -> {
            String deviceCode = staticsInfo.get("patroldevice_code");
            DeviceStaticsInfo deviceStaticsInfo = new DeviceStaticsInfo()
                    .setDeviceCode(deviceCode)
                    .setDeviceName(staticsInfo.get("patroldevice_name"))
                    .setDuration(staticsInfo.get("duration"))
                    .setCommissionDays(staticsInfo.get("commissionDays"))
                    .setCruisePercent(staticsInfo.get("cruisePercent"))
                    .setIntactPercent(staticsInfo.get("intactPercent"))
                    .setOfflineCount(staticsInfo.get("offLineCount"))
                    .setNormalDay(staticsInfo.get("normalDay"));
            if(!CollectionUtils.isEmpty(statisticsDao.selectStatisticsRobot(Long.valueOf(deviceStaticsInfo.getDeviceCode())))){
                deviceStaticsInfo.setDeviceType(0);
            }
            if(!CollectionUtils.isEmpty(statisticsDao.selectStatisticsDrone(Long.valueOf(deviceStaticsInfo.getDeviceCode())))){
                deviceStaticsInfo.setDeviceType(1);
            }
            if(!CollectionUtils.isEmpty(statisticsDao.countCamera(Long.valueOf(deviceStaticsInfo.getDeviceCode())))){
                deviceStaticsInfo.setDeviceType(2);
            }
            Map<String, String> info = uPatrolDeviceStaticsDao.selectByDeviceCode(deviceCode);
            if (CollectionUtils.isEmpty(info)) {
                uPatrolDeviceStaticsDao.insert(deviceStaticsInfo);
            } else {
                uPatrolDeviceStaticsDao.updateByDeviceCode(deviceStaticsInfo);
            }

        });
    }
}
