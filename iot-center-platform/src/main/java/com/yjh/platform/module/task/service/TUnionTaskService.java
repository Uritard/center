package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.TCfgUnionRuleDao;
import com.yjh.platform.module.task.dao.TUnionTaskAttrDao;
import com.yjh.platform.module.task.dao.TUnionTaskDao;
import com.yjh.platform.module.task.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

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


    private DateTimeUtil dateTimeUtil;
    @Logs(title = "插入", code = "tUnionTask",content = "根据web传递的参数插入巡检记录")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TUnionTask tUnionTask) {
        return this.tUnionTaskDao.insert(tUnionTask);
    }

    @Logs(title = "删除", code = "tUnionTask",content = "根据web传递的参数删除巡检记录")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String unionId) {
        return this.tUnionTaskDao.deleteByPrimaryId(unionId);
    }

    @Logs(title = "更新", code = "tUnionTask",content = "根据web传递的参数更新巡检记录")
    @Transactional(rollbackFor = Exception.class)
    public int update(TUnionTask tUnionTask) {
        return this.tUnionTaskDao.update(tUnionTask);
    }

    @Logs(title = "主键查询", code = "tUnionTask",content = "根据web传递的参数查询巡检记录")
    @Transactional(rollbackFor = Exception.class)
    public TUnionTask selectByPrimaryId(String unionId) {
        return this.tUnionTaskDao.selectByPrimaryId(unionId);
    }

    @Logs(title = "查询", code = "tUnionTask",content = "根据web传递的参数查询巡检记录")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTask> select(String unionId, Long ruleId, String unionName, Integer ruleDelay, Integer isFinish,  Long robotId,  Integer remark1, Integer remark2, String remark3, String paramValues, Date startTime, Date createTime) {
        List<TUnionTask> tUnionTaskList = tUnionTaskDao.select(unionId, ruleId, unionName, ruleDelay, isFinish, robotId, remark1, remark2, remark3, paramValues, startTime, createTime);
        return tUnionTaskList;
    }

    @Logs(title = "分页查询", code = "tUnionTask",content = "根据web传递的参数查询巡检记录")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTask> selectByPage(TUnionTask tUnionTask) {
        List<TUnionTask> tUnionTaskList = tUnionTaskDao.selectByPage(tUnionTask);

        int a = insertRecord(12l,"123122",9000000037L,null,new Date(),"测试用的断面数据");
        System.out.println("哈哈啊哈哈哈："+a);
        return tUnionTaskList;
    }

    @Logs(title = "批量插入", code = "tUnionTask",content = "根据web传递的参数批量插入巡检记录")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TUnionTask> list) {
        return this.tUnionTaskDao.batchInsert(list);
    }
    @Logs(title = "查看联动历史记录", code = "tUnionTask",content = "根据web传递的参数查看联动历史记录")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTaskExpand> selectHistory(String ruleName,Date endDateTemp,Date startDateTemp) {
        List<TUnionTaskExpand> tUnionTaskList  = tUnionTaskDao.selectHistory(ruleName,endDateTemp,startDateTemp);
        System.out.println("tUnionTaskList是："+tUnionTaskList);
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
    @Logs(title = "联动历史记录统计--近一周", code = "tUnionTask",content = "统计近一周发生的联动次数")
    @Transactional(rollbackFor = Exception.class)
    public List<TutHistoryStatistical> historyStatisticalByWeek() {
        List<String> weekDates = dateTimeUtil.getDayDateList(8);

        String firstTime1 = weekDates.get(0);
        String firstTime2 = weekDates.get(1);
        String firstTime3 = weekDates.get(2);
        String firstTime4 = weekDates.get(3);
        String firstTime5 = weekDates.get(4);
        String firstTime6 = weekDates.get(5);
        String firstTime7 = weekDates.get(6);
        String firstTime8 = weekDates.get(7);


        Map<String,Integer> map = tUnionTaskDao.getHistoryByWeek(firstTime1,firstTime2,firstTime3,firstTime4,
                firstTime5,firstTime6,firstTime7,firstTime8);
        List<TutHistoryStatistical> tutHistoryStatisticalList = new ArrayList<>();

        Iterator<String> iter = map.keySet().iterator();
        while(iter.hasNext()){
            String key=iter.next();
            String timeNode =  key.substring(0,10);
            Number mapValue = (Number)map.get(key);
            TutHistoryStatistical tutHistoryStatistical = new TutHistoryStatistical();
            tutHistoryStatistical.setTimeNode(timeNode);
            tutHistoryStatistical.setCount(mapValue);
            tutHistoryStatisticalList.add(tutHistoryStatistical);
        }

        Collections.sort(tutHistoryStatisticalList, new Comparator<TutHistoryStatistical>() {
            @Override
            public int compare(TutHistoryStatistical o1, TutHistoryStatistical o2) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = simpleDateFormat.parse(o1.getTimeNode());
                    date2 = simpleDateFormat.parse(o2.getTimeNode());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                int flag = date1.compareTo(date2);
                if (flag == -1) {
                    flag = -1;
                } else if (flag == 1) {
                    flag = 1;
                }
                return flag;
            }
        });
        return tutHistoryStatisticalList;
    }
    @Logs(title = "联动历史记录统计--近一年", code = "tUnionTask",content = "统计近一年发生的联动次数")
    @Transactional(rollbackFor = Exception.class)
    public List<TutHistoryStatistical> historyStatisticalByYear() {
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

        Map<String,Integer> map = tUnionTaskDao.getHistoryByYear(firstTime1,firstTime2,firstTime3,firstTime4,
                firstTime5,firstTime6,firstTime7,firstTime8,firstTime9,
                firstTime10,firstTime11,firstTime12,firstTime13);
        List<TutHistoryStatistical> tutHistoryStatisticalList = new ArrayList<>();

        Iterator<String> iter = map.keySet().iterator();
        while(iter.hasNext()){
            String key=iter.next();
            String timeNode =  key.substring(0,7);
            Number mapValue = (Number)map.get(key);

            TutHistoryStatistical tutHistoryStatistical = new TutHistoryStatistical();
            tutHistoryStatistical.setTimeNode(timeNode);
            tutHistoryStatistical.setCount(mapValue);
            tutHistoryStatisticalList.add(tutHistoryStatistical);
        }
        Collections.sort(tutHistoryStatisticalList, new Comparator<TutHistoryStatistical>() {
            @Override
            public int compare(TutHistoryStatistical o1, TutHistoryStatistical o2) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM");

                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = simpleDateFormat.parse(o1.getTimeNode());
                    date2 = simpleDateFormat.parse(o2.getTimeNode());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                int flag = date1.compareTo(date2);
                if (flag == -1) {
                    flag = -1;
                } else if (flag == 1) {
                    flag = 1;
                }
                return flag;
            }
        });
        return tutHistoryStatisticalList;
    }
    @Logs(title = "联动历史记录统计--近一月", code = "tUnionTask",content = "统计近一月发生的联动次数")
    @Transactional(rollbackFor = Exception.class)
    public List<WarnStatistical> historyStatisticalByMonth() {
        List<String> monthDates = dateTimeUtil.getDayDateList(30);
        String firstTime1 = monthDates.get(0);
        Date startingTime = dateTimeUtil.parse(firstTime1);
        String endTime = dateTimeUtil.getDayBefore(startingTime);
        String startTime = monthDates.get(29);
        List<WarnStatistical> list = tUnionTaskDao.getHistoryByMonth(startTime,endTime);
        List<WarnStatistical> list1 = new ArrayList<>();

        for (int i = 0;i < monthDates.size();i++){
            int flag = 0;
            for (int j = 0;j < list.size();j++){
                if (monthDates.get(i).substring(0, 10).equals(list.get(j).getTimeNode())){
                    flag++;
                }
            }
            if (flag == 0){
                WarnStatistical ws = new WarnStatistical();
                ws.setCount(0);
                ws.setTimeNode(monthDates.get(i).substring(0, 10));
                list1.add(ws);
            }
        }
        list.addAll(list1);
        Collections.sort(list, new Comparator<WarnStatistical>() {
            @Override
            public int compare(WarnStatistical o1, WarnStatistical o2) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = simpleDateFormat.parse(o1.getTimeNode());
                    date2 = simpleDateFormat.parse(o2.getTimeNode());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                int flag = date1.compareTo(date2);
                if (flag == -1) {
                    flag = -1;
                } else if (flag == 1) {
                    flag = 1;
                }
                return flag;
            }
        });
        return list;
    }
    @Logs(title = "联动记录存储", code = "tUnionTask",content = "触发联动进行记录存储")
    @Transactional(rollbackFor = Exception.class)
    public int insertRecord(Long meteId,String unionId,Long ruleId,Long robotId,Date createTime,String paramValues) {
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

        //计算巡视时间
        Date startTime = new Date();
        startTime.setSeconds(createTime.getSeconds() + tCfgUnionRule.getRuleDelay());
        tUnionTask.setStartTime(startTime);
        tUnionTask.setParamValues(paramValues);

        int result01 = tUnionTaskDao.insert(tUnionTask);

        //新增联合巡视预案属性数据
        List<TUnionTaskDetail> tUnionTaskDetailList = tUnionTaskDao.selectUnionDetail(ruleId);
        System.out.println("---------------------"+tUnionTaskDetailList);
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
        LinkageInformation linkageInformation = TUnionTaskAttrDao.linkageInformation(taskId);
        return linkageInformation;
    }

    //联动弹窗--监测数据
    @Logs
    @Transactional(rollbackFor = Exception.class)
    public List<LinkageMonitorData> linkageMonitorData(String taskId) throws Exception{
        Thread.sleep(1000);
        //根据任务Id查询相关内容
        List<LinkageMonitorData> linkageMonitorDataList = TUnionTaskAttrDao.selectDeviceInfo(taskId);

        for (LinkageMonitorData lmd : linkageMonitorDataList){
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + lmd.getInstanceId());
            if ("246".equals(redisInfoMap.get("cruiseResult"))
                    || "247".equals(redisInfoMap.get("cruiseResult"))){
                log.info("redisInfoMap==="+redisInfoMap);
                lmd.setPresetId(Long.valueOf(redisInfoMap.get("cruiseId")));
                lmd.setEndTime(redisInfoMap.get("endTime"));
                lmd.setStartTime(redisInfoMap.get("startTime"));
                lmd.setCameraId(Long.valueOf(redisInfoMap.get("cameraId")));
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
//        log.info("linkageMonitorDataList==="+linkageMonitorDataList);
        return linkageMonitorDataList;
    }
}