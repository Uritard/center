package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.entity.TCruiseTask;
import com.yjh.platform.module.task.entity.TCruiseTaskCount;
import com.yjh.platform.module.task.entity.TCruiseTaskCron;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author tt
 * @since 2020-08-27
 */
@Service
public class TCruiseTaskService {

    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTask tCruiseTask) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = null;
        try {
            startTime = format.parse("2000-01-01 00:00:00");
        } catch (Exception e) { e.getMessage(); }
        tCruiseTask.setStartTime(startTime);
        tCruiseTask.setTaskId(String.valueOf(UUID.randomUUID()));
        return this.tCruiseTaskDao.insert(tCruiseTask);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskId) {
        return this.tCruiseTaskDao.deleteByPrimaryId(taskId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTask tCruiseTask) {
        return this.tCruiseTaskDao.update(tCruiseTask);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTask selectByPrimaryId(String taskId) {
        return this.tCruiseTaskDao.selectByPrimaryId(taskId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTask> select(String taskId, String taskName, Long planId, String areaId, String name, Integer type, Integer ifRun, Long robotId, String dateType, Integer taskType, Date startTime, Date createTime) {
        List<TCruiseTask> tCruiseTaskList = tCruiseTaskDao.select(taskId, taskName, planId, areaId, name, type, ifRun, robotId, dateType, taskType, startTime, createTime);
        return tCruiseTaskList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTask> selectByPage(TCruiseTask tCruiseTask) {
        List<TCruiseTask> tCruiseTaskList = tCruiseTaskDao.selectByPage(tCruiseTask);
        return tCruiseTaskList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTask> list) {
        return this.tCruiseTaskDao.batchInsert(list);
    }

    //任务统计
    @Logs(title = "任务统计",code = "task")
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> taskCount(){

        SimpleDateFormat sdfF = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfF2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, date.getMonth());
        int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
        calendar.set(Calendar.DAY_OF_MONTH, fDay-7);
        String firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

        int lDay=0;
        //2月的平年瑞年天数
        if(date.getMonth()==2) {
            lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
        }else { lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); }
        calendar.set(Calendar.MONTH, date.getMonth());
        calendar.set(Calendar.DAY_OF_MONTH, lDay+7);
        String lastDay = sdfF.format(calendar.getTime())+" 23:59:59";
        List<TCruiseTaskCount> list = new ArrayList<>();
        Date dayBeforeOneWeek = new Date();
        Date dayAfterOneWeek = new Date();
        Date originTime = new Date();
        try {
            originTime = format.parse("2000-01-01 00:00:00");
            dayBeforeOneWeek = format.parse(firstDay);
            dayAfterOneWeek = format.parse(lastDay);
            list = this.tCruiseTaskDao.taskCount(dayBeforeOneWeek,dayAfterOneWeek);
        } catch (Exception e) { e.getMessage(); }

        List<Map<String, Object>> listTask = new ArrayList<>();
        for (int i=0;i<list.size();i++) {
            TCruiseTaskCount tCruiseTaskCount = list.get(i);
            if (tCruiseTaskCount.getIfRun()==172 && tCruiseTaskCount.getStartTime().compareTo(originTime) == 0) {
                List<Date> timeList = DateTimeUtil.cornTransTime(tCruiseTaskCount.getDateType(), dayAfterOneWeek);
                for(int j=0;j<timeList.size();j++) {
                    Map<String, Object> taskCountMap = new HashMap<>();
                    taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                    taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                    taskCountMap.put("type", tCruiseTaskCount.getDictNote());
                    //TODO 增加MQ获取任务状态
                    taskCountMap.put("taskStatus", "fuckWHL");
                    taskCountMap.put("startTime", sdfF2.format(timeList.get(j)));
                    listTask.add(taskCountMap);
                }
            } else {
                Map<String, Object> taskCountMap = new HashMap<>();
                taskCountMap.put("type", tCruiseTaskCount.getDictNote());
                taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                //TODO 增加MQ获取任务状态
                taskCountMap.put("taskStatus", "fuckWHL");
                taskCountMap.put("startTime", sdfF2.format(tCruiseTaskCount.getStartTime()));
                listTask.add(taskCountMap);
            }
        }
        System.out.println("listTask: "+listTask);
        return listTask;
    }

}

