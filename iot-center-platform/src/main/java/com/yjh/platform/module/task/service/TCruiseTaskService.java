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
import com.yjh.platform.common.utils.smUtil.Demo;
import com.yjh.platform.module.device.dao.TAlgorithmConfBakDao;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.task.dao.*;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.*;
import com.yjh.platform.module.user.entity.*;
import io.swagger.models.auth.In;
import lombok.Data;
import lombok.NonNull;
import org.apache.commons.lang.RandomStringUtils;
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
    private TCameraPresetDao tCameraPresetDao;
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
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    @Autowired
    private TSysParamDao tSysParamDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private TAlgorithmConfBakDao tAlgorithmConfBakDao;

    //模板图片路径
    private String picModelPath;
    //等待相机转到预置位时间
    private Long waitTime;
    ;
    //jobName
    @Value("${spring.QingHua.jobName}")
    private String jobName;
    //任务超期时间
    private Float tasksAreTime;
    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";

    private Logger log = LoggerFactory.getLogger(TCruiseTaskService.class);

    @Transactional(rollbackFor = Exception.class)
    public String insert(TCruiseTask tCruiseTask) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date startTime = null;
        try {
            if (tCruiseTask.getIfRun() == 172) {
                startTime = format.parse("2000-01-01 00:00:00");
                tCruiseTask.setStartTime(startTime);
            }
        } catch (Exception e) {
            e.getMessage();
        }
        if (Objects.isNull(tCruiseTask.getTaskId()))
            tCruiseTask.setTaskId(String.valueOf(UUID.randomUUID()).replace("-", ""));
        tCruiseTask.setTaskCode(tCruiseTask.getTaskId());
        List<TCruisePlanAttr> tCruisePlanAttrList = tCruisePlanAttrDao.select(tCruiseTask.getPlanId(), null, null, null, null, null, null, null, null, null, null, null, null, null);
        List<Long> instanceList = new ArrayList<>();
        for (TCruisePlanAttr tCruisePlanAttr : tCruisePlanAttrList) {
            instanceList.add(tCruisePlanAttr.getInstanceId());
        }
        List<TCruisePointInstance> tCruisePointInstanceList = this.tCruiseTaskAttrDao.batchSelect(instanceList);
        List<TCruiseTaskAttr> tCruiseTaskAttrList = new ArrayList<>();
        for (TCruisePointInstance tCruisePointInstance : tCruisePointInstanceList) {
            TCruiseTaskAttr tCruiseTaskAttr = new TCruiseTaskAttr();
            tCruiseTaskAttr.setTaskId(tCruiseTask.getTaskId());
            tCruiseTaskAttr.setInstanceId(tCruisePointInstance.getInstanceId());
            tCruiseTaskAttr.setDeviceMeteId(tCruisePointInstance.getDeviceMeteId());
            tCruiseTaskAttr.setDeviceId(tCruisePointInstance.getDeviceId());
            tCruiseTaskAttr.setCustomId(tCruisePointInstance.getCustomId());
            tCruiseTaskAttr.setPointTaskId(String.valueOf(tCruisePointInstance.getCruiseId()));
            tCruiseTaskAttrList.add(tCruiseTaskAttr);
        }

        //判断机器人是否空闲，不空闲则二次确认
