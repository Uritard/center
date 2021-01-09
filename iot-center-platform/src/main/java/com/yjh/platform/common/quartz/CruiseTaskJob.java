package com.yjh.platform.common.quartz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.device.dao.TAlgorithmConfBakDao;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.TAlgorithmConfBak;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TCruisePointInstanceNameDetail;
import com.yjh.platform.module.task.dao.*;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.TAlgorithmConfDao;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.entity.*;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;
import java.text.SimpleDateFormat;
import java.util.*;

@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Configuration
public class CruiseTaskJob extends QuartzJobBean {

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private  TCameraPresetDao tCameraPresetDao;
    @Autowired
    private TCruiseTaskDelDao tCruiseTaskDelDao;
    @Autowired
    private TCruiseResultDao tCruiseResultDao;
    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;
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
    private TCruisePlanAttrDao tCruisePlanAttrDao;
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    @Autowired
    private TAlgorithmConfBakDao tAlgorithmConfBakDao;



    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CruiseTaskJob.class);

    //相机抓图
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePictureForTask?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPresetForTask?presetId={presetId}&cameraId={cameraId}";
    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    //缺陷接口
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";
    //机器人任务接口
    private static final String ROBOT_TASK_URL = "http://iot-center-accessrobot/robot/v1/taskIssued";
    //模板图片路径
    private String picModelPath;
    //等待相机转到预置位时间
    private Long waitTime;
    //任务超期时间
    private Float tasksAreTime;

    /**
     * 巡视任务类
     * @param context
     */
    public void executeInternal(JobExecutionContext context) {
        try {
            //判断任务是否需要执行
            String taskId = context.getMergedJobDataMap().getString("taskId");
            SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM
            String taskDate = simpleDateFormat.format(context.getFireTime());
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
                //模板图片路径
                Map<String,Object> mapForPicModelPath  = redisTemplate.opsForHash().entries("t_sys_param:picModelPath");
                picModelPath = (String) mapForPicModelPath.get("content");
                //等待相机转到预置位时间
                Map<String,Object> mapForWaitTime  = redisTemplate.opsForHash().entries("t_sys_param:waitTime");
                waitTime = Long.valueOf((String) mapForWaitTime.get("content"));
                //任务超期时间
                Map<String,Object> mapForTaskAreTime  = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
                tasksAreTime = Float.valueOf((String) mapForTaskAreTime.get("content"));

                Date taskStart = new Date();
                log.info("开始进行任务" +taskStart);
                //Long taskIsStart = taskStart.getTime();

                TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);//获取任务
                //在任务表里新建一条
                if(tCruiseTask.getIfRun() == 172){
                    List<TCruiseTaskAttr> attrList = tCruiseTaskAttrDao.selectByTaskId(tCruiseTask.getTaskId());
                    String newTaskId = String.valueOf(UUID.randomUUID()).replace("-", "");//任务结果uuid
                    tCruiseTask.setTaskId(newTaskId);
                    tCruiseTask.setDateType(null);
                    tCruiseTask.setStartTime(new Date());
                    tCruiseTask.setCreateTime(new Date());
                    tCruiseTaskDao.insert(tCruiseTask);
                    for (TCruiseTaskAttr item:attrList) {
                        item.setTaskId(newTaskId);
                    }
                    tCruiseTaskAttrDao.batchInsert(attrList);
                }
                Map<String,Object> jasonMap=new HashMap<>();
                jasonMap.put("type","newTask");
                jasonMap.put("taskId",tCruiseTask.getTaskId());
                String json=JSON.toJSONString(jasonMap);
                log.info("发送给前端的消息：   "+json);
                WebSocketServer.sendMsg(json);
                log.info(taskDate+"需要执行的任务");
                //tCruiseTask.setTaskId(String.valueOf(UUID.randomUUID()).replace("-", ""));
                String uuid = String.valueOf(UUID.randomUUID()).replace("-", "");//任务结果uuid

                List<Long> instanceIdList = tCruisePlanAttrDao.selectByPlanId(tCruiseTask.getPlanId());//获取此任务下的巡检点数量
                Integer taskCount = instanceIdList.size();
                List<TCruisePointInstanceNameDetail> instancesList = tCruisePointInstanceDao.selectForTask(instanceIdList);//巡检点
                //开始任务
                TCruiseResult tCruiseResult = new TCruiseResult();
                tCruiseResult.setTaskResultId(uuid);
                tCruiseResult.setTaskId(tCruiseTask.getTaskId());
                tCruiseResult.setAreaId(tCruiseTask.getAreaId());
                tCruiseResult.setCType(tCruiseTask.getType());
                tCruiseResult.setCState(239);//正在执行
                tCruiseResult.setTaskName(tCruiseTask.getTaskName());
                tCruiseResult.setTaskCount(taskCount);
                tCruiseResult.setTaskWait(taskCount);
                tCruiseResult.setCreateTime(date);
                tCruiseResult.setExecuteTime(simpleDateFormat.parse(simpleDateFormat.format(new Date())));
                tCruiseResultDao.insert(tCruiseResult);//插入一条任务结果

                //任务状态
                TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
                tCruiseTaskResult.setTaskResultId(uuid);
                tCruiseTaskResult.setTaskId(tCruiseTask.getTaskId());
                //tCruiseTaskResult.setTaskStatus(239);
                tCruiseTaskResult.setRunExecute(tCruiseTask.getIfRun().toString());
                //tCruiseTaskResultDao.insert(tCruiseTaskResult);

                Map<String,String> mapForAbnormal = new HashMap<>();
                mapForAbnormal.put("all",taskCount.toString());
                mapForAbnormal.put("abnormal","0");
                mapForAbnormal.put("normal","0");
                mapForAbnormal.put("taskStart",simpleDateFormat.format(taskStart));
                mapForAbnormal.put("overDay",tasksAreTime.toString());
                String strForCountAbnormal = "countForAbnormal:"+tCruiseTask.getTaskId();
                redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);

                Integer taskAbnormal = 0;//异常数量
                Integer taskNormal = 0;//正常
                List<String> analysisInstanceList = new ArrayList<>();

                try {
                    QuartzTask quartzTaskForAre = new QuartzTask();
                    quartzTaskForAre.setJobName("检查"+tCruiseTask.getTaskName());
                    quartzTaskForAre.setJobGroup("jiancha");
                    SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    //String strForCountAbnormal = "countForAbnormal:"+tCruiseTask.getTaskId();
                    Map<String,String> mapForGetTaskAreTime  = redisTemplate.opsForHash().entries(strForCountAbnormal);
                    Date taskStartTime = sd.parse(mapForGetTaskAreTime.get("taskStart"));
                    Long taskStartTimes = taskStartTime.getTime();
                    //任务超期时间
                    //Map<String,Object> mapForTaskAreTime  = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
                    tasksAreTime = Float.valueOf((String) mapForTaskAreTime.get("content"));
                    //Long endTime = taskStartTimes + tasksAreTime*24*60*60*1000;
                    Float temp = tasksAreTime*24F*60F*60F*1000F;
                    Long endTime = taskStartTimes + temp.longValue();
                    String s =sd.format(endTime);
                    quartzTaskForAre.setStartTime(sd.parse(s));
                    JobManager jobManager =new JobManager();
                    jobManager.checkTaskIsOver(quartzTaskForAre, tCruiseTask.getTaskId());
                    log.info("检查任务超期任务创建成功");
                } catch (Exception e) {
                    log.info("检查任务超期任务创建失败"+e); }

                //找出机器人做任务的巡检点
                List<Long> robotCruiseList = new ArrayList<>();
                List<Long> instanceList = new ArrayList<>();
                for (TCruisePointInstance item : instancesList) {
                    if (228 == item.getCruiseType()) {
                        robotCruiseList.add(item.getCruiseId());
                        instanceList.add(item.getInstanceId());
                    }
                }
                log.info("robotCruiseList   :" +robotCruiseList);
                if(robotCruiseList.size() != 0){
                    List<String> robotCode = tRobotInspectionDao.selectForRobotTask(robotCruiseList);
                    List<RobotTaskInstanceInfo> robotTaskInfoList = new ArrayList<>();
                    log.info("robotCode   :" +robotCode);
                    for (String item: robotCode){
                        RobotTaskInstanceInfo robotTaskInfo = new RobotTaskInstanceInfo();
                        robotTaskInfo.setCruiseType(tCruiseTask.getType());
                        robotTaskInfo.setTaskId(taskId);
                        robotTaskInfo.setPriority(4);//优先级 暂定4
                        robotTaskInfo.setTaskName(tCruiseTask.getTaskName());
                        List<Long> robotTaskInstanceList = tRobotInspectionDao.selectRobotTaskInstanceId(instanceList,item);
                        robotTaskInfo.setInstanceList(robotTaskInstanceList);
                        robotTaskInfo.setRobotCode(item);
                        robotTaskInfoList.add(robotTaskInfo);
                    }
                    Map<String,List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>();
                    robotTaskInfoMap.put("robotTaskInfoList",robotTaskInfoList);

                    log.info("robotTaskInfoMap   :" +robotTaskInfoMap);
                    //让机器人做任务
                    robotTask(robotTaskInfoMap);
                }

                log.info("开始巡检"+new Date());


                for (TCruisePointInstanceNameDetail item : instancesList) {

                    TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                    tCruiseTaskResultDetail.setCruiseResultId(tCruiseResult.getTaskResultId()+item.getInstanceId().toString());
                    tCruiseTaskResultDetail.setTaskResultId(tCruiseResult.getTaskResultId());
                    tCruiseTaskResultDetail.setDeviceId(item.getDeviceId());
                    tCruiseTaskResultDetail.setInstanceId(item.getInstanceId());
                    tCruiseTaskResultDetail.setDeviceName(item.getDeviceName());
                    tCruiseTaskResultDetail.setInstanceName(item.getInstanceName());
                    //tCruiseTaskResultDetail.setCruiseTime(new Date());
                    String cruiseTime = simpleDateFormat.format(new Date());
                    tCruiseTaskResultDetail.setCruiseStatus(253);

                    TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                    tCruiseDataResult.setCruiseResultId(tCruiseResult.getTaskResultId()+item.getInstanceId().toString());
                    tCruiseDataResult.setCruiseId(item.getInstanceId());
                    tCruiseDataResult.setCruiseType(item.getCruiseType());
                    tCruiseDataResult.setCruiseName(item.getCruiseName());

                    Map tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                    String str = "t_cruise_task_result:"+taskId +":"+ item.getInstanceId();
                    Map tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                    tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                    tCruiseTaskResultDetailMap.put("taskId",taskId);
                    tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                    tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                    tCruiseTaskResultDetailMap.put("device_mete_id",item.getDeviceMeteId().toString());
                    tCruiseTaskResultDetailMap.put("taskName",tCruiseTask.getTaskName());
                    //tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                    //tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);

                    redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);
                    //一次循环 一个巡检点
                    if (229 == item.getCruiseType() || 230 == item.getCruiseType()) {//视频 红外
                        log.info("巡检点开始巡检"+new Date());
                        tCruiseDataResult.setCruiseType(item.getCruiseType());
                        //视频
                        TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(item.getCruiseId());
                        String isOk = "";
                        String urlPath = "";
                        String absPath = "";
                        if(tCameraPreset != null){
                            //1.转到预置位
                            HashMap<String, Object> map = new HashMap<>();
                            map.put("presetId", item.getCruiseId());
                            map.put("cameraId", tCameraPreset.getCameraId());
                            tCruiseTaskResultDetailMap.put("cameraId",tCameraPreset.getCameraId().toString());
                            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);
                            log.info(map.toString());
                            move(map);
                            Thread.sleep(waitTime);//等待摄像头转到预置位
                            //2.抓图
                            HashMap<String, Object> map2 = new HashMap<>();
                            map2.put("cameraId", tCameraPreset.getCameraId());
                            Result re = picture(map2);
                            if(re == null){
                                isOk = "";
                            }else{
                                JSONObject jsonForRe = (JSONObject) JSON.toJSON(re.getData());
                                //todo 对于相机的返回错误分析  任务异常终止/超期
                                urlPath = (String) jsonForRe.get("urlPath");
                                absPath = (String) jsonForRe.get("absPath");
                                isOk = re.getMessage();
                            }
                        }
                        if( !"success".equals(isOk)){
                            //抓图失败 任务失败
                            taskAbnormal = taskAbnormal+1;
                            tCruiseResult = tCruiseResultDao.selectByPrimaryId(uuid);
                            Integer taskWait = tCruiseResult.getTaskWait()-1;
                            if(taskWait == 0 ){
                                tCruiseResult.setCState(240);
                            }
                            tCruiseResult.setTaskWait(taskWait);
                            tCruiseResultDao.update(tCruiseResult);
                            tCruiseDataResult.setCruiseResult(247);
                            tCruiseDataResult.setCruiseAbnormal(248);
                            tCruiseDataResult.setResultNum("抓图失败");
                            tCruiseDataResultDao.insert(tCruiseDataResult);
                            tCruiseTaskResultDetail.setCruiseStatus(254);
                            tCruiseTaskResultDetail.setEndTime(new Date());

                            tCruiseTaskResultDetail.setCruiseTime(date);
                            tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);

                             tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                             str = "t_cruise_task_result:"+tCruiseTask.getTaskId() +":"+ item.getInstanceId();
                             tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                            tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                            tCruiseTaskResultDetailMap.put("taskId",tCruiseTask.getTaskId());
                            tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                            tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                            tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                            tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);

                            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);
                            // webSocket通知前端调用巡视监控的接口
                            Map<String,Object> jasonMapOnFinished=new HashMap<>();
                            jasonMapOnFinished.put("type","finishedOneInstance");
                            jasonMapOnFinished.put("taskId",tCruiseTask.getTaskId());
                            String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
                            log.info("发送给前端的消息："+jsonMessage);
                            WebSocketServer.sendMsg(jsonMessage);


                        }else {
                            //todo  现在是测点配置了算法 从测点寻找算法id
                            //TAlgorithmConfBak tAlgorithmConfBak = tAlgorithmConfBakDao.selectByPrimaryId(item.getDeviceMeteId());
                            //TAlgorithmConf tAlgorithmConf = tAlgorithmConfDao.selectByPrimaryId(item.getCruiseId());
                            TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoDao.selectByAnalyseType(item.getAnalyseType());
                            if(tAlgorithmInfo != null){//配置了算法
                                tCruiseDataResult.setPicpath(urlPath);
                                tCruiseDataResult.setOrigpic(absPath);

                                tCruiseTaskResultDetail.setCruiseStatus(253);
                                 tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                                 str = "t_cruise_task_result:"+tCruiseTask.getTaskId() +":"+ item.getInstanceId();

                                 tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                                tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                                tCruiseTaskResultDetailMap.put("taskId",tCruiseTask.getTaskId());
                                tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                                tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                                tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                                tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);
                                //log.info("tCruiseTaskResultDetailMap" +tCruiseTaskResultDetailMap);
                                //log.info("tCruiseDataResultMap" +tCruiseDataResultMap);
                                redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);
                                //抓图成功 算法分析
                                if(redisTemplate.hasKey("analysisList:"+taskId)) {
                                    //System.out.println("---------> true");
                                    //analysisInstanceList =  (List<String>) redisTemplate.opsForList().g(analysisInstanceList);
                                    //analysisInstanceList.add(item.getInstanceId().toString());
                                    redisTemplate.opsForList().leftPush("analysisList:"+taskId,item.getInstanceId().toString());
                                } else {
                                    analysisInstanceList.add(item.getInstanceId().toString());
                                    redisTemplate.opsForList().leftPushAll("analysisList:"+taskId,analysisInstanceList);
                                }
                                Analysis analysis = new Analysis();
                                analysis.setTaskId(tCruiseTask.getTaskId());
                                analysis.setInstanceId(item.getInstanceId());
                                analysis.setPicPath(absPath);
                                //todo
                                //TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoDao.selectByPrimaryId(tAlgorithmConfBak.getAlgorithmId());
                                //TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoDao.selectByPrimaryId(tAlgorithmConf.getAlgorithmId());
                                analysis.setAnalyseType(tAlgorithmInfo.getAnalyseType());
                                analysis.setPicModelPath(picModelPath+"/"+item.getCruiseId());
                                analysis.setIsAi(tAlgorithmInfo.getIsAi());
                                tCruiseDataResult.setPicpath(urlPath);
                                tCruiseDataResult.setOrigpic(absPath);
                                List<Analysis> analysisList = new ArrayList<>();
                                analysisList.add(analysis);
                                Map<String, List<Analysis>> analysisMap  = new HashMap<>();
                                analysisMap.put("list",analysisList);
                                log.info("算法信息：    "+analysisMap);
                                if(tAlgorithmInfo.getIsAi() == 1){//0-缺陷 1-表记
                                    analysis(analysisMap);
                                }else {
                                    defect(analysisMap);
                                }
                                // webSocket通知前端调用巡视监控的接口
                                Map<String,Object> jasonMapOnFinished=new HashMap<>();
                                jasonMapOnFinished.put("type","finishedOneInstance");
                                jasonMapOnFinished.put("taskId",tCruiseTask.getTaskId());
                                String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
                                log.info("发送给前端的消息："+jsonMessage);
                                WebSocketServer.sendMsg(jsonMessage);
                            }else {
                                taskNormal = taskNormal+1;
                                tCruiseResult = tCruiseResultDao.selectByPrimaryId(uuid);
                                Integer taskWait = tCruiseResult.getTaskWait()-1;
                                if(taskWait == 0 ){
                                    tCruiseResult.setCState(240);
                                }
                                tCruiseResult.setTaskWait(taskWait);
                                tCruiseResultDao.update(tCruiseResult);

                                tCruiseDataResult.setPicpath(urlPath);
                                tCruiseDataResult.setOrigpic(absPath);
                                tCruiseDataResult.setCruiseResult(247);
                                tCruiseDataResult.setCruiseAbnormal(248);
                                tCruiseDataResult.setResultNum("已拍照");
                                tCruiseDataResult.setEvaluationState(257);
                                tCruiseDataResult.setIsWarn(0);
                                tCruiseDataResultDao.insert(tCruiseDataResult);
                                tCruiseTaskResultDetail.setCruiseStatus(252);
                                tCruiseTaskResultDetail.setEndTime(new Date());
                                tCruiseTaskResultDetail.setCruiseTime(date);
                                tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);

                                 tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                                 str = "t_cruise_task_result:"+tCruiseTask.getTaskId() +":"+ item.getInstanceId();
                                 tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                                tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                                tCruiseTaskResultDetailMap.put("taskId",tCruiseTask.getTaskId());
                                tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                                tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                                tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                                tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);

                                redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);

                                // webSocket通知前端调用巡视监控的接口
                                Map<String,Object> jasonMapOnFinished=new HashMap<>();
                                jasonMapOnFinished.put("type","finishedOneInstance");
                                jasonMapOnFinished.put("taskId",tCruiseTask.getTaskId());
                                String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
                                log.info("发送给前端的消息："+jsonMessage);
                                WebSocketServer.sendMsg(jsonMessage);

                            }


                        }
                    }
                    if (231 == item.getCruiseType()) {//todo 在线监控
                    }
                    if (232 == item.getCruiseType()) {//todo scala
                    }

                    //检测任务是否暂停  或者任务是否超期
                    Map<String,String> mapForGet  = redisTemplate.opsForHash().entries(strForCountAbnormal);
                    TCruiseResult tCruiseResultIsPause = tCruiseResultDao.selectForTaskId(tCruiseTask.getTaskId());
                    Long taskEndTime = new Date().getTime();

                    if(tCruiseResultIsPause.getCState() != 239 && tCruiseResultIsPause.getCState() != 240){
                        if(tCruiseResultIsPause.getCState() == 242){
                            Map<String,Object> jsonMap=new HashMap<>();
                            jsonMap.put("type","newTask");
                            jsonMap.put("taskId","");
                            String jsonForShut=JSON.toJSONString(jsonMap);
                            log.info("任务终止的消息：   "+jsonForShut);
                            WebSocketServer.sendMsg(jsonForShut);
                        }
                        Integer abnormal = Integer.valueOf(mapForGet.get("abnormal")) + taskAbnormal;
                        Integer normal = Integer.valueOf(mapForGet.get("normal")) +taskNormal;
                        Integer all = Integer.valueOf(mapForGet.get("all"));
                        int re = abnormal+normal;
                        if(re == all){
                            //所有点都做完了
                            tCruiseTaskResult.setTaskAbnormal(taskAbnormal);
                            tCruiseTaskResult.setCruiseTaskTime(simpleDateFormat.parse(mapForGet.get("taskStart")));
                            tCruiseTaskResult.setTaskStatus(240);
                            tCruiseTaskResult.setTaskName(tCruiseTask.getTaskName());
                            if(abnormal != 0){
                                tCruiseTaskResult.setCruiseResult(247);
                            }else {
                                tCruiseTaskResult.setCruiseResult(246);
                            }
                            tCruiseTaskResultDao.insert(tCruiseTaskResult);

                            Map<String, Object> jsonForLastMap = new HashMap<>();
                            jsonForLastMap.put("type", "lastOneInstance");
                            jsonForLastMap.put("taskId", taskId);
                            String jsonForLast = JSON.toJSONString(jsonForLastMap);
                            log.info("发送给前端的消息：" + jsonForLast);
                            WebSocketServer.sendMsg(jsonForLast);

                            Thread.sleep(15000);
                            tCruiseResult.setCState(240);
                            tCruiseResultDao.update(tCruiseResult);
                        }
                        mapForAbnormal.put("abnormal",abnormal.toString());
                        mapForAbnormal.put("normal",normal.toString());
                        redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);

                        //任务结束生成结果，
                        Analysis analysis = new Analysis();
                        analysis.setTaskId(tCruiseTask.getTaskId());
                        analysis.setInstanceId(-1L);
                        //analysis.setPicPath(picUrl);
                        //TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoDao.selectByPrimaryId(tAlgorithmConf.getAlgorithmId());
                        //analysis.setAnalyseType(tAlgorithmInfo.getAnalyseType());
                        //analysis.setPicModelPath(picModelPath);//模板图片暂时没有
                        List<Analysis> analysisList = new ArrayList<>();
                        analysisList.add(analysis);
                        Map<String, List<Analysis>> analysisMap  = new HashMap<>();
                        analysisMap.put("list",analysisList);
                        log.info("算法信息：    "+analysisMap);
                        analysis(analysisMap);
                        log.info("任务停止执行"+tCruiseTask.getTaskId());
                        return;
                    }
                }
                //计算所有的异常巡检点
                Map<String,String> mapForGet  = redisTemplate.opsForHash().entries(strForCountAbnormal);
                Integer abnormal = Integer.valueOf(mapForGet.get("abnormal")) + taskAbnormal;
                Integer normal = Integer.valueOf(mapForGet.get("normal")) +taskNormal;
                Integer all = Integer.valueOf(mapForGet.get("all"));
                int re = abnormal+normal;
                if(re == all){
                    //所有点都做完了
                    mapForAbnormal.put("abnormal",abnormal.toString());
                    mapForAbnormal.put("normal",normal.toString());
                    redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);

                    tCruiseTaskResult.setTaskAbnormal(abnormal);
                    tCruiseTaskResult.setCruiseTaskTime(simpleDateFormat.parse(mapForGet.get("taskStart")));
                    tCruiseTaskResult.setTaskStatus(240);
                    tCruiseTaskResult.setTaskName(tCruiseTask.getTaskName());
                    if(abnormal != 0){
                        tCruiseTaskResult.setCruiseResult(247);
                    }else {
                        tCruiseTaskResult.setCruiseResult(246);
                    }
                    tCruiseTaskResultDao.insert(tCruiseTaskResult);

                    Thread.sleep(15000);
                    tCruiseResult.setCState(240);
                    tCruiseResultDao.update(tCruiseResult);

                }else {
                    mapForAbnormal.put("abnormal",abnormal.toString());
                    mapForAbnormal.put("normal",normal.toString());
                    redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);
                }
                //任务结束生成结果，
                Analysis analysis = new Analysis();
                analysis.setTaskId(tCruiseTask.getTaskId());
                analysis.setInstanceId(-1L);
                //analysis.setPicPath(picUrl);
                //TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoDao.selectByPrimaryId(tAlgorithmConf.getAlgorithmId());
                //analysis.setAnalyseType(tAlgorithmInfo.getAnalyseType());
                //analysis.setPicModelPath(picModelPath);//模板图片暂时没有
                List<Analysis> analysisList = new ArrayList<>();
                analysisList.add(analysis);
                Map<String, List<Analysis>> analysisMap  = new HashMap<>();
                analysisMap.put("list",analysisList);
                log.info("算法信息：    "+analysisMap);
                analysis(analysisMap);
                //todo 结果的状态未作处理
                //tCruiseResult.setCState(240);
                //tCruiseResult.setTaskWait(0);
                //tCruiseResultDao.update(tCruiseResult);
                //tCruiseTaskResult.setTaskStatus(240);
                //tCruiseTaskResult.setTaskAbnormal(taskAbnormasl);
                //if (taskAbnormasl>0){tCruiseTaskResult.setCruiseResult(1);}else{tCruiseTaskResult.setCruiseResult(0);}
                //tCruiseTaskResultDao.update(tCruiseTaskResult);
                log.info("完成任务执行"+new Date());
            }
        } catch (Exception e) {
            log.error("定时任务或周期任务异常: "+e);
            e.printStackTrace();
        }


    }
    //相机抓图
    private static Result picture(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(PICTURE_URL, Result.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
    }
    //相机转到预置位
    private static void move(HashMap<String,Object> map) {
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

    //让机器人做任务
    private void robotTask(Map<String,List<RobotTaskInstanceInfo>> robotTaskInfoMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(ROBOT_TASK_URL, robotTaskInfoMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

}
