package com.yjh.platform.module.task.service;

import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.TCfgUnionRuleDao;
import com.yjh.platform.module.task.dao.TUnionTaskAttrDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.dao.TUnionTaskDao;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-09-04
*/
@Service
public class TUnionTaskService{

    @Autowired
    private TUnionTaskDao tUnionTaskDao;
    @Autowired
    private TCfgUnionRuleDao  tCfgUnionRuleDao;
    @Autowired
    private TUnionTaskAttrDao TUnionTaskAttrDao;

    private DateTimeUtil dateTimeUtil;
    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TUnionTask tUnionTask) {
        return this.tUnionTaskDao.insert(tUnionTask);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String unionId) {
        return this.tUnionTaskDao.deleteByPrimaryId(unionId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TUnionTask tUnionTask) {
        return this.tUnionTaskDao.update(tUnionTask);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TUnionTask selectByPrimaryId(String unionId) {
        return this.tUnionTaskDao.selectByPrimaryId(unionId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTask> select(String unionId, Long ruleId, String unionName, Integer ruleDelay, Integer isFinish,  Long robotId,  Integer remark1, Integer remark2, String remark3, String paramValues, Date startTime, Date createTime) {
        List<TUnionTask> tUnionTaskList = tUnionTaskDao.select(unionId, ruleId, unionName, ruleDelay, isFinish, robotId, remark1, remark2, remark3, paramValues, startTime, createTime);
        return tUnionTaskList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTask> selectByPage(TUnionTask tUnionTask) throws ParseException{
        List<TUnionTask> tUnionTaskList = tUnionTaskDao.selectByPage(tUnionTask);
        return tUnionTaskList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TUnionTask> list) {
        return this.tUnionTaskDao.batchInsert(list);
    }
    @Logs(title = "查看联动历史记录", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTaskExpand> selectHistory(String ruleName,Date endDateTemp,Date startDateTemp) {
        List<TUnionTaskExpand> tUnionTaskList = tUnionTaskDao.selectHistory(ruleName,endDateTemp,startDateTemp);
        return tUnionTaskList;
    }
    @Logs(title = "联动历史记录统计--近一季度", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public QuarterEntity historyStatisticalByQuarter() {
        List<String> QuarterDates = dateTimeUtil.getYearDateList1(4);

        String firstTime1 = QuarterDates.get(0);
        String firstTime2 = QuarterDates.get(1);
        String firstTime3 = QuarterDates.get(2);
        String firstTime4 = QuarterDates.get(3);

        QuarterEntity quarterEntity = tUnionTaskDao.getHistoryByQuarter(firstTime1,firstTime2,firstTime3,firstTime4);
        return quarterEntity;
    }
    @Logs(title = "联动历史记录统计--近一年", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public YearEntity historyStatisticalByYear() {
        List<String> yearDates = dateTimeUtil.getYearDateList1(13);

        String firstTime1 = yearDates.get(0);
        String firstTime2 = yearDates.get(1);
        String firstTime3 = yearDates.get(2);
        String firstTime4 = yearDates.get(3);
        String firstTime5 = yearDates.get(4);
        String firstTime6 = yearDates.get(5);
        String firstTime7 = yearDates.get(6);
        String firstTime8 = yearDates.get(7);
        String firstTime9 = yearDates.get(8);
        String firstTime10 = yearDates.get(9);
        String firstTime11 = yearDates.get(10);
        String firstTime12 = yearDates.get(11);
        String firstTime13 = yearDates.get(12);

        YearEntity quarterEntity = tUnionTaskDao.getHistoryByYear(firstTime1,firstTime2,firstTime3,firstTime4,
                                                firstTime5,firstTime6,firstTime7,firstTime8,firstTime9,
                                                firstTime10,firstTime11,firstTime12,firstTime13);
        return quarterEntity;
    }
    @Logs(title = "联动历史记录统计--近一月", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public MonthEntity historyStatisticalByMonth() {
        List<String> monthDates = dateTimeUtil.getDayDateList(31);

        String firstTime1 = monthDates.get(0);
        String firstTime2 = monthDates.get(1);
        String firstTime3 = monthDates.get(2);
        String firstTime4 = monthDates.get(3);
        String firstTime5 = monthDates.get(4);
        String firstTime6 = monthDates.get(5);
        String firstTime7 = monthDates.get(6);
        String firstTime8 = monthDates.get(7);
        String firstTime9 = monthDates.get(8);
        String firstTime10 = monthDates.get(9);
        String firstTime11 = monthDates.get(10);
        String firstTime12 = monthDates.get(11);
        String firstTime13 = monthDates.get(12);
        String firstTime14 = monthDates.get(13);
        String firstTime15 = monthDates.get(14);
        String firstTime16 = monthDates.get(15);
        String firstTime17 = monthDates.get(16);
        String firstTime18 = monthDates.get(17);
        String firstTime19 = monthDates.get(18);
        String firstTime20 = monthDates.get(19);
        String firstTime21 = monthDates.get(20);
        String firstTime22 = monthDates.get(21);
        String firstTime23 = monthDates.get(22);
        String firstTime24 = monthDates.get(23);
        String firstTime25 = monthDates.get(24);
        String firstTime26 = monthDates.get(25);
        String firstTime27 = monthDates.get(26);
        String firstTime28 = monthDates.get(27);
        String firstTime29 = monthDates.get(28);
        String firstTime30 = monthDates.get(29);
        String firstTime31 = monthDates.get(30);

        MonthEntity dayEntity = tUnionTaskDao.getHistoryByMonth(firstTime1,firstTime2,firstTime3,firstTime4,firstTime5,
                                                            firstTime6,firstTime7,firstTime8,firstTime9,firstTime10,
                                                            firstTime11,firstTime12,firstTime13,firstTime14,firstTime15,
                                                            firstTime16,firstTime17,firstTime18,firstTime19,firstTime20,
                                                            firstTime21,firstTime22,firstTime23,firstTime24,firstTime25,
                                                            firstTime26,firstTime27,firstTime28,firstTime29,firstTime30,
                                                            firstTime31);
        return dayEntity;
    }
    @Logs(title = "联动记录存储", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insertRecord(Long ruleId,Long robotId,Date createTime) {
            //新增联合巡视预案数据
            TCfgUnionRule tCfgUnionRule = tCfgUnionRuleDao.selectByPrimaryId(ruleId);
            TUnionTask tUnionTask = new TUnionTask();
            tUnionTask.setUnionId(String.valueOf(UUID.randomUUID()).replace("-", ""));
            tUnionTask.setRuleId(ruleId);
            tUnionTask.setUnionName(tCfgUnionRule.getRuleName()+tCfgUnionRule.getPlanId());
            tUnionTask.setRuleDelay(tCfgUnionRule.getRuleDelay());
            tUnionTask.setRobotId(robotId);
            tUnionTask.setIsFinish(1);
            tUnionTask.setCreateTime(createTime);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //计算巡视时间
            Date startTime = new Date();
            startTime.setSeconds(createTime.getSeconds() + tCfgUnionRule.getRuleDelay());
            tUnionTask.setStartTime(startTime);
            tUnionTask.setParamValues(tCfgUnionRule.getInputParam());

//            System.out.println("tUnionTask是："+tUnionTask);
            tUnionTaskDao.insert(tUnionTask);

            //新增联合巡视预案属性数据
            List<TUnionTaskDetail> tUnionTaskDetailList = tUnionTaskDao.selectUnionDetail(ruleId);
//            System.out.println("tUnionTaskDetailList是："+tUnionTaskDetailList);
            List<TUnionTaskAttr> tUnionTaskAttrList = new ArrayList<>();
            for(TUnionTaskDetail tUnionTaskDetail:tUnionTaskDetailList){
                TUnionTaskAttr tUnionTaskAttr = new TUnionTaskAttr();

                tUnionTaskAttr.setUnionId(tUnionTask.getUnionId());
                tUnionTaskAttr.setInstanceId(tUnionTaskDetail.getInstanceId());
                tUnionTaskAttr.setDeviceCustomId(tUnionTaskDetail.getDeviceCustomId());
                tUnionTaskAttr.setDeviceMeteId(tUnionTaskDetail.getDeviceMeteId());
                if(tUnionTaskDetail.getCruiseType()==228){
                    tUnionTaskAttr.setIfRobot(1);
                }else
                    tUnionTaskAttr.setIfRobot(0);
                if(tUnionTaskDetail.getCruiseType()==229){
                    tUnionTaskAttr.setIfVideo(1);
                }else
                    tUnionTaskAttr.setIfVideo(0);
                if(tUnionTaskDetail.getCruiseType()==230){
                    tUnionTaskAttr.setIfInferad(1);
                }else
                    tUnionTaskAttr.setIfInferad(0);

                tUnionTaskAttrList.add(tUnionTaskAttr);
            }
//            tUnionTaskDao.insertRecordDetail(tUnionTaskAttrList);
            System.out.println("tUnionTaskAttrList是："+tUnionTaskAttrList);
            TUnionTaskAttrDao.batchInsert(tUnionTaskAttrList);

        return 11111;
    }
}

