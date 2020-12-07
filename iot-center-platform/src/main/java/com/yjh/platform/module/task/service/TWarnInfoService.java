package com.yjh.platform.module.task.service;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.TCameraAlarmDao;
import com.yjh.platform.module.task.dao.TCruiseResultDao;
import com.yjh.platform.module.task.dao.TRobotAlarmDao;
import com.yjh.platform.module.task.dao.TWarnInfoDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.yjh.platform.module.task.entity.*;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

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
    @Autowired
    private TCruiseResultDao tCruiseResultDao;
    @Autowired
    private RedisTemplate redisTemplate;

    private DateTimeUtil dateTimeUtil;

    @Logs(title = "插入", code = "tWarnInfo",content = "根据算法返回的结果插入告警记录")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TWarnInfo tWarnInfo) {
        return this.tWarnInfoDao.insert(tWarnInfo);
    }

    @Logs(title = "删除", code = "tWarnInfo",content = "根据web传递的参数删除告警记录")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long warnId) {
        return this.tWarnInfoDao.deleteByPrimaryId(warnId);
    }

    @Logs(title = "更新", code = "tWarnInfo",content = "根据web传递的参数更新告警记录")
    @Transactional(rollbackFor = Exception.class)
    public int update(TWarnInfo tWarnInfo) {
        return this.tWarnInfoDao.update(tWarnInfo);
    }

    @Logs(title = "主键查询", code = "tWarnInfo",content = "根据web传递的参数查询告警记录")
    @Transactional(rollbackFor = Exception.class)
    public TWarnInfo selectByPrimaryId(Long warnId) {
        return this.tWarnInfoDao.selectByPrimaryId(warnId);
    }

    @Logs(title = "查询", code = "tWarnInfo",content = "根据web传递的参数查询告警记录")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfo> select(Long warnId, Integer warnLevel, Date warnTime, Integer warnType, String warnName, String warnContent, Long deviceId, String cunstomId, Long instanceId, Long stdMeteId, Integer confMode, Integer isWarn, Integer dealType, String dealInfo, String dealPersonId, Date dealTime, Integer ifWarnDisable, Integer alarmSource, Integer warnSubtype, String deviceCode, String imagePath, String videoPath, String value, String outRange, String linkMessage) {
        List<TWarnInfo> tWarnInfoList = tWarnInfoDao.select(warnId, warnLevel, warnTime, warnType, warnName, warnContent, deviceId, cunstomId, instanceId, stdMeteId, confMode, isWarn, dealType, dealInfo, dealPersonId, dealTime, ifWarnDisable, alarmSource, warnSubtype, deviceCode, imagePath, videoPath, value, outRange, linkMessage);
        return tWarnInfoList;
    }

    @Logs(title = "分页查询", code = "tWarnInfo",content = "根据web传递的参数查询告警记录")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfo> selectByPage(TWarnInfo tWarnInfo) {
        List<TWarnInfo> tWarnInfoList = tWarnInfoDao.selectByPage(tWarnInfo);
        return tWarnInfoList;
    }

    @Logs(title = "批量插入", code = "tWarnInfo",content = "根据算法返回的结果批量插入告警记录")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TWarnInfo> list) {
        return this.tWarnInfoDao.batchInsert(list);
    }
    @Logs(title = "查询所有告警", code = "tWarnInfo",content = "根据web传递的参数查询告警记录")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfoDetail> selectWarnByPage(Integer warnLevel, Integer confMode, Integer alarmSource, Date startTime, Date endTime, String deviceName,String meteName) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("warnLevel", warnLevel);
        map.put("confMode", confMode);
        map.put("alarmSource", alarmSource);
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        map.put("deviceName", deviceName);
        map.put("meteName", meteName);
        return tWarnInfoDao.selectAllWarn(map);
    }
    @Logs(title = "统计告警数据", code = "tWarnInfo",content = "根据告警来源统计告警个数")
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
            tjContentInfo.setContent(key);
            tjContentInfoList.add(tjContentInfo);
        }
        return tjContentInfoList;
    }
    @Logs(title = "统计告警个数", code = "tWarnInfo",content = "根据设备类型统计告警个数")
    @Transactional(rollbackFor = Exception.class)
    public List<TJContentInfoDetail> countByDeviceType(){
        List<TJContentInfoDetail> tjContentInfoList = tWarnInfoDao.countByDeviceType();
        System.out.println("tjContentInfoList是："+tjContentInfoList);
        return tjContentInfoList;
    }
    @Logs(title = "统计告警个数", code = "tWarnInfo",content = "统计近一月的所有告警个数")
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
    @Logs(title = "统计告警个数", code = "tWarnInfo",content = "根据告警处理状态统计告警个数")
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
            tjContentInfo.setContent(key);
            tjContentInfoList.add(tjContentInfo);
        }
        return tjContentInfoList;
    }

    @Logs(title = "查看告警处理情况", code = "tWarnInfo",content = "根据web传递的参数查看告警处理情况")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfoDetail> selectAlarmProcess(Long warnId) {
        return tWarnInfoDao.selectAlarmProcess(warnId);
    }
    @Logs(title = "进行告警处理", code = "tWarnInfo",content = "根据web传递的参数进行告警处理")
    @Transactional(rollbackFor = Exception.class)
    public int alarmProcess(TWarnInfo tWarnInfoTemp,String userId) {
        Long warnId = tWarnInfoTemp.getWarnId();
        Integer alarmSource = tWarnInfoTemp.getAlarmSource();
        Integer dealType = tWarnInfoTemp.getDealType();
        String dealInfo = tWarnInfoTemp.getDealInfo();
        Date date = new Date();
        int jieGuo = 0;
        if (alarmSource == 279 ) {
            TRobotAlarm tRobotAlarm = new TRobotAlarm();
            tRobotAlarm.setRobotAlarmId(warnId);
            tRobotAlarm.setDealInfo(dealInfo);
            tRobotAlarm.setDealType(dealType);
            tRobotAlarm.setAlarmState(275);
            tRobotAlarm.setDealTime(date);
            tRobotAlarm.setDealPersonId(userId);
            jieGuo = tRobotAlarmDao.update(tRobotAlarm);
        }else if (alarmSource == 888)
        {
            TCameraAlarm tCameraAlarm = new TCameraAlarm();
            tCameraAlarm.setCameraAlarmId(warnId);
            tCameraAlarm.setDealInfo(dealInfo);
            tCameraAlarm.setDealType(dealType);
            tCameraAlarm.setAlarmState(275);
            tCameraAlarm.setDealTime(date);
            tCameraAlarm.setDealPersonId(userId);
            jieGuo = tCameraAlarmDao.update(tCameraAlarm);
        }else{
            TWarnInfo tWarnInfo = new TWarnInfo();
            tWarnInfo.setWarnId(warnId);
            tWarnInfo.setDealInfo(dealInfo);
            tWarnInfo.setDealType(dealType);
            tWarnInfo.setConfMode(275);
            tWarnInfo.setDealTime(date);
            tWarnInfo.setDealPersonId(userId);
            jieGuo = tWarnInfoDao.update(tWarnInfo);
        }
        return jieGuo;
    }

    @Logs(title = "查询未审核的告警数量",code = "tWarnInfo")
    @Transactional(rollbackFor = Exception.class)
    public Integer selectWarnCountsNonIdentify(){
        //总告警数量=redis中的数量+数据库中的数量
        Set<String> warnKeys=redisScan("warnInfo:");
        Integer finalCounts=warnKeys.size()+tWarnInfoDao.selectWarnCountsNonIdentify();
        return finalCounts;
    }
    //读批量redis
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }
}

