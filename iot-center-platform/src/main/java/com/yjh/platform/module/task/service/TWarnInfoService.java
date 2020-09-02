package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.RobotAlarm;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.dao.TWarnInfoDao;

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
public class TWarnInfoService{

    @Autowired
    private TWarnInfoDao tWarnInfoDao;

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

    //在传入设备内容时，查询结果不正确，
    @Logs(title = "查询所有的告警", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<HashMap<String,Object>> selectAll(Integer warnLevel, Integer confMode, Integer alarmSource, String value, Date startTime, Date endTime,String deviceName) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("warnLevel",warnLevel);
        map.put("confMode",confMode);
        map.put("alarmSource",alarmSource);
        map.put("value",value);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("deviceName",deviceName);
        return tWarnInfoDao.selectAllWarn(map);
    }

    //统计所有的告警，输入deviceName无效 原因：当不需要robot表中的数据时，无法去掉
    @Logs(title = "统计6天内的告警数", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<HashMap<String,Integer>> countSixDay(Integer warnLevel, Integer confMode, Integer alarmSource, String value, Date startTime, Date endTime,String deviceName) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("warnLevel",warnLevel);
        map.put("confMode",confMode);
        map.put("alarmSource",alarmSource);
        map.put("value",value);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("deviceName",deviceName);
        return this.tWarnInfoDao.countSixDay(map);
    }

    //机器人 其他除外的设备类型
    @Logs(title = "根据设备类型来查", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<HashMap<String,Object>> selectByType(Integer warnLevel, Integer confMode, Integer alarmSource, String value, Date startTime, Date endTime,String deviceName,Integer warnType) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("warnLevel",warnLevel);
        map.put("confMode",confMode);
        map.put("alarmSource",alarmSource);
        map.put("value",value);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("deviceName",deviceName);

            if(warnType == 0){
                //设备类型 机器人 其他除外
                map.put("warnType",warnType);
                return this.tWarnInfoDao.selectByType(map);
            }else {
                //其他
                map.put("warnType", warnType);
                return this.tWarnInfoDao.selectOthers(map);
            }

    }

    @Logs(title = "根据设备类型统计6天内的告警数", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<HashMap<String,Integer>> countByTepe(Integer warnLevel, Integer confMode, Integer alarmSource, String value, Date startTime, Date endTime,String deviceName,Integer warnType) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("warnLevel",warnLevel);
        map.put("confMode",confMode);
        map.put("alarmSource",alarmSource);
        map.put("value",value);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("deviceName",deviceName);
            if (warnType == 0) {
                //设备类型 机器人 其他除外
                map.put("warnType", warnType);
                return this.tWarnInfoDao.countByType(map);
            }
            map.put("warnType", warnType);
            return this.tWarnInfoDao.countOthers(map);
    }

    @Logs(title = "根据机器人来查", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<RobotAlarm> selectByRobot(Integer warnLevel, Integer confMode, Integer alarmSource, String value, Date startTime, Date endTime, String deviceName, Integer warnType) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("warnLevel",warnLevel);
        map.put("confMode",confMode);
        map.put("alarmSource",alarmSource);
        map.put("value",value);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("deviceName",deviceName);
        return this.tWarnInfoDao.selectByRobot(map);
    }

    @Logs(title = "根据机器人来统计6天内的告警数", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<HashMap<String,Integer>> countByRobot(Integer warnLevel, Integer confMode, Integer alarmSource, String value, Date startTime, Date endTime, String deviceName, Integer warnType) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("warnLevel",warnLevel);
        map.put("confMode",confMode);
        map.put("alarmSource",alarmSource);
        map.put("value",value);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        return this.tWarnInfoDao.countByRobot(map);
    }

}

