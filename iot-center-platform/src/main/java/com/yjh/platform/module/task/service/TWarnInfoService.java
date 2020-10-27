package com.yjh.platform.module.task.service;

import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.TCameraAlarmDao;
import com.yjh.platform.module.task.dao.TRobotAlarmDao;
import com.yjh.platform.module.task.dao.TWarnInfoDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.yjh.platform.module.task.entity.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-10-15
*/
@Service
public class TWarnInfoService{

    @Autowired
    private TWarnInfoDao tWarnInfoDao;
    @Autowired
    private TCameraAlarmDao tCameraAlarmDao;
    @Autowired
    private TRobotAlarmDao tRobotAlarmDao;

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
    public List<TWarnInfo> select(Long warnId, Integer warnLevel, Date warnTime, Integer warnType, String warnName, String warnContent, Long deviceId, String cunstomId, Long instanceId, Long stdMeteId, Integer confMode, Integer isWarn, Integer dealType, String dealInfo, String dealPersonId, Date dealTime, Integer ifWarnDisable, Integer alarmSource, Integer warnSubtype, String deviceCode, String imagePath, String videoPath, String value, String outRange, String linkMessage) {
        List<TWarnInfo> tWarnInfoList = tWarnInfoDao.select(warnId, warnLevel, warnTime, warnType, warnName, warnContent, deviceId, cunstomId, instanceId, stdMeteId, confMode, isWarn, dealType, dealInfo, dealPersonId, dealTime, ifWarnDisable, alarmSource, warnSubtype, deviceCode, imagePath, videoPath, value, outRange, linkMessage);
        return tWarnInfoList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfo> selectByPage(TWarnInfo tWarnInfo) {
        List<TWarnInfo> tWarnInfoList = tWarnInfoDao.selectByPage(tWarnInfo);
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
    @Logs(title = "根据设备类型统计告警数据", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfoDetail> countByDeviceType(){
        List<TJContentInfoDetail> tjContentInfoList = tWarnInfoDao.countByDeviceType();
        System.out.println("tjContentInfoList是："+tjContentInfoList);
        return tjContentInfoList;
    }
    @Logs(title = "统计近一月的所有告警个数", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> countWarnOnMonth() {
        List<String> monthDates = dateTimeUtil.getDayDateList(30);
        String firstTime1 = monthDates.get(0);
        Date startingTime = dateTimeUtil.parse(firstTime1);
        String endTime = dateTimeUtil.getDayBefore(startingTime);
        String startTime = monthDates.get(29);
        List<WarnStatistical> list = tWarnInfoDao.countWarnOnMonth(startTime,endTime);
        List<WarnStatistical> list1 = new ArrayList<>();

        for (int i = 0;i < monthDates.size();i++){
            int flag = 0;
            for (int j = 0;j < list.size();j++){
                if (monthDates.get(i).substring(0, 10).equals(list.get(j).getTimeNode())){
                    flag++;
                }
            }
            if (flag == 0){
                WarnStatistical ws = new WarnStatistical();
                ws.setCount(0);
                ws.setTimeNode(monthDates.get(i).substring(0, 10));
                list1.add(ws);
            }
        }
        list.addAll(list1);
        Collections.sort(list, new Comparator<WarnStatistical>() {
            @Override
            public int compare(WarnStatistical o1, WarnStatistical o2) {
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
        return list;
    }
    @Logs(title = "根据告警处理状态统计告警个数-饼图", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfo> countWarnConfMode() {
        Map<String, Integer> map = tWarnInfoDao.countWarnConfMode();
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
    public List<TWarnInfoDetail> selectAlarmProcess(Long warnId) {
        return tWarnInfoDao.selectAlarmProcess(warnId);
    }
    @Logs(title = "进行告警处理", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int alarmProcess(Long warnId,Integer alarmSource,Integer dealType,String dealInfo) {
        int jieGuo = 0;
        if (alarmSource == 279 ) {
            TRobotAlarm tRobotAlarm = new TRobotAlarm();
            tRobotAlarm.setRobotAlarmId(warnId);
            tRobotAlarm.setDealInfo(dealInfo);
            tRobotAlarm.setDealType(dealType);
            jieGuo = tRobotAlarmDao.update(tRobotAlarm);
        }else if (alarmSource == 888)
        {
            TCameraAlarm tCameraAlarm = new TCameraAlarm();
            tCameraAlarm.setCameraAlarmId(warnId);
            tCameraAlarm.setDealInfo(dealInfo);
            tCameraAlarm.setDealType(dealType);
            jieGuo = tCameraAlarmDao.update(tCameraAlarm);
        }else{
            TWarnInfo tWarnInfo = new TWarnInfo();
            tWarnInfo.setWarnId(warnId);
            tWarnInfo.setDealInfo(dealInfo);
            tWarnInfo.setDealType(dealType);
            jieGuo = tWarnInfoDao.update(tWarnInfo);
        }
        return jieGuo;
    }
}

