package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTaskAttrDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDelDao;
import com.yjh.platform.module.task.entity.*;
import lombok.NonNull;
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
    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;
    @Autowired
    private TCruisePlanAttrDao tCruisePlanAttrDao;
    @Autowired
    private TCruiseTaskDelDao tCruiseTaskDelDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTask tCruiseTask) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = null;
        try {
            if (tCruiseTask.getIfRun()==172) {
                startTime = format.parse("2000-01-01 00:00:00");
                tCruiseTask.setStartTime(startTime);
            }
        } catch (Exception e) { e.getMessage(); }
        tCruiseTask.setTaskId(String.valueOf(UUID.randomUUID()).replace("-", ""));
        List<TCruisePlanAttr> tCruisePlanAttrList = tCruisePlanAttrDao.select(tCruiseTask.getPlanId(),null,null,null,null,null,null,null,null,null,null,null,null);
        List<Long> instanceList = new ArrayList<>();
        for (TCruisePlanAttr tCruisePlanAttr:tCruisePlanAttrList) {
            instanceList.add(tCruisePlanAttr.getInstanceId());
        }
        List<TCruisePointInstance> tCruisePointInstanceList = this.tCruiseTaskAttrDao.batchSelect(instanceList);
        List<TCruiseTaskAttr> tCruiseTaskAttrList = new ArrayList<>();
        for (TCruisePointInstance tCruisePointInstance:tCruisePointInstanceList) {
            TCruiseTaskAttr tCruiseTaskAttr = new TCruiseTaskAttr();
            tCruiseTaskAttr.setTaskId(tCruiseTask.getTaskId());
            tCruiseTaskAttr.setInstanceId(tCruisePointInstance.getInstanceId());
            tCruiseTaskAttr.setDeviceMeteId(tCruisePointInstance.getDeviceMeteId());
            tCruiseTaskAttr.setDeviceId(tCruisePointInstance.getDeviceId());
            tCruiseTaskAttr.setCustomId(tCruisePointInstance.getCustomId());
            tCruiseTaskAttr.setPointTaskId(String.valueOf(tCruisePointInstance.getCruiseId()));
            tCruiseTaskAttrList.add(tCruiseTaskAttr);
        }
        //TODO 增加定时器任务
        this.tCruiseTaskDao.insert(tCruiseTask);
        return this.tCruiseTaskAttrDao.batchInsert(tCruiseTaskAttrList);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskId, Date taskDate) {
        TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        if (tCruiseTask.getIfRun()==172) {
            TCruiseTaskDel tCruiseTaskDel = new TCruiseTaskDel();
            tCruiseTaskDel.setTaskId(taskId);
            tCruiseTaskDel.setDelTime(taskDate);
            Date date = new Date("yyyy-MM-dd HH:mm:ss");
            tCruiseTaskDel.setCreateTime(date);
            return tCruiseTaskDelDao.insert(tCruiseTaskDel);
        } else {return this.tCruiseTaskDao.deleteByPrimaryId(taskId);}
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
    public List<TCruiseTask> select(String taskId, String taskName, Long planId, String areaId, Integer type, Integer ifRun, Long robotId, String dateType, Integer taskType, Date startTime, Date createTime) {
        List<TCruiseTask> tCruiseTaskList = tCruiseTaskDao.select(taskId, taskName, planId, areaId, type, ifRun, robotId, dateType, taskType, startTime, createTime);
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
    public List<Map<String, Object>> taskCount(Date taskStartDate){

        SimpleDateFormat sdfF = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfF2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dayBefore = new Date();
        Date dayAfter = new Date();
        Date originTime = new Date();
        String firstDay = "", lastDay = "";
        if (Objects.equals(null, taskStartDate)) {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, date.getMonth());
            int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, fDay);
            firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

            int lDay=0;
            //2月的平年瑞年天数
            if(date.getMonth()==1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            }else { lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); }
            calendar.set(Calendar.MONTH, date.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime())+" 23:59:59";
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            calendar.set(Calendar.YEAR, taskStartDate.getYear()+1900);
            int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
            calendar.set(Calendar.DAY_OF_MONTH, fDay);
            firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

            int lDay=0;
            //2月的平年瑞年天数
            if(taskStartDate.getMonth()==1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            }else { lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); }
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            calendar.set(Calendar.YEAR, taskStartDate.getYear()+1900);
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime())+" 23:59:59";
        }

        try {
            originTime = format.parse("2000-01-01 00:00:00");
            dayBefore = format.parse(firstDay);
            dayAfter = format.parse(lastDay);
        } catch (Exception e) { e.getMessage(); }
        List<TCruiseTaskCount> list = new ArrayList<>();
        list = this.tCruiseTaskDao.taskCount(dayBefore,dayAfter);
        System.out.println("list: "+list);
        List<TCruiseTaskDel> listDel = this.tCruiseTaskDelDao.slectByTimeZone(dayBefore, dayAfter);
        List<Map<String, Object>> listTask = new ArrayList<>();
        for (TCruiseTaskCount tCruiseTaskCount:list) {
            if (tCruiseTaskCount.getIfRun() == 172 && tCruiseTaskCount.getStartTime().compareTo(originTime) == 0) {
                List<Date> timeList = DateTimeUtil.cornTransTime(tCruiseTaskCount.getDateType(), dayBefore, dayAfter);
                for (Date aTimeList:timeList) {
                    Map<String, Object> taskCountMap = new HashMap<>();
                    long taskTime = aTimeList.getTime();
                    if (listDel.size()>0) {
                        for (TCruiseTaskDel tCruiseTaskDel:listDel) {
                            long taskDelTime = tCruiseTaskDel.getDelTime().getTime();
                            if (!Objects.equals(tCruiseTaskDel.getTaskId(), tCruiseTaskCount.getTaskId()) || taskDelTime!=taskTime) {
                                taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                                taskCountMap.put("total",tCruiseTaskCount.getTotal());
                                taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                                taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                                taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                                taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                                //TODO 增加redis获取任务状态，1是真
                                if (Objects.nonNull(tCruiseTaskCount.getTaskState())) taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                                if (Objects.nonNull(tCruiseTaskCount.getTaskStatus())) taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                                taskCountMap.put("startTime", sdfF2.format(aTimeList));
                                listTask.add(taskCountMap);
                            }
                        }
                    } else {
                        taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                        taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                        taskCountMap.put("total",tCruiseTaskCount.getTotal());
                        taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                        taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                        taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                        //TODO 增加redis获取任务状态，1是真
                        if (Objects.nonNull(tCruiseTaskCount.getTaskState())) taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                        if (Objects.nonNull(tCruiseTaskCount.getTaskStatus())) taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                        taskCountMap.put("startTime", sdfF2.format(aTimeList));
                        listTask.add(taskCountMap);
                    }
                }
            } else {
                Map<String, Object> taskCountMap = new HashMap<>();
                taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                taskCountMap.put("total",tCruiseTaskCount.getTotal());
                taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                if (Objects.nonNull(tCruiseTaskCount.getTaskState())) taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                if (Objects.nonNull(tCruiseTaskCount.getTaskState()) && tCruiseTaskCount.getTaskState().equals("239")) {
                    //TODO taskStatus增加redis获取任务状态，1是真
                    taskCountMap.put("taskStatus", "");
                } else {taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());}

                taskCountMap.put("startTime", sdfF2.format(tCruiseTaskCount.getStartTime()));
                listTask.add(taskCountMap);
            }
        }
        System.out.println("listTask: "+listTask);
        return listTask;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskList> selectPointStatus(String taskId) {
        List<TCruiseTaskList> tCruiseTaskList = tCruiseTaskDao.selectPointStatus(taskId);
        return tCruiseTaskList;
    }

}