//        TSysParam tSysParamConfirm= tSysParamDao.selectByParamType("confirmExpireTime");
//        long confirmExpireTime = Long.valueOf(tSysParamConfirm.getContent());
//        List<ConfirmImmediately> inspectionIdList = tRobotInspectionDao.selectRobotInspectionIds();
//        if (Objects.isNull(Constant.confirmImmediatelyMap.get(tCruiseTask.getTaskId())) && tCruiseTask.getIfRun() == 173) {
//            Constant.confirmImmediatelyMap.put(tCruiseTask.getTaskId(), 0);
//            for (TCruiseTaskAttr tCruiseTaskAttr:tCruiseTaskAttrList) {
//                for (ConfirmImmediately confirmImmediately:inspectionIdList){
//                    String inspectionIdString =  String.valueOf(confirmImmediately.getInspectionId());
//                    if (tCruiseTaskAttr.getPointTaskId().equals(inspectionIdString)) {
//                        Map<String, Object> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:"+ confirmImmediately.getRobotCode() +":41");
//                        new Thread(() -> {
//                            try { Thread.sleep(confirmExpireTime); } catch (Exception e) {e.getMessage();}
//                            if (Objects.nonNull(Constant.confirmImmediatelyMap.get(tCruiseTask.getTaskId()))) Constant.confirmImmediatelyMap.remove(tCruiseTask.getTaskId());
//                        }).start();
//                        if (Objects.nonNull(robotStatusMap.get("value")) && String.valueOf(robotStatusMap.get("value")).equals("2")) {
//                            JSONObject jsonObject = new JSONObject();
//                            jsonObject.put("robotId", confirmImmediately.getRobotId());
//                            jsonObject.put("robotName", confirmImmediately.getRobotName());
//                            jsonObject.put("taskId", tCruiseTask.getTaskId());
//                            return jsonObject.toString();
//                        }
//                    }
//                }
//            }
//        }
//        if (Objects.nonNull(Constant.confirmImmediatelyMap.get(tCruiseTask.getTaskId())) && Objects.equals(Constant.confirmImmediatelyMap.get(tCruiseTask.getTaskId()), 0)
//                && tCruiseTask.getIfRun() == 173)
//            Constant.confirmImmediatelyMap.remove(tCruiseTask.getTaskId());

        this.tCruiseTaskDao.insert(tCruiseTask);
        this.tCruiseTaskAttrDao.batchInsert(tCruiseTaskAttrList);
        //开启定时任务
        QuartzTask quartzTask = new QuartzTask();
        quartzTask.setJobName(tCruiseTask.getTaskName());
        quartzTask.setJobGroup(jobName);
        if (tCruiseTask.getDateType() == null) {
            if (tCruiseTask.getIfRun() == 173) {
                //立即执行
                try {
                    //模板图片路径
                    Map<String, Object> mapForPicModelPath = redisTemplate.opsForHash().entries("t_sys_param:picModelPath");
                    picModelPath = (String) mapForPicModelPath.get("content");
                    //等待相机转到预置位时间
                    Map<String, Object> mapForWaitTime = redisTemplate.opsForHash().entries("t_sys_param:waitTime");
                    waitTime = Long.valueOf((String) mapForWaitTime.get("content"));
                    //任务超期时间
                    Map<String, Object> mapForTaskAreTime = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
                    tasksAreTime = Float.valueOf((String) mapForTaskAreTime.get("content"));
                    RunAtNowTask runAtNowTask = new RunAtNowTask(tCruiseTask, waitTime, picModelPath, redisTemplate,
                            tCruisePointInstanceDao, tCameraPresetDao, tCruiseResultDao, tAlgorithmConfDao, tAlgorithmInfoDao, tCruisePlanAttrDao,
                            tCruiseDataResultDao, tCruiseTaskResultDetailDao, tCruiseTaskResultDao, false, tasksAreTime,
                            tRobotInspectionDao, tAlgorithmConfBakDao);
                    Thread thread = new Thread(runAtNowTask);
                    thread.setDaemon(true);
                    thread.start();
                } catch (Exception e) {
                    e.getMessage();
                }
            } else {
                //定时
                quartzTask.setStartTime(tCruiseTask.getStartTime());
                //quartzTask.setStartTime(new Date());
                JobManager jobManager = new JobManager();
                try {
                    jobManager.addCruiseTaskJobAtTime(quartzTask, tCruiseTask.getTaskId());
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        } else {
            //周期 0 */10 * * * ?
            quartzTask.setCronExpression(tCruiseTask.getDateType());
            //quartzTask.setCronExpression("0 */1 * * * ?");
            log.info("quartzTask: " + quartzTask.getCronExpression());
            JobManager jobManager = new JobManager();
            try {
                jobManager.addCruiseTaskJob(quartzTask, tCruiseTask.getTaskId());
            } catch (Exception e) {
                e.getMessage();
            }
        }

        //任务状态上报站端
        sendTaskStateToUp(tCruiseTask, 5);

        return tCruiseTask.getTaskId();
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskId, String startTime) {
        TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        if (Objects.nonNull(tCruiseTask.getIfRun()) && tCruiseTask.getIfRun() == 172) {
            if (!startTime.equals("-1")) {
                TCruiseTaskDel tCruiseTaskDel = new TCruiseTaskDel();
                tCruiseTaskDel.setTaskId(taskId);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM
                try {
                    Date taskDate = simpleDateFormat.parse(startTime);
                    tCruiseTaskDel.setDelTime(taskDate);
                } catch (Exception e) {
                    e.getMessage();
                }
                tCruiseTaskDel.setCreateTime(new Date());
                log.info("del task single...");
                return tCruiseTaskDelDao.insert(tCruiseTaskDel);
            } else {
                //判断当前周期任务是否已执行 --by tt 2021.3.10
                if (Objects.isNull(tCruiseTask.getDateType())) { taskId = tCruiseTask.getTaskCode(); }
                log.info("del taskId..."+taskId);
                //删除整个周期任务
                for (Iterator<ConcurrentHashMap<String, Object>> it = Constant.taskMap.iterator(); it.hasNext(); ) {
                    ConcurrentHashMap<String, Object> mapItem = it.next();
                    //找到任务Id
                    if (mapItem.get("taskId").equals(taskId)) {
                        JobManager.removeJob(mapItem.get("jobName").toString(), mapItem.get("jobGroupName").toString(), mapItem.get("triggerName").toString(), mapItem.get("triggerGroupName").toString());
                        Constant.taskMap.remove(mapItem);
                    }
                }
                log.info("del task totally...");
                tCruiseTaskAttrDao.deleteByPrimaryId(taskId);
                tCruiseTaskDelDao.deleteByPrimaryId(taskId);
                return this.tCruiseTaskDao.deleteByPrimaryId(taskId);
            }
        } else if (Objects.nonNull(tCruiseTask.getIfRun()) && tCruiseTask.getIfRun() == 174) {
            //删除定时任务
            for (ConcurrentHashMap<String, Object> mapItem : Constant.taskMap) {
                //找到任务Id
                if (mapItem.get("taskId").equals(taskId)) {
                    //删除定时任务
                    JobManager.removeJob(mapItem.get("jobName").toString(), mapItem.get("jobGroupName").toString(), mapItem.get("triggerName").toString(), mapItem.get("triggerGroupName").toString());
                    Constant.taskMap.remove(mapItem);
                }
            }
        }
        tCruiseTaskAttrDao.deleteByPrimaryId(taskId);
        tCruiseTaskDelDao.deleteByPrimaryId(taskId);
        return this.tCruiseTaskDao.deleteByPrimaryId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTask tCruiseTask) {
        return this.tCruiseTaskDao.update(tCruiseTask);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseTask selectByPrimaryId(String taskId) {
        return this.tCruiseTaskDao.selectByPrimaryId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTask> select(String taskId, String taskName, Long planId, String areaId, Integer type, Integer ifRun, Long robotId, String dateType, Integer taskType, Integer taskLevel, Date startTime, Date createTime) {
        List<TCruiseTask> tCruiseTaskList = tCruiseTaskDao.select(taskId, taskName, planId, areaId, type, ifRun, robotId, dateType, taskType, taskLevel, startTime, createTime);
        return tCruiseTaskList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTask> selectByPage(TCruiseTask tCruiseTask) {
        List<TCruiseTask> tCruiseTaskList = tCruiseTaskDao.selectByPage(tCruiseTask);
        return tCruiseTaskList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTask> list) {
        return this.tCruiseTaskDao.batchInsert(list);
    }

    //任务统计
    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> taskCount(Date taskStartDate) {

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
            calendar.set(Calendar.MONTH, date.getMonth() - 1);
            if ((date.getMonth()) == 1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            } else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("firstDay: " + firstDay);

            int lDay = 0;
            calendar.set(Calendar.MONTH, date.getMonth());
            //2月的平年瑞年天数
            if (date.getMonth() == 1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            } else {
                lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            calendar.set(Calendar.MONTH, date.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("lastDay: " + lastDay);
        } else {
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
//            calendar.set(Calendar.YEAR, taskStartDate.getYear()+1900);
//            int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
//            calendar.set(Calendar.DAY_OF_MONTH, fDay);
//            firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, taskStartDate.getMonth() - 1);
            if ((taskStartDate.getMonth()) == 1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            } else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("firstDay: " + firstDay);

            int lDay = 0;
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            //2月的平年瑞年天数
            if (taskStartDate.getMonth() == 1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            } else {
                lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            calendar.set(Calendar.YEAR, taskStartDate.getYear() + 1900);
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("lastDay: " + lastDay);
        }

        try {
            originTime = format.parse("2000-01-01 00:00:00");
            dayBefore = format.parse(firstDay);
            dayAfter = format.parse(lastDay);
        } catch (Exception e) {
            e.getMessage();
        }
        List<TCruiseTaskCount> list = new ArrayList<>();
        list = this.tCruiseTaskDao.taskCount(dayBefore, dayAfter);
        log.info("list: " + list);
        List<TCruiseTaskDel> listDel = this.tCruiseTaskDelDao.slectByTimeZone(dayBefore, dayAfter);
        List<Map<String, Object>> listTask = new ArrayList<>();
        for (TCruiseTaskCount tCruiseTaskCount : list) {
            if (tCruiseTaskCount.getIfRun() == 172 && tCruiseTaskCount.getStartTime().compareTo(originTime) == 0) {
                List<Date> timeList = DateTimeUtil.cornTransTime(tCruiseTaskCount.getDateType(), dayBefore, dayAfter);
                for (Date aTimeList : timeList) {
                    Map<String, Object> taskCountMap = new HashMap<>();
                    Map<String, Object> taskCountMapDel = new HashMap<>();
                    long taskTime = aTimeList.getTime();
                    if (listDel.size() > 0) {
                        for (TCruiseTaskDel tCruiseTaskDel : listDel) {
                            long taskDelTime = tCruiseTaskDel.getDelTime().getTime();
                            if (Objects.equals(tCruiseTaskDel.getTaskId(), tCruiseTaskCount.getTaskId()) && taskDelTime == taskTime) {
                                log.info("已删除的任务信息： " + tCruiseTaskCount.getTaskId() + " " + taskDelTime);
                                taskCountMapDel.put("taskId", tCruiseTaskCount.getTaskId());
                                taskCountMapDel.put("taskDelTime", taskDelTime);
                            }
                        }
                        if (taskCountMapDel.size() == 0) {
                            taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                            taskCountMap.put("total", tCruiseTaskCount.getTotal());
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
                            } else {
                                taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                            }
                            taskCountMap.put("startTime", sdfF2.format(aTimeList));
                            listTask.add(taskCountMap);
                        }
                    } else {
                        taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                        taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                        taskCountMap.put("total", tCruiseTaskCount.getTotal());
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
                        } else {
                            taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                        }
                        taskCountMap.put("startTime", sdfF2.format(aTimeList));
                        listTask.add(taskCountMap);
                    }
                }
            } else {
                Map<String, Object> taskCountMap = new HashMap<>();
                taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                taskCountMap.put("total", tCruiseTaskCount.getTotal());
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
                } else {
                    taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                }

                taskCountMap.put("startTime", sdfF2.format(tCruiseTaskCount.getStartTime()));
                listTask.add(taskCountMap);
            }
        }
        log.info("listTask: " + listTask);
        return listTask;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskList> selectPointStatus(String taskId) {
        List<TCruiseTaskList> tCruiseTaskList = tCruiseTaskDao.selectPointStatus(taskId);
        return tCruiseTaskList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskPause(String taskId) {
        TCruiseResult tCruiseResult = tCruiseResultDao.selectForTaskId(taskId);
        tCruiseResult.setCState(241);
        try {
            //TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
//            //给算法暂停
//            Analysis analysis = new Analysis();
//            analysis.setTaskId(tCruiseTask.getTaskId());
//            analysis.setInstanceId(-1L);
//            List<Analysis> analysisList = new ArrayList<>();
//            analysisList.add(analysis);
//            Map<String, List<Analysis>> analysisMap  = new HashMap<>();
//            analysisMap.put("list",analysisList);
//            log.info("算法信息：    "+analysisMap);
//            analysis(analysisMap);
//            log.info("任务暂停"+tCruiseTask.getTaskId());

            //Thread.sleep(10000);
            //机器人任务暂停
            List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(taskId);
            if (robotCodeList != null && robotCodeList.size() > 0) {
                Map<String, Object> robotTaskStatesMap = new HashMap<>();
                robotTaskStatesMap.put("taskId", taskId);
                robotTaskStatesMap.put("commandValue", 2);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                robotTaskStates(robotTaskStatesMap);
            }
            Map<String,String> jasonMapOnFinished=new HashMap<>();
            jasonMapOnFinished.put("type","taskChange");
            jasonMapOnFinished.put("taskId",taskId);
            String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
            log.info("发送给前端的消息："+jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);
        } catch (Exception e) {
            log.error("任务暂停异常: " + e);
            e.printStackTrace();
        }

        //任务状态上报站端
        TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(tCruiseTask, 3);

        return tCruiseResultDao.update(tCruiseResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskGoOn(String taskId) throws Exception {
        TCruiseResult tCruiseResult = tCruiseResultDao.selectForTaskId(taskId);
        if (tCruiseResult.getCState() == 240) {
            return 1;
        }
        tCruiseResult.setCState(239);
        TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        //模板图片路径
        Map<String, Object> mapForPicModelPath = redisTemplate.opsForHash().entries("t_sys_param:picModelPath");
        picModelPath = (String) mapForPicModelPath.get("content");
        //等待相机转到预置位时间
        Map<String, Object> mapForWaitTime = redisTemplate.opsForHash().entries("t_sys_param:waitTime");
        waitTime = Long.valueOf((String) mapForWaitTime.get("content"));
        //任务超期时间
        Map<String, Object> mapForTaskAreTime = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
        tasksAreTime = Float.valueOf((String) mapForTaskAreTime.get("content"));
        RunAtNowTask runAtNowTask = new RunAtNowTask(tCruiseTask, waitTime, picModelPath, redisTemplate,
                tCruisePointInstanceDao, tCameraPresetDao, tCruiseResultDao, tAlgorithmConfDao, tAlgorithmInfoDao, tCruisePlanAttrDao,
                tCruiseDataResultDao, tCruiseTaskResultDetailDao, tCruiseTaskResultDao, true, tasksAreTime,
                tRobotInspectionDao, tAlgorithmConfBakDao);
        Thread thread = new Thread(runAtNowTask);
        thread.setDaemon(true);
        thread.start();

        //机器人任务继续
        List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(taskId);
        if (robotCodeList != null && robotCodeList.size() > 0) {
            Map<String, Object> robotTaskStatesMap = new HashMap<>();
            robotTaskStatesMap.put("taskId", taskId);
            robotTaskStatesMap.put("commandValue", 3);
            robotTaskStatesMap.put("robotCodeList", robotCodeList);
            robotTaskStates(robotTaskStatesMap);
        }

        Map<String,String> jasonMapOnFinished=new HashMap<>();
        jasonMapOnFinished.put("type","taskChange");
        jasonMapOnFinished.put("taskId",taskId);
        String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
        log.info("发送给前端的消息："+jsonMessage);
        Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);

        //任务状态上报站端
        //TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(tCruiseTask, 2);
        return tCruiseResultDao.update(tCruiseResult);
    }

    private void robotTaskStates(Map<String, Object> robotTaskStatesMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(Constant.ROBOT_TASK_STATUS_URL, robotTaskStatesMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskShutDown(String taskId) {
        TCruiseResult tCruiseResult = tCruiseResultDao.selectForTaskId(taskId);
        if (tCruiseResult.getCState() == 240) {
            return 1;
        }
        tCruiseResult.setCState(242);
        TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        try {
            QuartzTask quartzTaskForAre = new QuartzTask();
            quartzTaskForAre.setJobName(tCruiseTask.getTaskName() + "-" + System.currentTimeMillis());
            quartzTaskForAre.setJobGroup("jiancha");
            quartzTaskForAre.setStartTime(new Date());
            JobManager jobManager = new JobManager();
            jobManager.taskShutDown(quartzTaskForAre, tCruiseTask.getTaskId());
            log.info("任务终止创建成功=="+taskId);
        } catch (Exception e) {
            log.info("任务终止创建失败" + e);
        }

        //任务状态上报站端
        //TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(tCruiseTask, 4);
        return tCruiseResultDao.update(tCruiseResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> taskCountByCondition(Date startTime, Date endTime, String taskState, String taskName) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        HashMap<String, Object> map = new HashMap<>();
        if ("-1".equals(taskState)) {
            taskState = null;
        }
        map.put("taskState", taskState);
        map.put("taskName", taskName);
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> resultListAfter = null;
        List<Map<String, Object>> resultListBefore = new ArrayList<>();
        List<Map<String, Object>> resultListForAdd = new ArrayList<>();
        Date now = new Date();
        try {
            if (endTime == null) {
                Calendar calendar = new GregorianCalendar();
                calendar.add(Calendar.DAY_OF_MONTH, 0);

                //一天的开始时间 yyyy:MM:dd 00:00:00
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date dayStart = calendar.getTime();
                String startStr = format.format(dayStart);
                now = format.parse(startStr);
                startTime = now;
                //System.out.println("一天的开始时间: "+now);
                //一天的结束时间 yyyy:MM:dd 23:59:59
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                Date dayEnd = calendar.getTime();
                String endStr = format.format(dayEnd);
                endTime = format.parse(endStr);
                //System.out.println("一天的开始时间: "+endTime);
            }
        } catch (Exception e) {
            e.getMessage();
        }

        map.put("startTime", startTime);
        map.put("endTime", endTime);
        resultListBefore = this.afterTaskCount(startTime, taskState, taskName);
        resultListAfter = this.afterTaskCount(endTime, taskState, taskName);

        resultListForAdd.addAll(resultListBefore);

        for (Map<String, Object> itemBefore : resultListBefore) {
            for (Map<String, Object> itemAfter : resultListAfter) {
                if (itemAfter.get("taskId").toString().equals(itemBefore.get("taskId").toString())) {
                    resultListForAdd.remove(itemBefore);
                    break;
                }
            }
        }
        resultListForAdd.addAll(resultListAfter);
        if (resultListForAdd == null || resultListForAdd.size() == 0) {
            return resultList;
        } else {
            try {
                for (Map<String, Object> item : resultListForAdd) {

                    if (startTime.compareTo(format.parse(item.get("startTime").toString())) <= 0 && endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0) {
                        if (taskState != null && !taskState.equals("")) {
//                        System.out.println("------------:"+item.get("taskState"));
//                        System.out.println("-------------"+taskState.equals(item.get("taskState").toString()));
                            if (taskState.equals(item.get("taskState").toString())) {
                                resultList.add(item);
                                continue;
                            } else {
                                continue;
                            }
                        }
                        resultList.add(item);
                    }
                }
            } catch (Exception e) {
                e.getMessage();
            }
        }
        return resultList;
    }

    public List<Map<String, Object>> afterTaskCount(Date taskStartDate, String taskState, String taskName) {

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
            calendar.set(Calendar.MONTH, date.getMonth() - 1);
            if ((date.getMonth()) == 1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            } else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("firstDay: " + firstDay);

            int lDay = 0;
            calendar.set(Calendar.MONTH, date.getMonth());
            //2月的平年瑞年天数
            if (date.getMonth() == 1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            } else {
                lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            calendar.set(Calendar.MONTH, date.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("lastDay: " + lastDay);
        } else {
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
//            calendar.set(Calendar.YEAR, taskStartDate.getYear()+1900);
//            int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
//            calendar.set(Calendar.DAY_OF_MONTH, fDay);
//            firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, taskStartDate.getMonth() - 1);
            if ((taskStartDate.getMonth()) == 1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            } else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("firstDay: " + firstDay);

            int lDay = 0;
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            //2月的平年瑞年天数
            if (taskStartDate.getMonth() == 1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            } else {
                lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            calendar.set(Calendar.YEAR, taskStartDate.getYear() + 1900);
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("lastDay: " + lastDay);
        }

        try {
            originTime = format.parse("2000-01-01 00:00:00");
            dayBefore = format.parse(firstDay);
            dayAfter = format.parse(lastDay);
        } catch (Exception e) {
            e.getMessage();
        }
        List<TCruiseTaskCount> list = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("startTime", dayBefore);
        map.put("endTime", dayAfter);
        map.put("taskState", taskState);
        map.put("taskName", taskName);
        list = this.tCruiseTaskDao.afterTaskCount(map);
        log.info("list: " + list);
        List<TCruiseTaskDel> listDel = this.tCruiseTaskDelDao.slectByTimeZone(dayBefore, dayAfter);
        List<Map<String, Object>> listTask = new ArrayList<>();
        for (TCruiseTaskCount tCruiseTaskCount : list) {
            if (tCruiseTaskCount.getIfRun() == 172 && tCruiseTaskCount.getStartTime().compareTo(originTime) == 0) {
                List<Date> timeList = DateTimeUtil.cornTransTime(tCruiseTaskCount.getDateType(), dayBefore, dayAfter);
                for (Date aTimeList : timeList) {
                    Map<String, Object> taskCountMap = new HashMap<>();
                    Map<String, Object> taskCountMapDel = new HashMap<>();
                    long taskTime = aTimeList.getTime();
                    if (listDel.size() > 0) {
                        for (TCruiseTaskDel tCruiseTaskDel : listDel) {
                            long taskDelTime = tCruiseTaskDel.getDelTime().getTime();
                            if (Objects.equals(tCruiseTaskDel.getTaskId(), tCruiseTaskCount.getTaskId()) && taskDelTime == taskTime) {
                                log.info("已删除的任务信息： " + tCruiseTaskCount.getTaskId() + " " + taskDelTime);
                                taskCountMapDel.put("taskId", tCruiseTaskCount.getTaskId());
                                taskCountMapDel.put("taskDelTime", taskDelTime);
                            }
                        }
                        if (taskCountMapDel.size() == 0) {
                            taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                            taskCountMap.put("total", tCruiseTaskCount.getTotal());
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
                            } else {
                                taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                            }
                            taskCountMap.put("startTime", sdfF2.format(aTimeList));
                            listTask.add(taskCountMap);
                        }
                    } else {
                        taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                        taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                        taskCountMap.put("total", tCruiseTaskCount.getTotal());
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
                        } else {
                            taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                        }
                        taskCountMap.put("startTime", sdfF2.format(aTimeList));
                        listTask.add(taskCountMap);
                    }
                }
            } else {
                Map<String, Object> taskCountMap = new HashMap<>();
                taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                taskCountMap.put("total", tCruiseTaskCount.getTotal());
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
                } else {
                    taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                }

                taskCountMap.put("startTime", sdfF2.format(tCruiseTaskCount.getStartTime()));
                listTask.add(taskCountMap);
            }
        }
        log.info("listTask: " + listTask);
        return listTask;
    }

    //表记分析
    private void analysis(Map<String, List<Analysis>> analysisMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(ALGORITHM_URL, analysisMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int taskConfirmation(String userId, String password) throws Exception {
        //return 1;
        //todo 密码的解密
        SysUser sysUser = sysUserDao.selectByPrimaryId(Long.valueOf(userId));
        //判断开关
        Map<String,String> map= redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
        Boolean flag =Boolean.valueOf(map.get("content")) ;
        if(!flag) {
            //需要自己解密数据库password
            sysUser.setPassword(Demo.decryptDB(sysUser.getPassword()));
        //}
        }else {
            //全要解密
            password = Demo.decrypt(password);
            sysUser.setPassword(Demo.decryptDB(sysUser.getPassword()));
        }
        //password = Demo.decrypt(password);
        //sysUser.setPassword(Demo.decryptDB(sysUser.getPassword()));
        if (sysUser.getPassword().equals(password)) {
            return 1;
        }
        return -1;
    }

    private Result sendTaskStateToUp(TCruiseTask tCruiseTask, Integer state) {
        //任务状态上报站端

        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        xmlBaseModel.setType("41");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
        item.put("task_patrolled_id",tCruiseTask.getTaskId()+"_"+simpleDateFormat2.format(tCruiseTask.getStartTime()));
        item.put("task_name",tCruiseTask.getTaskName());
        item.put("task_code",tCruiseTask.getTaskCode());
        item.put("task_state",state);
        item.put("plan_start_time",tCruiseTask.getStartTime());
        if(tCruiseTask.getIfRun() == 172){
            try{
                //CronExpression expression = new CronExpression(tCruiseTask.getDateType());
                item.put("start_time",tCruiseTask.getStartTime());
            }catch (Exception e){
                log.info("上报出错"+e.getMessage());
            }
        } else {
            item.put("start_time", tCruiseTask.getStartTime());
        }
        item.put("task_progress", "0%");
        Integer i = 0;
        if (state == 5) {
            i = tCruiseTaskDao.countInstance(tCruiseTask.getTaskId());
        } else {
            Map<String, String> mapForGet = redisTemplate.opsForHash().entries("countForAbnormal:" + tCruiseTask.getTaskId());
            Integer all = Integer.valueOf(mapForGet.get("all"));
            Integer normal = Integer.valueOf(mapForGet.get("normal"));
            Integer abnormal = Integer.valueOf(mapForGet.get("abnormal"));
            i = all - normal - abnormal;
        }

        item.put("task_estimated_time", i * 60 * 5);
        item.put("description", "");
        items.add(item);
        xmlBaseModel.setItems(items);

        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        Map<String, List<XMLBaseModel>> map = new HashMap<>();
        map.put("list", list);
        Result re = null;
        try {
            log.info("信息上报：-" + map);
            re = Constant.otherServer(map, Constant.TCP_URL);//江苏要求
        } catch (Exception e) {
            log.info("上报出错" + e.getMessage());
        }
        return re;
    }

    @Transactional(rollbackFor = Exception.class)
    public int upSystemCtrl(XMLBaseModel xmlBaseModel) throws Exception{
        String com = xmlBaseModel.getCommand();
        String taskId = xmlBaseModel.getCode();
        TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        if (tCruiseTask == null) {
            return -1;
        }
        if ("1".equals(com)) {
            //任务启动
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String uuid = String.valueOf(UUID.randomUUID()).replace("-", "");
            tCruiseTask.setTaskCode(tCruiseTask.getTaskId());
            tCruiseTask.setTaskId(uuid);
            tCruiseTask.setTaskName(tCruiseTask.getTaskName() + "-站端-" + simpleDateFormat.format(new Date()));
            tCruiseTaskDao.insert(tCruiseTask);
            log.info("--站端启动--" + tCruiseTask);
            //立即执行
            try {
                //模板图片路径
                Map<String, Object> mapForPicModelPath = redisTemplate.opsForHash().entries("t_sys_param:picModelPath");
                picModelPath = (String) mapForPicModelPath.get("content");
                //等待相机转到预置位时间
                Map<String, Object> mapForWaitTime = redisTemplate.opsForHash().entries("t_sys_param:waitTime");
                waitTime = Long.valueOf((String) mapForWaitTime.get("content"));
                //任务超期时间
                Map<String, Object> mapForTaskAreTime = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
                tasksAreTime = Float.valueOf((String) mapForTaskAreTime.get("content"));
                RunAtNowTask runAtNowTask = new RunAtNowTask(tCruiseTask, waitTime, picModelPath, redisTemplate,
                        tCruisePointInstanceDao, tCameraPresetDao, tCruiseResultDao, tAlgorithmConfDao, tAlgorithmInfoDao, tCruisePlanAttrDao,
                        tCruiseDataResultDao, tCruiseTaskResultDetailDao, tCruiseTaskResultDao, false, tasksAreTime,
                        tRobotInspectionDao, tAlgorithmConfBakDao);
                Thread thread = new Thread(runAtNowTask);
                thread.setDaemon(true);
                thread.start();
            } catch (Exception e) {
                e.getMessage();
            }

        }
        if ("2".equals(com)) {
            //任务暂停
            return this.taskPause(taskId);
        }
        if ("3".equals(com)) {
            //任务继续
            return this.taskGoOn(taskId);
        }
        if ("4".equals(com)) {
            //任务终止
            return this.taskShutDown(taskId);
        }
        return -1;
    }

    @Transactional(rollbackFor = Exception.class)
    public String upSystemIssuedTask(TCruiseTaskAdd tCruiseTaskAdd) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        TCruiseTask tCruiseTask = new TCruiseTask();
        String uuid = String.valueOf(UUID.randomUUID()).replace("-", "");
        tCruiseTask.setTaskId(uuid);
        tCruiseTask.setTaskCode(tCruiseTaskAdd.getTaskId());
        tCruiseTask.setStartTime(tCruiseTaskAdd.getStartTime());
        tCruiseTask.setDateType(tCruiseTaskAdd.getDateType());
        tCruiseTask.setAreaId(tCruiseTaskAdd.getAreaId());
        tCruiseTask.setIfRun(tCruiseTaskAdd.getIfRun());
        tCruiseTask.setPlanId(tCruiseTaskAdd.getPlanId());
        tCruiseTask.setRobotId(tCruiseTaskAdd.getRobotId());
        tCruiseTask.setTaskName(tCruiseTaskAdd.getTaskName() + simpleDateFormat.format(new Date()));
        tCruiseTask.setTaskLevel(tCruiseTaskAdd.getTaskLevel());
        //TCruisePlanCount tCruisePlanCount = tCruisePlanDao.selectByPrimaryId(tCruiseTaskAdd.getPlanId());
        tCruiseTask.setTaskType(tCruiseTaskAdd.getTaskType());
        tCruiseTask.setType(tCruiseTaskAdd.getType());

        String[] l = tCruiseTaskAdd.getDeviceList().split(",");
        List<Long> instanceList = new ArrayList<>();
        for (String item : l) {
            instanceList.add(Long.valueOf(item));
        }

        List<TCruisePointInstance> tCruisePointInstanceList = this.tCruiseTaskAttrDao.batchSelect(instanceList);
        List<TCruiseTaskAttr> tCruiseTaskAttrList = new ArrayList<>();
        for (TCruisePointInstance tCruisePointInstance : tCruisePointInstanceList) {
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
        if (tCruiseTask.getDateType() == null) {
            if (tCruiseTask.getIfRun() == 173) {
                //立即执行
                try {
                    //模板图片路径
                    Map<String, Object> mapForPicModelPath = redisTemplate.opsForHash().entries("t_sys_param:picModelPath");
                    picModelPath = (String) mapForPicModelPath.get("content");
                    //等待相机转到预置位时间
                    Map<String, Object> mapForWaitTime = redisTemplate.opsForHash().entries("t_sys_param:waitTime");
                    waitTime = Long.valueOf((String) mapForWaitTime.get("content"));
                    //任务超期时间
                    Map<String, Object> mapForTaskAreTime = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
                    tasksAreTime = Float.valueOf((String) mapForTaskAreTime.get("content"));
                    RunAtNowTask runAtNowTask = new RunAtNowTask(tCruiseTask, waitTime, picModelPath, redisTemplate,
                            tCruisePointInstanceDao, tCameraPresetDao, tCruiseResultDao, tAlgorithmConfDao, tAlgorithmInfoDao, tCruisePlanAttrDao,
                            tCruiseDataResultDao, tCruiseTaskResultDetailDao, tCruiseTaskResultDao, false, tasksAreTime,
                            tRobotInspectionDao, tAlgorithmConfBakDao);
                    Thread thread = new Thread(runAtNowTask);
                    thread.setDaemon(true);
                    thread.start();
                } catch (Exception e) {
                    e.getMessage();
                }
            } else {
                //定时
                quartzTask.setStartTime(tCruiseTask.getStartTime());
                //quartzTask.setStartTime(new Date());
                JobManager jobManager = new JobManager();
                try {
                    jobManager.addCruiseTaskJobAtTime(quartzTask, tCruiseTask.getTaskId());
                } catch (Exception e) {
                    e.getMessage();
                }
            }
        } else {
            //周期 0 */10 * * * ?
            quartzTask.setCronExpression(tCruiseTask.getDateType());
            //quartzTask.setCronExpression("0 */1 * * * ?");
            log.info("quartzTask: " + quartzTask.getCronExpression());
            JobManager jobManager = new JobManager();
            try {
                jobManager.addCruiseTaskJob(quartzTask, tCruiseTask.getTaskId());
            } catch (Exception e) {
                e.getMessage();
            }
        }

        //任务状态上报站端
        sendTaskStateToUp(tCruiseTask, 5);
        return uuid;
    }


    @Transactional(rollbackFor = Exception.class)
    public int executeDifferentiateTasks(List<String> images) {
        //任务封装
        List<Analysis> analysisList=new ArrayList<>();
        String taskId=RandomStringUtils.randomAlphanumeric(12);
        for (String imageUrl : images) {
            Analysis analysis = new Analysis();
            analysis.setTaskId(taskId);
            analysis.setAnalyseType("11");
            analysis.setPicPath(imageUrl);
            if (imageUrl.equals(images.get(0))) {//Normal
                analysis.setInstanceId(Long.valueOf(0));
            } else {                             //different
                analysis.setInstanceId(Long.valueOf(RandomStringUtils.randomNumeric(10)));
            }
            analysisList.add(analysis);
        }

        log.info("List:"+analysisList);
        //任务插库

        TCruiseTask tCruiseTask=new TCruiseTask();
        tCruiseTask.setTaskId(taskId);
        tCruiseTask.setTaskName("图像判别-"+taskId);
        tCruiseTask.setStartTime(new Date());
        int status=tCruiseTaskDao.insert(tCruiseTask);

        // TODO: 2021/2/6 联调时放开 任务下发请求 
//        //任务下发请求
//        Map<String,List<Analysis>> listMap=new HashMap<>();
//        listMap.put("list",analysisList);
//        defect(listMap);
        //返回自定义结果
        return status;
    }
    private void defect(Map<String, List<Analysis>> analysisMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(DEFECT_URL, analysisMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}

