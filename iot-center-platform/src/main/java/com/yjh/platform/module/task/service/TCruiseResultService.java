package com.yjh.platform.module.task.service;

import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.dao.TCruiseResultDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author czh
 * @since 2020-08-25
 */
@Service
public class TCruiseResultService{

    @Autowired
    private TCruiseResultDao tCruiseResultDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseResult tCruiseResult) {
        return this.tCruiseResultDao.insert(tCruiseResult);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskResultId) {
        return this.tCruiseResultDao.deleteByPrimaryId(taskResultId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseResult tCruiseResult) {
        return this.tCruiseResultDao.update(tCruiseResult);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectByPrimaryId(String taskResultId) {
        return this.tCruiseResultDao.selectByPrimaryId(taskResultId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseResult> select(String taskResultId, String taskId, String areaId, Integer cType, Integer cState, Integer modifyState, Integer taskCount, Integer taskWait, String checkUser, Date checkDate, String weather, Date createTime, Date executeTime, String taskCode, String remark) {
        List<TCruiseResult> tCruiseResultList = tCruiseResultDao.select(taskResultId, taskId, areaId, cType, cState, modifyState, taskCount, taskWait, checkUser, checkDate, weather, createTime, executeTime, taskCode, remark);
        return tCruiseResultList;
    }

    @Logs(title = "分页查询--巡视结果确认", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseResultExpand> selectTaskByPage(TCruiseResultExpand tCruiseResultExpand) {
        List<TCruiseResultExpand> tCruiseResultExpandList = tCruiseResultDao.selectTaskByPage(tCruiseResultExpand);
        return tCruiseResultExpandList;
    }
    @Logs(title = "分页查询--任务结果详细", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultDetail> selectCruiseByPage( CruiseResultDetail cruiseResultDetail) {
        List<CruiseResultDetail> cruiseResultDetailList = tCruiseResultDao.selectCruiseByPage(cruiseResultDetail);
        return cruiseResultDetailList;
    }
    @Logs(title = "巡视点结果操作", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public CruiseResultDetail cruiseResultOperate(CruiseResultDetail cruiseResultDetail) {
        return this.tCruiseResultDao.cruiseResultOperate(cruiseResultDetail);
    }
    @Logs(title = "人工复核", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int manualReview(CruiseManualReview cruiseManualReview) {
        return this.tCruiseResultDao.manualReview(cruiseManualReview);
    }
    @Logs(title = "巡视任务结果统计", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TaskStatistical> taskStatistical() {
        String weekStart = DateTimeUtil.getWeekStart();
        String weekEnd = DateTimeUtil.getWeekEnd();
        List<TaskStatistical> taskStatisticalList = new ArrayList<>();
        taskStatisticalList.add(tCruiseResultDao.taskStatistical(weekStart,weekEnd));
        String lastWeekStart = DateTimeUtil.getLastWeekStart();
        String lastWeekend = DateTimeUtil.getLastWeekend();
        taskStatisticalList.add(tCruiseResultDao.taskStatistical(lastWeekStart,lastWeekend));
        return taskStatisticalList;
    }
    @Logs(title = "巡视点结果统计", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseStatistical> cruiseStatistical() {
        String weekStart = DateTimeUtil.getWeekStart();
        String weekEnd = DateTimeUtil.getWeekEnd();
        List<CruiseStatistical> cruiseStatisticalList = new ArrayList<>();
        cruiseStatisticalList.add(tCruiseResultDao.cruiseStatistical(weekStart,weekEnd));
        String lastWeekStart = DateTimeUtil.getLastWeekStart();
        String lastWeekend = DateTimeUtil.getLastWeekend();
        cruiseStatisticalList.add(tCruiseResultDao.cruiseStatistical(lastWeekStart,lastWeekend));
        return cruiseStatisticalList;
    }
    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseResult> list) {
        return this.tCruiseResultDao.batchInsert(list);
    }

    @Logs(title = "查询正在执行中的任务",code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TaskSimpleInfo> selectTaskIsRunning(){
        return this.tCruiseResultDao.selectTaskIsRunning();
    }

}

