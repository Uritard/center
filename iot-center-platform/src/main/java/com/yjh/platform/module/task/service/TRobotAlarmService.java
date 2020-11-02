package com.yjh.platform.module.task.service;

import java.util.List;
import java.util.Date;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.module.task.dao.TRobotAlarmDao;
import com.yjh.platform.module.task.entity.TRobotAlarm;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-10-15
*/
@Service
public class TRobotAlarmService{

    @Autowired
    private TRobotAlarmDao tRobotAlarmDao;

    @Logs(title = "插入", code = "tRobotAlarm",content = "机器人产生告警时插入信息")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotAlarm tRobotAlarm) {
        return this.tRobotAlarmDao.insert(tRobotAlarm);
    }

    @Logs(title = "删除", code = "tRobotAlarm",content = "删除机器人产生的告警信息")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long robotAlarmId) {
        return this.tRobotAlarmDao.deleteByPrimaryId(robotAlarmId);
    }

    @Logs(title = "更新", code = "tRobotAlarm",content = "更新机器人产生的告警信息")
    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotAlarm tRobotAlarm) {
        return this.tRobotAlarmDao.update(tRobotAlarm);
    }

    @Logs(title = "主键查询", code = "tRobotAlarm",content = "查询机器人产生的告警信息")
    @Transactional(rollbackFor = Exception.class)
    public TRobotAlarm selectByPrimaryId(Long robotAlarmId) {
        return this.tRobotAlarmDao.selectByPrimaryId(robotAlarmId);
    }

    @Logs(title = "查询", code = "tRobotAlarm",content = "查询机器人产生的告警信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotAlarm> select(Long robotAlarmId, String alarmName, Long robotId, String stationId, Integer alarmType, Integer alarmLevel, String alarmInfo, Date alarmTime, Integer dealType, String dealInfo, String dealPersonId, Date dealTime, String positionStationNum, String positionOffset, Integer alarmState, Date createTime, Date endTime) {
        List<TRobotAlarm> tRobotAlarmList = tRobotAlarmDao.select(robotAlarmId, alarmName, robotId, stationId, alarmType, alarmLevel, alarmInfo, alarmTime, dealType, dealInfo, dealPersonId, dealTime, positionStationNum, positionOffset, alarmState, createTime, endTime);
        return tRobotAlarmList;
    }

    @Logs(title = "分页查询", code = "tRobotAlarm",content = "查询机器人产生的告警信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TRobotAlarm> selectByPage(TRobotAlarm tRobotAlarm) {
        List<TRobotAlarm> tRobotAlarmList = tRobotAlarmDao.selectByPage(tRobotAlarm);
        return tRobotAlarmList;
    }

    @Logs(title = "批量插入", code = "tRobotAlarm",content = "机器人产生告警时批量插入信息")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotAlarm> list) {
        return this.tRobotAlarmDao.batchInsert(list);
    }

}

