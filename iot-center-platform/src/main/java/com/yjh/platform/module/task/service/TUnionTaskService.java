package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.TCfgUnionRuleDao;
import com.yjh.platform.module.task.dao.TUnionTaskAttrDao;
import com.yjh.platform.module.task.dao.TUnionTaskDao;
import com.yjh.platform.module.task.entity.*;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

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
    @Autowired
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(TUnionTaskService.class);

    @Transactional(rollbackFor = Exception.class)
    public int insert(TUnionTask tUnionTask) {
        return this.tUnionTaskDao.insert(tUnionTask);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String unionId) {
        return this.tUnionTaskDao.deleteByPrimaryId(unionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TUnionTask tUnionTask) {
        return this.tUnionTaskDao.update(tUnionTask);
    }

    @Transactional(rollbackFor = Exception.class)
    public TUnionTask selectByPrimaryId(String unionId) {
        return this.tUnionTaskDao.selectByPrimaryId(unionId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTask> select(String unionId, Long ruleId, String unionName, Integer ruleDelay, Integer isFinish,  Long robotId,  Long meteId, Date triggeringTime, String ruleName, String paramValues, Date startTime, Date createTime,String planName,String ruleContent) {
        return tUnionTaskDao.select(unionId, ruleId, unionName, ruleDelay, isFinish, robotId, meteId, triggeringTime, ruleName, paramValues, startTime, createTime,planName,ruleContent);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTask> selectByPage(TUnionTask tUnionTask) {
        return tUnionTaskDao.selectByPage(tUnionTask);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TUnionTask> list) {
        return this.tUnionTaskDao.batchInsert(list);
    }
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTaskExpand> selectHistory(String ruleName,String startDate,String endDate) {
        List<TUnionTaskExpand> tUnionTaskList  = tUnionTaskDao.selectHistory(ruleName,startDate,endDate);
        log.info("tUnionTaskList是："+tUnionTaskList);
        for (TUnionTaskExpand tUnionTaskExpand:tUnionTaskList)
        {
            if (tUnionTaskExpand.getIsFinish() == 0){
                tUnionTaskExpand.setIsFinishName("失败");
            }else {
                tUnionTaskExpand.setIsFinishName("成功");
            }
        }
        return tUnionTaskList;
    }
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> historyStatisticalByWeek() {
        return tUnionTaskDao.getHistoryByWeek();
    }
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> historyStatisticalByYear() {
        return tUnionTaskDao.getHistoryByYear();
    }
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> historyStatisticalByMonth() {
        return tUnionTaskDao.getHistoryByMonth();
    }
    @Transactional(rollbackFor = Exception.class)
    public int insertRecord(Long meteId,String unionId,Long ruleId,Long robotId,Date createTime,String paramValues,Date triggeringTime) {
        TCfgUnionRule tCfgUnionRule = tCfgUnionRuleDao.selectByPrimaryId(ruleId);
        TUnionTask tUnionTask = new TUnionTask();
        tUnionTask.setUnionId(unionId);
        tUnionTask.setRuleId(ruleId);
        tUnionTask.setUnionName(tCfgUnionRule.getRuleName()+tCfgUnionRule.getPlanId());
        tUnionTask.setRuleDelay(tCfgUnionRule.getRuleDelay());
        tUnionTask.setRobotId(robotId);
        tUnionTask.setIsFinish(1);
        tUnionTask.setMeteId(meteId);
        tUnionTask.setCreateTime(createTime);
        tUnionTask.setRuleName(tCfgUnionRule.getRuleName());
        tUnionTask.setPlanName(tCfgUnionRuleDao.getPlanName(tCfgUnionRule.getPlanId()));
        tUnionTask.setRuleContent(tCfgUnionRule.getRuleContent());

        //计算巡视时间
        Date startTime = new Date();
        startTime.setSeconds(createTime.getSeconds() + tCfgUnionRule.getRuleDelay());
        tUnionTask.setStartTime(startTime);
        tUnionTask.setParamValues(paramValues);
        tUnionTask.setTriggeringTime(triggeringTime);

        int result01 = tUnionTaskDao.insert(tUnionTask);

        //新增联合巡视预案属性数据
        List<TUnionTaskDetail> tUnionTaskDetailList = tUnionTaskDao.selectUnionDetail(ruleId);
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
        int result02 = TUnionTaskAttrDao.batchInsert(tUnionTaskAttrList);
        return result01+result02;
    }

    //联动弹窗--联动信息
    @Transactional(rollbackFor = Exception.class)
    public LinkageInformation linkageInformation(String taskId) {

        List<Map<String, Object>> list = TUnionTaskAttrDao.linkageCruiseDevice(taskId);

        log.info("linkageCruiseDevice: {}", JSON.toJSONString(list));

        LinkageInformation info = TUnionTaskAttrDao.linkageInformation(taskId);
        for (Map<String, Object> li : list) {
            if (li.get("camera_id") != null) {
                info.setTaskCameraId(MapUtils.getLongValue(li, "camera_id"));
            }
            if (li.get("robot_id") != null) {
                info.setTaskRobotId(MapUtils.getLongValue(li, "robot_id"));
            }
        }

        return info;
    }

    //联动弹窗--监测数据
    @Transactional(rollbackFor = Exception.class)
    public List<LinkageMonitorData> linkageMonitorData(String taskId) throws InterruptedException{
        TimeUnit.MILLISECONDS.sleep(1000);
        //根据任务Id查询相关内容
        List<LinkageMonitorData> linkageMonitorDataList = TUnionTaskAttrDao.selectDeviceInfo(taskId);

        for (LinkageMonitorData lmd : linkageMonitorDataList){
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + lmd.getInstanceId());
            if (!redisInfoMap.isEmpty()){
                if (Objects.nonNull(redisInfoMap.get("cruiseResult")) &&
                        ("246".equals(redisInfoMap.get("cruiseResult")) || "247".equals(redisInfoMap.get("cruiseResult")))){
                    log.info("redisInfoMap==="+redisInfoMap);
                    lmd.setPresetId(Long.valueOf(redisInfoMap.get("cruiseId")));
//                    lmd.setEndTime(redisInfoMap.get("endTime"));
//                    lmd.setStartTime(redisInfoMap.get("startTime"));
                    if (Objects.nonNull(redisInfoMap.get("cameraId")) && !"".equals(redisInfoMap.get("cameraId"))){
                        lmd.setCameraId(Long.valueOf(redisInfoMap.get("cameraId")));
                        lmd.setDeviceType(1);
                    }else {
                        lmd.setCameraId(Long.valueOf(redisInfoMap.get("robotId")));
                        lmd.setDeviceType(0);
                    }
                    Integer cruiseResult = Integer.valueOf(redisInfoMap.get("cruiseResult"));
                    String taskName = TUnionTaskAttrDao.selectTaskName(taskId);
                    String CruiseResultName = TUnionTaskAttrDao.selectCruiseResultName(cruiseResult);
                    lmd.setTaskName(taskName);
                    lmd.setCruiseTime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")));
                    lmd.setCruiseResult(cruiseResult);
                    lmd.setCruiseResultName(CruiseResultName);
                    lmd.setResultNum(redisInfoMap.get("resultNum"));
                    lmd.setPicpath(redisInfoMap.get("picpath"));
                }
            }

        }
        return linkageMonitorDataList;
    }
}