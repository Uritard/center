package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.TCruiseResultDao;
import com.yjh.platform.module.task.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author czh
 * @since 2020-08-25
 */
@Service
public class TCruiseResultService{

    private Logger log = LoggerFactory.getLogger(TCruiseResultService.class);
    @Autowired
    private TCruiseResultDao tCruiseResultDao;

    @Logs(title = "插入", code = "cruiseResult",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseResult tCruiseResult) {
        return this.tCruiseResultDao.insert(tCruiseResult);
    }

    @Logs(title = "删除", code = "cruiseResult",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskResultId) {
        return this.tCruiseResultDao.deleteByPrimaryId(taskResultId);
    }

    @Logs(title = "更新", code = "cruiseResult",content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseResult tCruiseResult) {
        return this.tCruiseResultDao.update(tCruiseResult);
    }

    @Logs(title = "主键查询", code = "cruiseResult",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseResult selectByPrimaryId(String taskResultId) {
        return this.tCruiseResultDao.selectByPrimaryId(taskResultId);
    }

    @Logs(title = "查询", code = "cruiseResult",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseResult> select(String taskResultId, String taskId, String areaId, Integer cType, Integer cState, Integer modifyState, Integer taskCount, Integer taskWait, String checkUser, Date checkDate, String weather, Date createTime, Date executeTime, String taskCode, String remark) {
        List<TCruiseResult> tCruiseResultList = tCruiseResultDao.select(taskResultId, taskId, areaId, cType, cState, modifyState, taskCount, taskWait, checkUser, checkDate, weather, createTime, executeTime, taskCode, remark);
        return tCruiseResultList;
    }

    @Logs(title = "分页查询--巡视结果确认", code = "cruiseResult",content = "根据web传递的参数查询任务结果")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseResultExpand> selectTaskByPage(String taskName,Integer cType, Integer cState) {
        List<TCruiseResultExpand> tCruiseResultExpandList = tCruiseResultDao.selectTaskByPage(taskName,cState,cType);
        return tCruiseResultExpandList;
    }
    @Logs(title = "分页查询--任务结果详细", code = "cruiseResult",content = "根据web传递的参数查询巡检点结果")
    @Transactional(rollbackFor = Exception.class)
    public List<CruiseResultDetail> selectCruiseByPage( String taskResultId,Integer cruiseType,Integer cruiseAbnormal,String deviceName ) {
        List<CruiseResultDetail> cruiseResultDetailList = tCruiseResultDao.selectCruiseByPage(taskResultId,cruiseType,cruiseAbnormal,deviceName);
        return cruiseResultDetailList;
    }
    @Logs(title = "人工复核", code = "cruiseResult",content = "根据web传递的参数进行人工审核")
    @Transactional(rollbackFor = Exception.class)
    public int manualReview(CruiseManualReview cruiseManualReview,String userId) {
        //获取审核人
        Integer userID = Integer.valueOf(userId);
        String userName = tCruiseResultDao.selectUserName(userID);
        cruiseManualReview.setCheckUser(userName);
        //获取审核时间
        Date cruiseCheckDate = new Date();
        cruiseManualReview.setCheckDate(cruiseCheckDate);
        //审核
        int result1 = tCruiseResultDao.manualReview(cruiseManualReview);

        //判断该巡检点是否产生告警；若是，则修改告警表if_warn_disable字段
        log.info("cruiseDataId是==="+cruiseManualReview.getCruiseDataId());
        Map<String,Object> judgeCondition = tCruiseResultDao.selectJudgeCondition(cruiseManualReview.getCruiseDataId());
        log.info("judgeCondition是==="+judgeCondition);
        int isWarn =  Integer.parseInt(judgeCondition.get("is_warn").toString());
        String taskId = judgeCondition.get("task_id").toString();
        Long instanceId = Long.valueOf(judgeCondition.get("instance_id").toString());
        log.info("查询的告警条件isWarn是==="+isWarn);
        log.info("查询的告警条件taskId是==="+taskId);
        log.info("查询的告警条件instanceId是==="+instanceId);
        if ( isWarn == 1){
            tCruiseResultDao.updateWarnInfo(taskId,instanceId);//更新告警表信息
        }

        //获取审核后的信息
        String taskResultId1  = cruiseManualReview.getTaskResultId();
        List<CruiseManualReview> cruiseManualReviewList = tCruiseResultDao.selectManualDetail(taskResultId1);
        //判断是否全部审核，若都已审核，统计所有的审核人，统计最晚审核的时间，将信息插入
        HashSet<String> haS1 = new HashSet<>();
        for (CruiseManualReview cMR:cruiseManualReviewList){
            if (cMR.getEvaluationState()==257){
                break;
            }else {
                haS1.add(cMR.getCheckUser());
            }
        }
        StringBuilder sb = new StringBuilder();
        for (String checkUser:haS1){
            sb.append(checkUser + ",");
        }
        String checkUserName = sb.toString().substring(0,sb.toString().length()-1);
        String taskResultId = cruiseManualReview.getTaskResultId();
        Date taskCheckDate =findLastDate(cruiseManualReviewList);
        //更新任务审核人以及审核时间
        List<String> list = new ArrayList<>();
        for (CruiseManualReview cMR:cruiseManualReviewList) {
            if (cMR.getEvaluationState() == 256) {
                list.add(cMR.getCheckUser());
            }
        }
        int result2 = 0;
        if (list.size() == cruiseManualReviewList.size()){
            result2 = tCruiseResultDao.updateCheck(taskResultId,checkUserName,taskCheckDate);
        }
        return result1+result2;
    }
    public Date findLastDate(List<CruiseManualReview> list) {
        CruiseManualReview cruiseManualReview = new CruiseManualReview();
        Long dates[] = new Long[list.size()];

        for (int i = 0; i < list.size(); i++) {
            // 把date类型的时间对象转换为long类型，时间越往后，long的值就越大，
            // 所以就依靠这个原理来判断距离现在最近的时间
            Date timeTempOne = list.get(i).getCheckDate();
            if(timeTempOne!=null){
                dates[i] = timeTempOne.getTime();
            } else {
                dates[i] = Long.valueOf(0);
            }
        }
        Long maxIndex = dates[0];// 定义最大值为该数组的第一个数
        for (int j = 0; j < dates.length; j++) {
            if (maxIndex < dates[j]) {
                maxIndex = dates[j];
                cruiseManualReview = list.get(j);
            } else {
                // 找到了这个j
                cruiseManualReview = list.get(0);
            }
        }
        return cruiseManualReview.getCheckDate();
    }
    @Logs(title = "巡视任务结果统计", code = "cruiseResult",content = "根据巡视类型统计本周和上周的任务结果")
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
    @Logs(title = "巡视点结果统计", code = "cruiseResult",content = "根据巡视数据状态统计巡检点结果")
    @Transactional(rollbackFor = Exception.class)
    public List<StatisticalResult2> cruiseStatistical() {

        String colName1 = "abnormal_type";

        List<StatisticalResult2> taskStatisticalList = new ArrayList<>();

        StatisticalResult2 statisticalResult2 = new StatisticalResult2();
        List<CruiseStatistical> list = tCruiseResultDao.cruiseStatistical(colName1);

        CruiseStatistical cruiseStatistical2 = tCruiseResultDao.cruiseStatistical2();
        list.add(cruiseStatistical2);

        statisticalResult2.setCruiseStatisticalList(list);
        taskStatisticalList.add(statisticalResult2);

        return taskStatisticalList;
    }
    @Logs(title = "批量插入", code = "cruiseResult")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseResult> list) {
        return this.tCruiseResultDao.batchInsert(list);
    }

    @Logs(title = "查询正在执行中的任务",code = "cruiseResult",content = "查询正在执行中的任务")
    @Transactional(rollbackFor = Exception.class)
    public List<TaskSimpleInfo> selectTaskIsRunning(){
        return this.tCruiseResultDao.selectTaskIsRunning();
    }
    
}

