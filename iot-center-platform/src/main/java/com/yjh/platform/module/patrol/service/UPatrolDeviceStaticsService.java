package com.yjh.platform.module.patrol.service;

import com.google.common.collect.Maps;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.patrol.dao.UPatrolDeviceStaticsDao;
import com.yjh.platform.module.patrol.entity.DeviceStaticsInfo;
import com.yjh.platform.module.task.dao.StatisticsDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UPatrolDeviceStaticsService {


    @Autowired
    private UPatrolDeviceStaticsDao uPatrolDeviceStaticsDao;

    @Autowired
    private StatisticsDao statisticsDao;

    /**
     * 表中存在则更新，否则插入
     *
     * @param statistic
     */
    public void insertOrUpdate(Map<String,Object> statistic) {
        String regionCode = statistic.get("regionCode").toString();
        List<Map<String,String>> statisticInfoList = (List) statistic.get("list");
        Map<String, DeviceStaticsInfo> statisticMap = Maps.newHashMap();
        statisticInfoList.forEach(statisticInfo -> {
            DeviceStaticsInfo deviceStaticsInfo;
            //若结果集中已存在待处理信息，取出继续处理
            if (statisticMap.keySet().contains(statisticInfo.get("patroldevice_code"))) {
                deviceStaticsInfo = statisticMap.get(statisticInfo.get("patroldevice_code"));
            }
            //不存在则新建对象
            else {
                deviceStaticsInfo = new DeviceStaticsInfo();
                deviceStaticsInfo.setDeviceCode(statisticInfo.get("patroldevice_code"));
                deviceStaticsInfo.setDeviceName(statisticInfo.get("patroldevice_name"));
                deviceStaticsInfo.setDeviceType(statisticInfo.get("device_type"));
                deviceStaticsInfo.setRegionCode(regionCode);
                deviceStaticsInfo.setDeviceResumeDate(DateTimeUtil.getDateTimeString(new Date()));
                statisticMap.put(statisticInfo.get("patroldevice_code"),deviceStaticsInfo);
            }
            //处理上报信息
            switch (statisticInfo.get("type")) {
                case StatisticType.DURATION:
                    deviceStaticsInfo.setDuration("1".equals(statisticInfo.get("value_unit"))? statisticInfo.get("value") + statisticInfo.get("unit") :statisticInfo.get("value"));
                    break;
                case StatisticType.OFF_LINE_COUNT:
                    deviceStaticsInfo.setOfflineCount("1".equals(statisticInfo.get("value_unit"))? statisticInfo.get("value") + statisticInfo.get("unit") :statisticInfo.get("value"));
                    break;
                case StatisticType.NORMAL_DAY:
                    deviceStaticsInfo.setNormalDay("1".equals(statisticInfo.get("value_unit"))? statisticInfo.get("value") + statisticInfo.get("unit") :statisticInfo.get("value"));
                    break;
                case StatisticType.COMMISSION_DAY:
                    deviceStaticsInfo.setCommissionDays("1".equals(statisticInfo.get("value_unit"))? statisticInfo.get("value") + statisticInfo.get("unit") :statisticInfo.get("value"));
                    break;
                case StatisticType.CRUISE_PERCENT:
                    deviceStaticsInfo.setCruisePercent("1".equals(statisticInfo.get("value_unit"))? statisticInfo.get("value") + statisticInfo.get("unit") :statisticInfo.get("value"));
                    break;
                case StatisticType.INTACT_PERCENT:
                    deviceStaticsInfo.setIntactPercent("1".equals(statisticInfo.get("value_unit"))? statisticInfo.get("value") + statisticInfo.get("unit") :statisticInfo.get("value"));
                    break;
            }
        });
        List<DeviceStaticsInfo> deviceStaticsInfos = new ArrayList<>(statisticMap.values());
        Set<String> codeSet = statisticMap.keySet();
        if (CollectionUtils.isEmpty(codeSet)) {
            return;
        }
        uPatrolDeviceStaticsDao.batchDelete(codeSet);
        uPatrolDeviceStaticsDao.batchInsert(deviceStaticsInfos);

    }


    private static class StatisticType {
        public static final String DURATION = "1";
        public static final String OFF_LINE_COUNT = "2";
        public static final String NORMAL_DAY = "3";
        public static final String COMMISSION_DAY = "4";
        public static final String CRUISE_PERCENT = "5";
        public static final String INTACT_PERCENT = "6";

    }
}
