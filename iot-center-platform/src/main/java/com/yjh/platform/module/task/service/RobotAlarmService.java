package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.RobotAlarm;
import com.yjh.platform.module.task.dao.RobotAlarmDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author czh
* @since 2020-08-25
*/
@Service
public class RobotAlarmService{

    @Autowired
    private RobotAlarmDao robotAlarmDao;

    @Logs(title = "插入", code = "RobotAlarm",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(RobotAlarm robotAlarm) {
        return this.robotAlarmDao.insert(robotAlarm);
    }

    @Logs(title = "删除", code = "RobotAlarm",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long robotAlarmId) {
        return this.robotAlarmDao.deleteByPrimaryId(robotAlarmId);
    }

    @Logs(title = "更新", code = "RobotAlarm",content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(RobotAlarm robotAlarm) {
        return this.robotAlarmDao.update(robotAlarm);
    }

    @Logs(title = "主键查询", code = "RobotAlarm",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public RobotAlarm selectByPrimaryId(Long robotAlarmId) {
        return this.robotAlarmDao.selectByPrimaryId(robotAlarmId);
    }

    @Logs(title = "查询", code = "RobotAlarm",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<RobotAlarm> select(Long robotAlarmId, String stationId, Integer alarmType, Integer alarmLevel, String alarmInfo, Date alarmTime, Integer dealType, String dealInfo, String dealPersonId, String dealPersonName, String positionStationNum, String positionOffset, Integer alarmState, Date createTime, Date endTime) {
        List<RobotAlarm> robotAlarmList = robotAlarmDao.select(robotAlarmId, stationId, alarmType, alarmLevel, alarmInfo, alarmTime, dealType, dealInfo, dealPersonId, dealPersonName, positionStationNum, positionOffset, alarmState, createTime, endTime);
        return robotAlarmList;
    }

    @Logs(title = "分页查询", code = "RobotAlarm",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<RobotAlarm> selectByPage(RobotAlarm robotAlarm) {
        List<RobotAlarm> robotAlarmList = robotAlarmDao.selectByPage(robotAlarm);
        return robotAlarmList;
    }

    @Logs(title = "批量插入", code = "RobotAlarm",content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<RobotAlarm> list) {
        return this.robotAlarmDao.batchInsert(list);
    }

}

