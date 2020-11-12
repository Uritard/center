package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.quartz.CruiseTaskJob;
import com.yjh.platform.common.quartz.JobManager;
import com.yjh.platform.common.quartz.QuartzTask;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.task.dao.*;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.TAlgorithmConfDao;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.entity.TCameraPreset;
import lombok.Data;
import lombok.NonNull;
import org.quartz.CronExpression;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private  TCameraPresetDao tCameraPresetDao;
    @Autowired
    private TCruiseResultDao tCruiseResultDao;
    @Autowired
    private TAlgorithmConfDao tAlgorithmConfDao;
    @Autowired
    private TAlgorithmInfoDao tAlgorithmInfoDao;
    @Autowired
    private TCruiseTaskResultDao tCruiseTaskResultDao;
    @Autowired
    private TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao;
    @Autowired
    private TCruiseDataResultDao tCruiseDataResultDao;
    //模板图片路径
    @Value("${spring.picModelPath.dir}")
    private String picModelPath;
    //等待相机转到预置位时间
    @Value("${spring.move.waitTime}")
    private Long waitTime;
    //jobName
    @Value("${spring.QingHua.jobName}")
    private String jobName;

    private Logger log = LoggerFactory.getLogger(TCruiseTaskService.class);

    @Logs(title = "插入", code = "TCruiseTask",content = "根据web传入的参数新增任务")
    @Transactional(rollbackFor = Exception.class)
    public String insert(TCruiseTask tCruiseTask) {
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
        this.tCruiseTaskDao.insert(tCruiseTask);
        this.tCruiseTaskAttrDao.batchInsert(tCruiseTaskAttrList);
        //开启定时任务
        QuartzTask quartzTask = new QuartzTask();
        quartzTask.setJobName(tCruiseTask.getTaskName());
        quartzTask.setJobGroup(jobName);
        if(tCruiseTask.getDateType()== null){
            if(tCruiseTask.getIfRun() == 173){
                //立即执行
                try {
                    RunAtNowTask runAtNowTask = new RunAtNowTask(tCruiseTask,waitTime,picModelPath,redisTemplate,
                            tCruisePointInstanceDao ,tCameraPresetDao,tCruiseResultDao,tAlgorithmConfDao,tAlgorithmInfoDao,tCruisePlanAttrDao,
                            tCruiseDataResultDao,tCruiseTaskResultDetailDao,tCruiseTaskResultDao,false);
                    Thread thread = new Thread(runAtNowTask);
                    thread.setDaemon(true);
                    thread.start();
                } catch (Exception e) { e.getMessage(); }
            }else {
                //定时
                quartzTask.setStartTime(tCruiseTask.getStartTime());
                JobManager jobManager = new JobManager();
                try {
                    jobManager.addCruiseTaskJobAtTime(quartzTask, tCruiseTask.getTaskId());
                } catch (Exception e) { e.getMessage(); }
            }
        }else{
            //周期 0 */10 * * * ?
            quartzTask.setCronExpression(tCruiseTask.getDateType());
            //quartzTask.setCronExpression("0 */1 * * * ?");
            log.info("quartzTask: "+quartzTask.getCronExpression());
            JobManager jobManager = new JobManager();
            try {
                jobManager.addCruiseTaskJob(quartzTask, tCruiseTask.getTaskId());
            } catch (Exception e) { e.getMessage(); }
        }

        return tCruiseTask.getTaskId();
    }

    @Logs(title = "删除", code = "TCruiseTask",content = "根据web传入的参数删除任务")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskId, String startTime) {
        TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        if (Objects.nonNull(tCruiseTask.getIfRun()) && tCruiseTask.getIfRun()==172 ) {
            if(!startTime.equals("-1")){
                TCruiseTaskDel tCruiseTaskDel = new TCruiseTaskDel();
                tCruiseTaskDel.setTaskId(taskId);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM
                try {
                    Date taskDate = simpleDateFormat.parse(startTime);
                    tCruiseTaskDel.setDelTime(taskDate);
                } catch (Exception e) { e.getMessage(); }
                tCruiseTaskDel.setCreateTime(new Date());
                log.info("del task single...");
                return tCruiseTaskDelDao.insert(tCruiseTaskDel);
            }else {
                //删除整个周期任务
                for (ConcurrentHashMap<String,Object> mapItem: Constant.taskMap) {
                    //找到任务Id
                    if(mapItem.get("taskId").equals(taskId)){
                        JobManager.removeJob(mapItem.get("jobName").toString(),mapItem.get("jobGroupName").toString(),mapItem.get("triggerName").toString(),mapItem.get("triggerGroupName").toString());
                        Constant.taskMap.remove(mapItem);
                    }
                }
                log.info("del task totally...");
                tCruiseTaskAttrDao.deleteByPrimaryId(taskId);
                tCruiseTaskDelDao.deleteByPrimaryId(taskId);
                return this.tCruiseTaskDao.deleteByPrimaryId(taskId);
            }
        } else if (Objects.nonNull(tCruiseTask.getIfRun()) && tCruiseTask.getIfRun()==174){
            //删除定时任务
            for (ConcurrentHashMap<String,Object> mapItem: Constant.taskMap) {
                //找到任务Id
                if(mapItem.get("taskId").equals(taskId)){
                    //删除定时任务
                    JobManager.removeJob(mapItem.get("jobName").toString(),mapItem.get("jobGroupName").toString(),mapItem.get("triggerName").toString(),mapItem.get("triggerGroupName").toString());
                    Constant.taskMap.remove(mapItem);
                }
            }
        }
        tCruiseTaskAttrDao.deleteByPrimaryId(taskId);
        tCruiseTaskDelDao.deleteByPrimaryId(taskId);
        return this.tCruiseTaskDao.deleteByPrimaryId(taskId);
    }

    @Logs(title = "更新", code = "TCruiseTask",content = "根据web传入的参数更新数据")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTask tCruiseTask) {
        return this.tCruiseTaskDao.update(tCruiseTask);
    }

    @Logs(title = "主键查询", code = "TCruiseTask",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTask selectByPrimaryId(String taskId) {
        return this.tCruiseTaskDao.selectByPrimaryId(taskId);
    }

    @Logs(title = "查询", code = "TCruiseTask",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTask> select(String taskId, String taskName, Long planId, String areaId, Integer type, Integer ifRun, Long robotId, String dateType, Integer taskType, Date startTime, Date createTime) {
        List<TCruiseTask> tCruiseTaskList = tCruiseTaskDao.select(taskId, taskName, planId, areaId, type, ifRun, robotId, dateType, taskType, startTime, createTime);
        return tCruiseTaskList;
    }

    @Logs(title = "分页查询", code = "TCruiseTask",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTask> selectByPage(TCruiseTask tCruiseTask) {
        List<TCruiseTask> tCruiseTaskList = tCruiseTaskDao.selectByPage(tCruiseTask);
        return tCruiseTaskList;
    }

    @Logs(title = "批量插入", code = "TCruiseTask",content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTask> list) {
        return this.tCruiseTaskDao.batchInsert(list);
    }

    //任务统计
    @Logs(title = "任务统计",code = "TCruiseTask",content = "任务统计")
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
//            Date date = new Date();
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(Calendar.MONTH, date.getMonth());
//            int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
//            calendar.set(Calendar.DAY_OF_MONTH, fDay);
//            firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, date.getMonth()-1);
            if((date.getMonth())==1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = sdfF.format(calendar.getTime())+" 23:59:59";
            log.info("firstDay: "+firstDay);

            int lDay=0;
            calendar.set(Calendar.MONTH, date.getMonth());
            //2月的平年瑞年天数
            if(date.getMonth()==1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            }else { lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); }
            calendar.set(Calendar.MONTH, date.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime())+" 23:59:59";
            log.info("lastDay: "+lastDay);
        } else {
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
//            calendar.set(Calendar.YEAR, taskStartDate.getYear()+1900);
//            int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
//            calendar.set(Calendar.DAY_OF_MONTH, fDay);
//            firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, taskStartDate.getMonth()-1);
            if((taskStartDate.getMonth())==1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = sdfF.format(calendar.getTime())+" 23:59:59";
            log.info("firstDay: "+firstDay);

            int lDay=0;
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            //2月的平年瑞年天数
            if(taskStartDate.getMonth()==1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            }else { lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); }
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            calendar.set(Calendar.YEAR, taskStartDate.getYear()+1900);
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime())+" 00:00:00";
            log.info("lastDay: "+lastDay);
        }

        try {
            originTime = format.parse("2000-01-01 00:00:00");
            dayBefore = format.parse(firstDay);
            dayAfter = format.parse(lastDay);
        } catch (Exception e) { e.getMessage(); }
        List<TCruiseTaskCount> list = new ArrayList<>();
        list = this.tCruiseTaskDao.taskCount(dayBefore,dayAfter);
        log.info("list: "+list);
        List<TCruiseTaskDel> listDel = this.tCruiseTaskDelDao.slectByTimeZone(dayBefore, dayAfter);
        List<Map<String, Object>> listTask = new ArrayList<>();
        for (TCruiseTaskCount tCruiseTaskCount:list) {
            if (tCruiseTaskCount.getIfRun() == 172 && tCruiseTaskCount.getStartTime().compareTo(originTime) == 0) {
                List<Date> timeList = DateTimeUtil.cornTransTime(tCruiseTaskCount.getDateType(), dayBefore, dayAfter);
                for (Date aTimeList:timeList) {
                    Map<String, Object> taskCountMap = new HashMap<>();
                    Map<String, Object> taskCountMapDel = new HashMap<>();
                    long taskTime = aTimeList.getTime();
                    if (listDel.size()>0) {
                        for (TCruiseTaskDel tCruiseTaskDel:listDel) {
                            long taskDelTime = tCruiseTaskDel.getDelTime().getTime();
                            if (Objects.equals(tCruiseTaskDel.getTaskId(), tCruiseTaskCount.getTaskId()) && taskDelTime==taskTime) {
                                log.info("已删除的任务信息： "+tCruiseTaskCount.getTaskId()+" "+taskDelTime);
                                taskCountMapDel.put("taskId", tCruiseTaskCount.getTaskId());
                                taskCountMapDel.put("taskDelTime", taskDelTime);
                            }
                        }
                        if (taskCountMapDel.size()==0) {
                            taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                            taskCountMap.put("total",tCruiseTaskCount.getTotal());
                            taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                            taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                            taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                            taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                            //TODO 增加redis获取任务状态，1是真
                            if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                                taskCountMap.put("taskState", 238);
                                taskCountMap.put("taskStateName", "任务未开始");
                            } else {
                                taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                                taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());

                            }
                            if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                                taskCountMap.put("taskStatus", "-1");
                            } else { taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus()); }
                            taskCountMap.put("startTime", sdfF2.format(aTimeList));
                            listTask.add(taskCountMap);
                        }
                    } else {
                        taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                        taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                        taskCountMap.put("total",tCruiseTaskCount.getTotal());
                        taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                        taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                        taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                        //TODO 增加redis获取任务状态，1是真
                        if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                            taskCountMap.put("taskState", 238);
                            taskCountMap.put("taskStateName", "任务未开始");
                        } else {
                            taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                            taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());

                        }
                        if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                            taskCountMap.put("taskStatus", "-1");
                        } else { taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus()); }
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
                //TODO 增加redis获取任务状态，1是真
                if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                    taskCountMap.put("taskState", 238);
                    taskCountMap.put("taskStateName", "任务未开始");
                } else {
                    taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                    taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());

                }
                if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                    taskCountMap.put("taskStatus", "-1");
                } else { taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus()); }

                taskCountMap.put("startTime", sdfF2.format(tCruiseTaskCount.getStartTime()));
                listTask.add(taskCountMap);
            }
        }
        log.info("listTask: "+listTask);
        return listTask;
    }

    @Logs(title = "分页查询", code = "TCruiseTask",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskList> selectPointStatus(String taskId) {
        List<TCruiseTaskList> tCruiseTaskList = tCruiseTaskDao.selectPointStatus(taskId);
        return tCruiseTaskList;
    }

    @Logs(title = "任务暂停", code = "TCruiseTask",content = "任务暂停")
    @Transactional(rollbackFor = Exception.class)
    public int taskPause(String taskId) {
        TCruiseResult tCruiseResult = tCruiseResultDao.selectForTaskId(taskId);
        tCruiseResult.setCState(241);
        return tCruiseResultDao.update(tCruiseResult);
    }

    @Logs(title = "任务继续", code = "TCruiseTask",content = "任务继续")
    @Transactional(rollbackFor = Exception.class)
    public int taskGoOn(String taskId) {
        TCruiseResult tCruiseResult = tCruiseResultDao.selectForTaskId(taskId);
        tCruiseResult.setCState(239);
        TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        RunAtNowTask runAtNowTask = new RunAtNowTask(tCruiseTask,waitTime,picModelPath,redisTemplate,
                tCruisePointInstanceDao ,tCameraPresetDao,tCruiseResultDao,tAlgorithmConfDao,tAlgorithmInfoDao,tCruisePlanAttrDao,
                tCruiseDataResultDao,tCruiseTaskResultDetailDao,tCruiseTaskResultDao,true);
        Thread thread = new Thread(runAtNowTask);
        thread.setDaemon(true);
        thread.start();
        return tCruiseResultDao.update(tCruiseResult);
    }

    @Logs(title = "任务终止", code = "TCruiseTask",content = "任务终止")
    @Transactional(rollbackFor = Exception.class)
    public int taskShutDown(String taskId) {
        TCruiseResult tCruiseResult = tCruiseResultDao.selectForTaskId(taskId);
        tCruiseResult.setCState(242);
        return tCruiseResultDao.update(tCruiseResult);
    }

    @Logs(title = "查找任务", code = "TCruiseTask",content = "根据参数过滤查找任务")
    @Transactional(rollbackFor = Exception.class)
    public  List<Map<String, Object>>  taskCountByCondition(Date startTime,Date endTime,String taskState,String taskName){
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HashMap<String,Object> map = new HashMap<>();
        map.put("taskState",taskState);
        map.put("taskName",taskName);
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> resultListAfter = null;
        List<Map<String, Object>> resultListBefore = new ArrayList<>();
        List<Map<String, Object>> resultListForAdd = new ArrayList<>();
        Date now = new Date();
        try{
            if(endTime == null){
                Calendar calendar = new GregorianCalendar();
                calendar.add(Calendar.DAY_OF_MONTH,0);

                //一天的开始时间 yyyy:MM:dd 00:00:00
                calendar.set(Calendar.HOUR_OF_DAY,0);
                calendar.set(Calendar.MINUTE,0);
                calendar.set(Calendar.SECOND,0);
                calendar.set(Calendar.MILLISECOND,0);
                Date dayStart = calendar.getTime();
                String startStr = format.format(dayStart);
                now = format.parse(startStr);
                startTime = now;
                System.out.println("一天的开始时间: "+now);
                //一天的结束时间 yyyy:MM:dd 23:59:59
                calendar.set(Calendar.HOUR_OF_DAY,23);
                calendar.set(Calendar.MINUTE,59);
                calendar.set(Calendar.SECOND,59);
                calendar.set(Calendar.MILLISECOND,999);
                Date dayEnd = calendar.getTime();
                String endStr = format.format(dayEnd);
                endTime = format.parse(endStr);
                System.out.println("一天的开始时间: "+endTime);
            }
        }catch (Exception e) { e.getMessage(); }

        map.put("startTime",startTime);
        map.put("endTime",endTime);
        resultListBefore = this.afterTaskCount(startTime,taskState,taskName);
        resultListAfter = this.afterTaskCount(endTime,taskState,taskName);

        resultListForAdd.addAll(resultListBefore);

        for (Map<String, Object> itemBefore:resultListBefore) {
            for (Map<String, Object> itemAfter:resultListAfter) {
                if(itemAfter.get("taskId").toString().equals(itemBefore.get("taskId").toString())){
                    resultListForAdd.remove(itemBefore);
                    break;
                }
            }
        }
        resultListForAdd.addAll(resultListAfter);
        if(resultListForAdd == null || resultListForAdd.size() == 0){
            return resultList;
        }else {
            try {
            for (Map<String, Object> item:resultListForAdd){

                if (startTime.compareTo(format.parse(item.get("startTime").toString())) <= 0  && endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0){
                    if(taskState != null){
//                        System.out.println("------------:"+item.get("taskState"));
//                        System.out.println("-------------"+taskState.equals(item.get("taskState").toString()));
                        if(taskState.equals(item.get("taskState").toString())){
                            resultList.add(item);
                            continue;
                        }else {
                            continue;
                        }
                    }
                    resultList.add(item);
                }
            }
            } catch (Exception e) { e.getMessage(); }
        }
        return resultList;
    }

    public List<Map<String, Object>> afterTaskCount(Date taskStartDate,String taskState,String taskName){

        SimpleDateFormat sdfF = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfF2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dayBefore = new Date();
        Date dayAfter = new Date();
        Date originTime = new Date();
        String firstDay = "", lastDay = "";
        if (Objects.equals(null, taskStartDate)) {
//            Date date = new Date();
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(Calendar.MONTH, date.getMonth());
//            int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
//            calendar.set(Calendar.DAY_OF_MONTH, fDay);
//            firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, date.getMonth()-1);
            if((date.getMonth())==1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = sdfF.format(calendar.getTime())+" 23:59:59";
            log.info("firstDay: "+firstDay);

            int lDay=0;
            calendar.set(Calendar.MONTH, date.getMonth());
            //2月的平年瑞年天数
            if(date.getMonth()==1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            }else { lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); }
            calendar.set(Calendar.MONTH, date.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime())+" 23:59:59";
            log.info("lastDay: "+lastDay);
        } else {
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
//            calendar.set(Calendar.YEAR, taskStartDate.getYear()+1900);
//            int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
//            calendar.set(Calendar.DAY_OF_MONTH, fDay);
//            firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, taskStartDate.getMonth()-1);
            if((taskStartDate.getMonth())==1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = sdfF.format(calendar.getTime())+" 23:59:59";
            log.info("firstDay: "+firstDay);

            int lDay=0;
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            //2月的平年瑞年天数
            if(taskStartDate.getMonth()==1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            }else { lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH); }
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            calendar.set(Calendar.YEAR, taskStartDate.getYear()+1900);
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime())+" 00:00:00";
            log.info("lastDay: "+lastDay);
        }

        try {
            originTime = format.parse("2000-01-01 00:00:00");
            dayBefore = format.parse(firstDay);
            dayAfter = format.parse(lastDay);
        } catch (Exception e) { e.getMessage(); }
        List<TCruiseTaskCount> list = new ArrayList<>();
        HashMap<String,Object> map = new HashMap<>();
        map.put("startTime",dayBefore);
        map.put("endTime",dayAfter);
        map.put("taskState",taskState);
        map.put("taskName",taskName);
        list = this.tCruiseTaskDao.afterTaskCount(map);
        log.info("list: "+list);
        List<TCruiseTaskDel> listDel = this.tCruiseTaskDelDao.slectByTimeZone(dayBefore, dayAfter);
        List<Map<String, Object>> listTask = new ArrayList<>();
        for (TCruiseTaskCount tCruiseTaskCount:list) {
            if (tCruiseTaskCount.getIfRun() == 172 && tCruiseTaskCount.getStartTime().compareTo(originTime) == 0) {
                List<Date> timeList = DateTimeUtil.cornTransTime(tCruiseTaskCount.getDateType(), dayBefore, dayAfter);
                for (Date aTimeList:timeList) {
                    Map<String, Object> taskCountMap = new HashMap<>();
                    Map<String, Object> taskCountMapDel = new HashMap<>();
                    long taskTime = aTimeList.getTime();
                    if (listDel.size()>0) {
                        for (TCruiseTaskDel tCruiseTaskDel:listDel) {
                            long taskDelTime = tCruiseTaskDel.getDelTime().getTime();
                            if (Objects.equals(tCruiseTaskDel.getTaskId(), tCruiseTaskCount.getTaskId()) && taskDelTime==taskTime) {
                                log.info("已删除的任务信息： "+tCruiseTaskCount.getTaskId()+" "+taskDelTime);
                                taskCountMapDel.put("taskId", tCruiseTaskCount.getTaskId());
                                taskCountMapDel.put("taskDelTime", taskDelTime);
                            }
                        }
                        if (taskCountMapDel.size()==0) {
                            taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                            taskCountMap.put("total",tCruiseTaskCount.getTotal());
                            taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                            taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                            taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                            taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                            //TODO 增加redis获取任务状态，1是真
                            if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                                taskCountMap.put("taskState", 238);
                                taskCountMap.put("taskStateName", "任务未开始");
                            } else {
                                taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                                taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());

                            }
                            if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                                taskCountMap.put("taskStatus", "-1");
                            } else { taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus()); }
                            taskCountMap.put("startTime", sdfF2.format(aTimeList));
                            listTask.add(taskCountMap);
                        }
                    } else {
                        taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                        taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                        taskCountMap.put("total",tCruiseTaskCount.getTotal());
                        taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                        taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                        taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                        //TODO 增加redis获取任务状态，1是真
                        if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                            taskCountMap.put("taskState", 238);
                            taskCountMap.put("taskStateName", "任务未开始");
                        } else {
                            taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                            taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());

                        }
                        if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                            taskCountMap.put("taskStatus", "-1");
                        } else { taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus()); }
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
                //TODO 增加redis获取任务状态，1是真
                if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                    taskCountMap.put("taskState", 238);
                    taskCountMap.put("taskStateName", "任务未开始");
                } else {
                    taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                    taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());
                }
                if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                    taskCountMap.put("taskStatus", "-1");
                } else { taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus()); }

                taskCountMap.put("startTime", sdfF2.format(tCruiseTaskCount.getStartTime()));
                listTask.add(taskCountMap);
            }
        }
        log.info("listTask: "+listTask);
        return listTask;
    }



}

