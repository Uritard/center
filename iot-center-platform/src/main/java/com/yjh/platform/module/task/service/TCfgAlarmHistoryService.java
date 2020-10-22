package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TCfgAlarmHistory;
import com.yjh.platform.module.task.dao.TCfgAlarmHistoryDao;

import java.math.BigDecimal;
import java.util.List;
import java.util.Date;

import com.yjh.platform.module.task.entity.TCfgAlarmHistoryResultInfo;
import com.yjh.platform.module.task.entity.TUnionTaskAttr;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author czh
* @since 2020-08-24
*/
@Service
public class TCfgAlarmHistoryService{

    @Autowired
    private TCfgAlarmHistoryDao tCfgAlarmHistoryDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCfgAlarmHistory tCfgAlarmHistory) {
        return this.tCfgAlarmHistoryDao.insert(tCfgAlarmHistory);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long alarmNo) {
        return this.tCfgAlarmHistoryDao.deleteByPrimaryId(alarmNo);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCfgAlarmHistory tCfgAlarmHistory) {
        return this.tCfgAlarmHistoryDao.update(tCfgAlarmHistory);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCfgAlarmHistory selectByPrimaryId(Long alarmNo) {
        return this.tCfgAlarmHistoryDao.selectByPrimaryId(alarmNo);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgAlarmHistory> select(Long alarmNo, Long deviceId, String cunstomId, Long meteId, Date alarmTime, Integer alarmLevel, String alarmValue, String alarmDesc, Date clearTime, BigDecimal clearValue, Integer confirmState, String confirmPeople, Date confirmTime, String confirmRemark, Integer defect, Integer defectLevel, String forceClearReason, String meteCode, String isClear, String showType, Date updateTime) {
        List<TCfgAlarmHistory> tCfgAlarmHistoryList = tCfgAlarmHistoryDao.select(alarmNo, deviceId, cunstomId, meteId, alarmTime, alarmLevel, alarmValue, alarmDesc, clearTime, clearValue, confirmState, confirmPeople, confirmTime, confirmRemark, defect, defectLevel, forceClearReason, meteCode, isClear, showType, updateTime);
        return tCfgAlarmHistoryList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgAlarmHistory> selectByPage(Long alarmNo, Long deviceId, String cunstomId, Long meteId, Date alarmTime, Integer alarmLevel, String alarmValue, String alarmDesc, Date clearTime, BigDecimal clearValue, Integer confirmState, String confirmPeople, Date confirmTime, String confirmRemark, Integer defect, Integer defectLevel, String forceClearReason, String meteCode, String isClear, String showType, Date updateTime) {
        List<TCfgAlarmHistory> tCfgAlarmHistoryList = tCfgAlarmHistoryDao.selectByPage(alarmNo, deviceId, cunstomId, meteId, alarmTime, alarmLevel, alarmValue, alarmDesc,
                clearTime, clearValue, confirmState, confirmPeople, confirmTime, confirmRemark, defect, defectLevel, forceClearReason, meteCode,
                isClear, showType, updateTime);
        return tCfgAlarmHistoryList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCfgAlarmHistory> list) {
        return this.tCfgAlarmHistoryDao.batchInsert(list);
    }

    @Logs(title = "查询联动详细结果", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCfgAlarmHistoryResultInfo> selectUnionCruiseResult(String unionId) {
        return tCfgAlarmHistoryDao.selectUnionCruiseResult(unionId);
    }

}

