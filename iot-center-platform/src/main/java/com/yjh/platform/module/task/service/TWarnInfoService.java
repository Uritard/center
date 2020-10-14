package com.yjh.platform.module.task.service;

import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.dao.TWarnInfoDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author czh
* @since 2020-08-24
*/
@Service
public class TWarnInfoService {

    @Autowired
    private TWarnInfoDao tWarnInfoDao;
    private DateTimeUtil dateTimeUtil;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TWarnInfo tWarnInfo) {
        return this.tWarnInfoDao.insert(tWarnInfo);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long warnId) {
        return this.tWarnInfoDao.deleteByPrimaryId(warnId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TWarnInfo tWarnInfo) {
        return this.tWarnInfoDao.update(tWarnInfo);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TWarnInfo selectByPrimaryId(Long warnId) {
        return this.tWarnInfoDao.selectByPrimaryId(warnId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfo> select(Long warnId, Integer warnLevel, Date warnTime, Integer warnType, Long deviceId, String cunstomId, Long instanceId, Long stdMeteId, Integer confMode, Date confTime, String confUserId, String confInfo, Integer ifWarnDisable, Integer alarmSource, Integer warnSubtype, String deviceCode, String imagePath, String videoPath, String value, Integer defect, Integer defectLevel, String outRange, String linkMessage) {
        List<TWarnInfo> tWarnInfoList = tWarnInfoDao.select(warnId, warnLevel, warnTime, warnType, deviceId, cunstomId, instanceId, stdMeteId, confMode, confTime, confUserId, confInfo, ifWarnDisable, alarmSource, warnSubtype, deviceCode, imagePath, videoPath, value, defect, defectLevel, outRange, linkMessage);
        return tWarnInfoList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TWarnInfo> list) {
        return this.tWarnInfoDao.batchInsert(list);
    }

    @Logs(title = "查询所有告警", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfoDetail> selectWarnByPage(Integer warnLevel, Integer confMode, Integer alarmSource, Date startTime, Date endTime, String deviceName) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("warnLevel", warnLevel);
        map.put("confMode", confMode);
        map.put("alarmSource", alarmSource);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("deviceName", deviceName);
        return tWarnInfoDao.selectAllWarn(map);
    }
    @Logs(title = "查询所有缺陷", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TDefectInfo> selectDefectByPage(Integer confMode,Integer defectType, Date startTime, Date endTime, String deviceName) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("confMode", confMode);
        map.put("defectType", defectType);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("deviceName", deviceName);
        return tWarnInfoDao.selectAllDefect(map);
    }
    @Logs(title = "统计本周内的告警数", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TutHistoryStatistical> countOnWeek() {
        List<String> weekDates = dateTimeUtil.getDayDateList(8);

        String firstTime1 = weekDates.get(0);
        String firstTime2 = weekDates.get(1);
        String firstTime3 = weekDates.get(2);
        String firstTime4 = weekDates.get(3);
        String firstTime5 = weekDates.get(4);
        String firstTime6 = weekDates.get(5);
        String firstTime7 = weekDates.get(6);
        String firstTime8 = weekDates.get(7);

        Map<String, Integer> map1 = tWarnInfoDao.countOnWeek(firstTime1, firstTime2, firstTime3, firstTime4,
                firstTime5, firstTime6, firstTime7, firstTime8);
        List<TutHistoryStatistical> tutHistoryStatisticalList = new ArrayList<>();

        Iterator<String> iter = map1.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            String timeNode = key.substring(0, 10);
            Number mapValue = (Number) map1.get(key);
            TutHistoryStatistical tutHistoryStatistical = new TutHistoryStatistical();
            tutHistoryStatistical.setTimeNode(timeNode);
            tutHistoryStatistical.setCount(mapValue);
            tutHistoryStatisticalList.add(tutHistoryStatistical);
        }
        Collections.sort(tutHistoryStatisticalList, new Comparator<TutHistoryStatistical>() {
            @Override
            public int compare(TutHistoryStatistical o1, TutHistoryStatistical o2) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = simpleDateFormat.parse(o1.getTimeNode());
                    date2 = simpleDateFormat.parse(o2.getTimeNode());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                int flag = date1.compareTo(date2);
                if (flag == -1) {
                    flag = -1;
                } else if (flag == 1) {
                    flag = 1;
                }
                return flag;
            }
        });
        return tutHistoryStatisticalList;
    }

    @Logs(title = "根据设备类型统计告警数据", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfoDetail> countByDeviceType(){
        List<TJContentInfoDetail> tjContentInfoList = tWarnInfoDao.countByDeviceType();
        System.out.println("tjContentInfoList是："+tjContentInfoList);
        return tjContentInfoList;
}
    @Logs(title = "根据告警处理状态统计告警个数-饼图", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfo> countConfMode() {
        Map<String, Integer> map = tWarnInfoDao.countConfMode();
        List<TJContentInfo> tjContentInfoList = new ArrayList<>();
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Number mapValue =  (Number)map.get(key);
            TJContentInfo tjContentInfo = new TJContentInfo();
            tjContentInfo.setCount(mapValue);
            tjContentInfo.setTJContent(key);
            tjContentInfoList.add(tjContentInfo);
        }
        return tjContentInfoList;
    }
    @Logs(title = "根据告警来源统计告警数据", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfo> countByAlarmSource() {
        Map<String, Integer> map = tWarnInfoDao.countByAlarmSource();
        List<TJContentInfo> tjContentInfoList = new ArrayList<>();
        Iterator<String> iter = map.keySet().iterator();
        while (iter.hasNext()) {
            String key = iter.next();
            Number mapValue =  (Number)map.get(key);
            TJContentInfo tjContentInfo = new TJContentInfo();
            tjContentInfo.setCount(mapValue);
            tjContentInfo.setTJContent(key);
            tjContentInfoList.add(tjContentInfo);
        }
        return tjContentInfoList;
    }
    @Logs(title = "查看告警处理情况", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfoDetail> selectAlarmProcessByPage(Long warnId) {
        return tWarnInfoDao.selectAlarmProcessByPage(warnId);
    }
    //count未确定
    public int updateForOk(Long userId,String warnIds,Integer confMode){
        String[] list = warnIds.split(",");
        TWarnInfo tWarnInfo = new TWarnInfo();
        int i = 0;
        for (String item:list) {
            tWarnInfo.setWarnId(Long.valueOf(item));
            tWarnInfo.setConfUserId(userId.toString());
            tWarnInfo.setConfMode(confMode);
            i =+ this.tWarnInfoDao.update(tWarnInfo);
        }
        return i;
    }

}

