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

    @Transactional(rollbackFor = Exception.class)
    public int insert(TRobotAlarm tRobotAlarm) {
        return this.tRobotAlarmDao.insert(tRobotAlarm);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long robotAlarmId) {
        return this.tRobotAlarmDao.deleteByPrimaryId(robotAlarmId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TRobotAlarm tRobotAlarm) {
        return this.tRobotAlarmDao.update(tRobotAlarm);
    }

    @Transactional(rollbackFor = Exception.class)
    public TRobotAlarm selectByPrimaryId(Long robotAlarmId) {
        return this.tRobotAlarmDao.selectByPrimaryId(robotAlarmId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TRobotAlarm> select(Long robotAlarmId, String alarmName, Long robotId,String robotName, String stationId, Integer alarmType, Integer alarmLevel, String alarmInfo, Date alarmTime, Integer dealType, String dealInfo, String dealPersonId, Date dealTime, String positionStationNum, String positionOffset, Integer alarmState, Date createTime, Date endTime) {
        List<TRobotAlarm> tRobotAlarmList = tRobotAlarmDao.select(robotAlarmId, alarmName, robotId, robotName,stationId, alarmType, alarmLevel, alarmInfo, alarmTime, dealType, dealInfo, dealPersonId, dealTime, positionStationNum, positionOffset, alarmState, createTime, endTime);
        return tRobotAlarmList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TRobotAlarm> selectByPage(TRobotAlarm tRobotAlarm) {
        List<TRobotAlarm> tRobotAlarmList = tRobotAlarmDao.selectByPage(tRobotAlarm);
        return tRobotAlarmList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TRobotAlarm> list) {
        return this.tRobotAlarmDao.batchInsert(list);
    }

}

