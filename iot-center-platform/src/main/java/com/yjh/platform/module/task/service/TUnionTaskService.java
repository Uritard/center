package com.yjh.platform.module.task.service;

import com.alibaba.druid.util.StringUtils;
import com.google.common.collect.Sets;
import com.yjh.platform.module.task.dao.TCfgUnionRuleDao;
import com.yjh.platform.module.task.dao.TUnionTaskAttrDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.dao.TUnionTaskDao;

import java.net.SocketTimeoutException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
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
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        int result = insertRecord(9000000005L,1,null,date);
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
    @Logs(title = "联动历史记录统计", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTaskExpand> historyStatistical() {
        List<TUnionTaskExpand> tUnionTaskList = tUnionTaskDao.historyStatistical();
        return tUnionTaskList;
    }
    @Logs(title = "联动记录存储", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insertRecord(Long ruleId,Integer isFinish,Long robotId,Date createTime) throws ParseException {
        if (isFinish==1){
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
        }
        return 11111;
    }
}

