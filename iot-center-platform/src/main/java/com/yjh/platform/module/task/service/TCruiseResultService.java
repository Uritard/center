package com.yjh.platform.module.task.service;

import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.dao.TCruiseResultDao;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

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
    public List<TCruiseResultExpand> selectTaskByPage(String taskName,Integer cType, Integer cState) {
        List<TCruiseResultExpand> tCruiseResultExpandList = tCruiseResultDao.selectTaskByPage(taskName,cState,cType);
        return tCruiseResultExpandList;
    }
    @Logs(title = "分页查询--任务结果详细", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultDetail> selectCruiseByPage( String taskId,Integer cruiseType,Integer state,String executeTime,String deviceName ) {
        List<CruiseResultDetail> cruiseResultDetailList = tCruiseResultDao.selectCruiseByPage(taskId,cruiseType,state,executeTime,deviceName);
        return cruiseResultDetailList;
    }
    @Logs(title = "人工复核", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int manualReview(CruiseManualReview cruiseManualReview,String userId) {
        Integer userID = Integer.valueOf(userId);
        String userName = tCruiseResultDao.selectUserName(userID);
        cruiseManualReview.setCheckUser(userName);
        return this.tCruiseResultDao.manualReview(cruiseManualReview);
    }
    @Logs(title = "巡视任务结果统计", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticalResult> taskStatistical() {

        String weekStart = DateTimeUtil.getWeekStart();
        String weekEnd = DateTimeUtil.getWeekEnd();
        String lastWeekStart = DateTimeUtil.getLastWeekStart();
        String lastWeekend = DateTimeUtil.getLastWeekend();

        String colName1 = "plan_type";

        List<StatisticalResult> taskStatisticalList = new ArrayList<>();

        StatisticalResult statisticalResult = new StatisticalResult();
        statisticalResult.setTimeNode("本周");
        statisticalResult.setStatisticalList(tCruiseResultDao.taskStatistical(colName1,weekStart,weekEnd));
        taskStatisticalList.add(statisticalResult);

        StatisticalResult statisticalResult1 = new StatisticalResult();
        statisticalResult1.setTimeNode("上周");
        statisticalResult1.setStatisticalList(tCruiseResultDao.taskStatistical(colName1,lastWeekStart,lastWeekend));
        taskStatisticalList.add(statisticalResult1);

        return taskStatisticalList;
    }
    @Logs(title = "巡视点结果统计", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticalResult> cruiseStatistical() {

        String colName1 = "data_state";

        List<StatisticalResult> taskStatisticalList = new ArrayList<>();

        StatisticalResult statisticalResult1 = new StatisticalResult();
        statisticalResult1.setStatisticalList(tCruiseResultDao.cruiseStatistical(colName1));
        taskStatisticalList.add(statisticalResult1);

        return taskStatisticalList;
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

