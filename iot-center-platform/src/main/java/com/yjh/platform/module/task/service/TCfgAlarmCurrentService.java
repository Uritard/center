package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TCfgAlarmCurrent;
import com.yjh.platform.module.task.dao.TCfgAlarmCurrentDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author czh
* @since 2020-08-24
*/
@Service
public class TCfgAlarmCurrentService{

    @Autowired
    private TCfgAlarmCurrentDao tCfgAlarmCurrentDao;

    @Logs(title = "插入", code = "TCfgAlarmCurrent",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgAlarmCurrent tCfgAlarmCurrent) {
        return this.tCfgAlarmCurrentDao.insert(tCfgAlarmCurrent);
    }

    @Logs(title = "删除", code = "TCfgAlarmCurrent",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long alarmNo) {
        return this.tCfgAlarmCurrentDao.deleteByPrimaryId(alarmNo);
    }

    @Logs(title = "更新", code = "TCfgAlarmCurrent",content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgAlarmCurrent tCfgAlarmCurrent) {
        return this.tCfgAlarmCurrentDao.update(tCfgAlarmCurrent);
    }

    @Logs(title = "主键查询", code = "TCfgAlarmCurrent",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public TCfgAlarmCurrent selectByPrimaryId(Long alarmNo) {
        return this.tCfgAlarmCurrentDao.selectByPrimaryId(alarmNo);
    }

    @Logs(title = "查询", code = "TCfgAlarmCurrent",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgAlarmCurrent> select(Long alarmNo, Long deviceId, String cunstomId, Long meteId, Date alarmTime, Integer alarmLevel, String alarmValue, String alarmDesc, Integer confirmState, String confirmPeople, Date confirmTime, String confirmRemark, Integer defect, Integer defectLevel, String meteCode, String isClear, String showType, Date updateTime) {
        List<TCfgAlarmCurrent> tCfgAlarmCurrentList = tCfgAlarmCurrentDao.select(alarmNo, deviceId, cunstomId, meteId, alarmTime, alarmLevel, alarmValue, alarmDesc, confirmState, confirmPeople, confirmTime, confirmRemark, defect, defectLevel, meteCode, isClear, showType, updateTime);
        return tCfgAlarmCurrentList;
    }

    @Logs(title = "分页查询", code = "TCfgAlarmCurrent",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgAlarmCurrent> selectByPage(TCfgAlarmCurrent tCfgAlarmCurrent) {
        List<TCfgAlarmCurrent> tCfgAlarmCurrentList = tCfgAlarmCurrentDao.selectByPage(tCfgAlarmCurrent);
        return tCfgAlarmCurrentList;
    }

    @Logs(title = "批量插入", code = "TCfgAlarmCurrent",content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgAlarmCurrent> list) {
        return this.tCfgAlarmCurrentDao.batchInsert(list);
    }

}

