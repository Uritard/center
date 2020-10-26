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
    private TCameraPresetDao tCameraPresetDao;
    @Autowired
    private TCruiseResultDao tCruiseResultDao;
    @Autowired
    private TAlgorithmConfDao tAlgorithmConfDao;
    @Autowired
    private TAlgorithmInfoDao tAlgorithmInfoDao;
    //相机抓图
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePicture?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}&cameraId={cameraId}";
    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    //缺陷接口
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";
    //模板图片路径
    @Value("${picModelPath.dir}")
    private String picModelPath;
    //等待相机转到预置位时间
    @Value("${waitTime}")
    private Long waitTime;

    private Logger log = LoggerFactory.getLogger(TCruiseTaskService.class);

    //专为立即任务服务的方法
    public void runAtNow(TCruiseTask tCruiseTask) {
        try {
            //判断任务是否需要执行
            String taskId = tCruiseTask.getTaskId();
            SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM
            String taskDate = simpleDateFormat.format(tCruiseTask.getStartTime());
            Date date = null;
            try {
                date = simpleDateFormat.parse(taskDate);
            } catch (Exception e) { e.getMessage(); }
            List<TCruiseTaskDel> tCruiseTaskDelList =tCruiseTaskDelDao.select(taskId,date,null);

            if(tCruiseTaskDelList != null && tCruiseTaskDelList.size()>0){
                //不需要执行任务
                log.info(taskDate+"此时间任务不需要执行");
            }else {

                //Thread.sleep(10000);
                Map<String,Object> jasonMap=new HashMap<>();
                jasonMap.put("type","newTask");
                jasonMap.put("taskId",taskId);
                String json= JSON.toJSONString(jasonMap);
                log.info("发送给前端的消息：   "+json);
                WebSocketServer.sendMsg(json);
                log.info(taskDate+"需要执行的任务");
                log.info("开始进行任务" +new Date());
                //tCruiseTask.setTaskId(String.valueOf(UUID.randomUUID()).replace("-", ""));
                String uuid = String.valueOf(UUID.randomUUID()).replace("-", "");//任务结果uuid
                //TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);//获取任务
                List<Long> instanceIdList = tCruiseTaskAttrDao.selectInstanceId(taskId);//获取此任务下的巡检点数量
                Integer taskCount = instanceIdList.size();
                List<TCruisePointInstance> instancesList = tCruisePointInstanceDao.selectForTask(instanceIdList);//巡检点
                //开始任务
                TCruiseResult tCruiseResult = new TCruiseResult();
                tCruiseResult.setTaskResultId(uuid);
                tCruiseResult.setTaskId(taskId);
                tCruiseResult.setAreaId(tCruiseTask.getAreaId());
                tCruiseResult.setCType(tCruiseTask.getType());
                tCruiseResult.setCState(239);//正在执行
                tCruiseResult.setTaskCount(taskCount);
                tCruiseResult.setTaskWait(taskCount);
                tCruiseResult.setCreateTime(date);
                tCruiseResult.setExecuteTime(simpleDateFormat.parse(simpleDateFormat.format(new Date())));
                tCruiseResultDao.insert(tCruiseResult);//插入一条任务结果
                log.info("开始巡检"+new Date());
                for (TCruisePointInstance item : instancesList) {
                    TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                    tCruiseTaskResultDetail.setCruiseResultId(uuid+item.getInstanceId().toString());
                    tCruiseTaskResultDetail.setTaskResultId(uuid);
                    tCruiseTaskResultDetail.setDeviceId(item.getDeviceId());
                    tCruiseTaskResultDetail.setInstanceId(item.getInstanceId());
                    tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(simpleDateFormat.format(new Date())));
                    tCruiseTaskResultDetail.setCruiseStatus(252);

                    TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                    tCruiseDataResult.setCruiseResultId(uuid+item.getInstanceId().toString());
                    tCruiseDataResult.setCruiseId(item.getInstanceId());
                    //一次循环 一个巡检点
                    if (228 == item.getCruiseType()) {//todo 机器人
                    }
                    if (229 == item.getCruiseType()) {
                        log.info("巡检点开始巡检"+new Date());
                        tCruiseDataResult.setCruiseType(229);
                        //视频
                        TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(item.getCruiseId());
                        //1.转到预置位
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("presetId", item.getCruiseId());
                        map.put("cameraId", tCameraPreset.getCameraId());
                        log.info(map.toString());
                        move(map);
                        Thread.sleep(waitTime);//等待摄像头转到预置位
                        //2.抓图
                        HashMap<String, Object> map2 = new HashMap<>();
                        map2.put("cameraId", tCameraPreset.getCameraId());
                        String picUrl = picture(map2);
                        //String picUrl = "home/yjh/iot-picture/model-picture/Template/Infrared/2AFE48DF21F64F78AB57396F6DCCAC06/bigi_0.jpg";
                        if(picUrl == null){
                            //抓图失败 任务失败
                            tCruiseTaskResultDetail.setCruiseStatus(254);
                            tCruiseDataResult.setState(250);
                        }else {
                            TAlgorithmConf tAlgorithmConf = tAlgorithmConfDao.selectByPrimaryId(item.getCruiseId());
                            if(tAlgorithmConf != null){//摄像头配置了算法
                                //抓图成功 算法分析
                                Analysis analysis = new Analysis();
                                analysis.setTaskId(taskId);
                                analysis.setInstanceId(item.getInstanceId());
                                analysis.setPicPath(picUrl);
                                TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoDao.selectByPrimaryId(tAlgorithmConf.getAlgorithmId());
                                analysis.setAnalyseType(tAlgorithmInfo.getAnalyseType());
                                analysis.setPicModelPath(picModelPath);//模板图片暂时没有
                                List<Analysis> analysisList = new ArrayList<>();
                                analysisList.add(analysis);
                                Map<String, List<Analysis>> analysisMap  = new HashMap<>();
                                analysisMap.put("list",analysisList);
                                if(tAlgorithmInfo.getIsAi() == 1){//0-算法 1-缺陷
                                    analysis(analysisMap);
                                }else {
                                    defect(analysisMap);
                                }
                            }
                            tCruiseDataResult.setPicpath(picUrl);

                        }
                    }
                    if (230 == item.getCruiseType()) {//todo 红外
                    }
                    if (231 == item.getCruiseType()) {//todo 在线监控
                    }
                    if (232 == item.getCruiseType()) {//todo scala
                    }
                    tCruiseTaskResultDetail.setEndTime(simpleDateFormat.parse(simpleDateFormat.format(new Date())));
                    //resultDetailList.add(tCruiseTaskResultDetail);
                    Map map = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                    //String str = "t_cruise_task_result:"+tCruiseTaskResultDetail.getCruiseResultId();
                    String str = "t_cruise_task_result:"+taskId + item.getInstanceId();
                    //dataResultList.add(tCruiseDataResult);
                    Map map2 = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                    map.putAll(map2);
                    map.put("taskId",taskId);
                    map.put("startTime",simpleDateFormat.format(date));
                    map.put("if_run",tCruiseTask.getIfRun().toString());
                    redisTemplate.opsForHash().putAll(str, map);
                }
                log.info("完成任务执行"+new Date());
            }
        } catch (Exception e) {
            log.error("定时任务异常" + e);
        }


    }
    //相机抓图
    private  String picture(HashMap map) {
        String url = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                Result re = serviceRestTemplate.getForObject(PICTURE_URL, Result.class,map);
                JSONObject json = (JSONObject) JSON.toJSON(re.getData());
                url = (String) json.get("urlPath");
            }
        } catch (Exception e) {

            log.error(e.getMessage(), e);
        }
        return url;
    }
    //相机转到预置位
    private  void move(HashMap<String,Object> map) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.getForObject(MOVE_URL, String.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
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
    //缺陷分析
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
        this.tCruiseTaskDao.insert(tCruiseTask);
        this.tCruiseTaskAttrDao.batchInsert(tCruiseTaskAttrList);
        //开启定时任务
        Constant.taskId = tCruiseTask.getTaskId();
        QuartzTask quartzTask = new QuartzTask();
        quartzTask.setJobName(tCruiseTask.getTaskName());
        quartzTask.setJobGroup("qh111");
        if(tCruiseTask.getDateType()== null){
            if(tCruiseTask.getIfRun() == 173){
                //立即执行
                try {
                    runAtNow(tCruiseTask);
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
            //周期
            quartzTask.setCronExpression(tCruiseTask.getDateType());
            log.info("quartzTask: "+quartzTask.getCronExpression());
            JobManager jobManager = new JobManager();
            try {
                jobManager.addCruiseTaskJob(quartzTask, tCruiseTask.getTaskId());
            } catch (Exception e) { e.getMessage(); }
        }

        return 1;
    }

    @Logs(title = "删除", code = "module")
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
                return tCruiseTaskDelDao.insert(tCruiseTaskDel);
            }else {
                //删除整个周期任务
                return  1;
            }

        } else {
            if(Objects.nonNull(tCruiseTask.getIfRun()) && tCruiseTask.getIfRun()==174){
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
        //删除周期的任务

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
                                taskCountMap.put("taskState", "任务未开始");
                            } else { taskCountMap.put("taskState", tCruiseTaskCount.getTaskState()); }
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
                            taskCountMap.put("taskState", "任务未开始");
                        } else { taskCountMap.put("taskState", tCruiseTaskCount.getTaskState()); }
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
                    taskCountMap.put("taskState", "任务未开始");
                } else { taskCountMap.put("taskState", tCruiseTaskCount.getTaskState()); }
                if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                    taskCountMap.put("taskStatus", "-1");
                } else { taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus()); }

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

