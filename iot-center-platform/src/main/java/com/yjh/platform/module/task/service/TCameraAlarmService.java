package com.yjh.platform.module.task.service;

import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;
import java.util.Date;
import com.github.pagehelper.PageHelper;
import com.yjh.platform.module.task.dao.TCameraAlarmDao;
import com.yjh.platform.module.task.entity.TCameraAlarm;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-10-15
*/
@Service
public class TCameraAlarmService{

    @Autowired
    private TCameraAlarmDao tCameraAlarmDao;

    @Logs(title = "插入", code = "tCameraAlarm",content = "可视设备本体产生告警插入信息")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCameraAlarm tCameraAlarm) {
        return this.tCameraAlarmDao.insert(tCameraAlarm);
    }

    @Logs(title = "删除", code = "tCameraAlarm",content = "删除可视设备本体产生告警的信息")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long cameraAlarmId) {
        return this.tCameraAlarmDao.deleteByPrimaryId(cameraAlarmId);
    }

    @Logs(title = "更新", code = "tCameraAlarm",content = "更新可视设备本体产生告警的信息")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCameraAlarm tCameraAlarm) {
        return this.tCameraAlarmDao.update(tCameraAlarm);
    }

    @Logs(title = "主键查询", code = "tCameraAlarm",content = "查询可视设备本体产生告警的信息")
    @Transactional(rollbackFor = Exception.class)
    public TCameraAlarm selectByPrimaryId(Long cameraAlarmId) {
        return this.tCameraAlarmDao.selectByPrimaryId(cameraAlarmId);
    }

    @Logs(title = "查询", code = "tCameraAlarm",content = "查询可视设备本体产生告警的信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraAlarm> select(Long cameraAlarmId, String alarmName, Long cameraId, String stationId, Integer alarmType, Integer alarmLevel, String alarmInfo, Date alarmTime, Integer isAlarm, Integer dealType, String dealInfo, String dealPersonId, Date dealTime, Integer alarmState, Date createTime, Date endTime) {
        List<TCameraAlarm> tCameraAlarmList = tCameraAlarmDao.select(cameraAlarmId, alarmName, cameraId, stationId, alarmType, alarmLevel, alarmInfo, alarmTime, isAlarm, dealType, dealInfo, dealPersonId, dealTime, alarmState, createTime, endTime);
        return tCameraAlarmList;
    }

    @Logs(title = "分页查询", code = "tCameraAlarm",content = "查询可视设备本体产生告警的信息")
    @Transactional(rollbackFor = Exception.class)
    public List<TCameraAlarm> selectByPage(TCameraAlarm tCameraAlarm) {
        List<TCameraAlarm> tCameraAlarmList = tCameraAlarmDao.selectByPage(tCameraAlarm);
        return tCameraAlarmList;
    }

    @Logs(title = "批量插入", code = "tCameraAlarm",content = "可视设备本体产生告警批量插入信息")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCameraAlarm> list) {
        return this.tCameraAlarmDao.batchInsert(list);
    }

}

