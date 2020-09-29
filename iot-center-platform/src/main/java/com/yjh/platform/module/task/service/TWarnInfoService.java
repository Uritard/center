package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.RobotAlarm;
import com.yjh.platform.module.task.entity.TWarnInfo;
import com.yjh.platform.module.task.dao.TWarnInfoDao;

import java.util.*;

import com.yjh.platform.module.task.entity.TWarnInfoDetail;
import org.apache.poi.ss.formula.functions.T;
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

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TWarnInfo> list) {
        return this.tWarnInfoDao.batchInsert(list);
    }

    //在传入设备内容时，查询结果不正确，主要是机器人部分 机器人没有自己的属性表
    //根据设备类型过滤查询未作 设备类型不明确
    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TWarnInfoDetail> selectByPage(Integer warnLevel, Integer confMode, Integer alarmSource, String value, Date startTime, Date endTime, String deviceName,Integer deviceType) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("warnLevel",warnLevel);
        map.put("confMode",confMode);
        map.put("alarmSource",alarmSource);
        map.put("value",value);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("deviceName",deviceName);
        if(map.get("alarmSource") != null){
            if(deviceType != null) {
                List<Long> list = tWarnInfoDao.selectIds(String.valueOf(deviceType));
                map.put("list", list);
                return tWarnInfoDao.selectByPageWarn(map);
            }else {
                return tWarnInfoDao.selectByPageWarn(map);
            }
        }else if(false){//条件不足 未作
            if(false){//条件不足 未作
                return tWarnInfoDao.selectByPageRobot(map);
            }
            return tWarnInfoDao.selectByPageCfg(map);
        }
        //if (map.get() == "机器人")
        return tWarnInfoDao.selectAllWarn(map);

    }

    //统计所有的告警，输入deviceName无效 原因：当不需要robot表中的数据时，无法去掉
    //只能统计所有的告警 未作根据设备类型（消防，主设备）来做过滤
    @Logs(title = "统计本月内的告警数", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<HashMap<String,Integer>> countOnMonth(Integer warnLevel, Integer confMode, Integer alarmSource, String value, Date startTime, Date endTime,String deviceName,Integer deviceType) {
        HashMap<String,Object> map = new HashMap<>();
        map.put("warnLevel",warnLevel);
        map.put("confMode",confMode);
        map.put("alarmSource",alarmSource);
        map.put("value",value);
        map.put("startTime",startTime);
        map.put("endTime",endTime);
        map.put("deviceName",deviceName);
        if(map.get("alarmSource") != null){
            if(deviceType != null) {
                List<Long> list = tWarnInfoDao.selectIds(String.valueOf(deviceType));
                map.put("list", list);
                return tWarnInfoDao.countOnMonthByWarn(map);
            }else {
                return tWarnInfoDao.countOnMonthByWarn(map);
            }
        }else if(false){//条件不足 未作
            if(false){//条件不足 未作
                return tWarnInfoDao.countOnMonthByRobot(map);
            }
            return tWarnInfoDao.countOnMonthByCfg(map);
        }
        //if (map.get() == "机器人")
        return tWarnInfoDao.countOnMonth(map);
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

