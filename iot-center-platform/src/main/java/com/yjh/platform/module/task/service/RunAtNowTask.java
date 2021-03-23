package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.quartz.JobManager;
import com.yjh.platform.common.quartz.QuartzTask;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.device.dao.TAlgorithmConfBakDao;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.*;
import com.yjh.platform.module.task.dao.*;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.TAlgorithmConfDao;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import com.yjh.platform.module.user.dao.TSysParamDao;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.entity.TCameraPreset;
import com.yjh.platform.module.user.entity.TSysParam;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lqh
 * @since 2020/10/27
 */
public class RunAtNowTask implements Runnable{


    private RedisTemplate redisTemplate;

    private TCruisePointInstanceDao tCruisePointInstanceDao;

    private TCameraPresetDao tCameraPresetDao;

    private TCruiseResultDao tCruiseResultDao;

    private TAlgorithmConfDao tAlgorithmConfDao;
    private TAlgorithmInfoDao tAlgorithmInfoDao;

    private TCruisePlanAttrDao tCruisePlanAttrDao;
    private TCruiseDataResultDao tCruiseDataResultDao;
    private TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao;
    private TCruiseTaskResultDao tCruiseTaskResultDao;
    private Boolean isGoOn;
    private TRobotInspectionDao tRobotInspectionDao;
    private TAlgorithmConfBakDao tAlgorithmConfBakDao;

    private Logger log = LoggerFactory.getLogger(RunAtNowTask.class);

    //相机抓图
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePictureForTask?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPresetForTask?presetId={presetId}&cameraId={cameraId}";
    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    //缺陷接口
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";
    //机器人任务路径
    private static final String ROBOT_TASK_URL = "http://iot-center-accessrobot/robot/v1/taskIssued";
    //红外相机拍图
    private static final String RED_MOVE_URL = "http://iot-center-accessvideo/camera/v1/givePicFir?presetId={presetId}&cameraId={cameraId}";
    //模板图片路径
    private String picModelPath;
    //等待相机转到预置位时间
    private Long waitTime;
    //任务超期天数
    private Float tasksAreTime;

    private TCruiseTask tCruiseTask;

    public RunAtNowTask(TCruiseTask tCruiseTask,Long waitTime ,String picModelPath,RedisTemplate redisTemplate,TCruisePointInstanceDao tCruisePointInstanceDao,
                        TCameraPresetDao tCameraPresetDao,TCruiseResultDao tCruiseResultDao,TAlgorithmConfDao tAlgorithmConfDao,
                        TAlgorithmInfoDao tAlgorithmInfoDao,TCruisePlanAttrDao tCruisePlanAttrDao,TCruiseDataResultDao tCruiseDataResultDao,
                        TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao,TCruiseTaskResultDao tCruiseTaskResultDao,Boolean isGoOn,Float tasksAreTime,
                        TRobotInspectionDao tRobotInspectionDao,TAlgorithmConfBakDao tAlgorithmConfBakDao) {
        this.tCruiseTask = tCruiseTask;
        this.waitTime = waitTime;
        this.picModelPath = picModelPath;
        this.redisTemplate = redisTemplate;
        this.tCruisePointInstanceDao = tCruisePointInstanceDao;
        this.tCameraPresetDao = tCameraPresetDao;
        this.tCruiseResultDao = tCruiseResultDao;
        this.tAlgorithmConfDao =tAlgorithmConfDao;
        this.tAlgorithmInfoDao =tAlgorithmInfoDao;
        this.tCruisePlanAttrDao = tCruisePlanAttrDao;
        this.tCruiseDataResultDao = tCruiseDataResultDao;
        this.tCruiseTaskResultDetailDao = tCruiseTaskResultDetailDao;
        this.tCruiseTaskResultDao = tCruiseTaskResultDao;
        this.isGoOn = isGoOn;//判断是否是任务重启
        this.tasksAreTime = tasksAreTime;
        this.tRobotInspectionDao = tRobotInspectionDao;
        this.tAlgorithmConfBakDao = tAlgorithmConfBakDao;
    }

    //相机抓图
    private  Result picture(HashMap map) {
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

    //红外相机抓图
    private  Result redPicture(HashMap map) {
        Result re = null;
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                re =  serviceRestTemplate.getForObject(RED_MOVE_URL, Result.class,map);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return re;
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

    @Override
    public void run() {
        try {
            //Thread.sleep(10000);
            Date taskStart = new Date();
            log.info("开始进行任务" +taskStart);
            Long taskIsStart = taskStart.getTime();
            String taskId = tCruiseTask.getTaskId();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM
            String taskDate = simpleDateFormat.format(tCruiseTask.getStartTime());
            Date date = null;
            try {
                date = simpleDateFormat.parse(taskDate);
            } catch (Exception e) { e.getMessage(); }

            //Thread.sleep(10000);
            Map<String,String> jasonMap=new HashMap<>();
            jasonMap.put("type","newTask");
            jasonMap.put("taskId",taskId);
            String json=JSON.toJSONString(jasonMap);
            log.info("发送给前端的消息：   "+json);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
            log.info(taskDate+"需要执行的任务");

            TCruiseDataResultService tCruiseDataResultService  = StaticContextAccessor.getBean(TCruiseDataResultService.class);

            //tCruiseTask.setTaskId(String.valueOf(UUID.randomUUID()).replace("-", ""));
            //String uuid = String.valueOf(UUID.randomUUID()).replace("-", "");//任务结果uuid
            //TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);//获取任务
            List<Long> instanceIdList = tCruisePlanAttrDao.selectByPlanId(tCruiseTask.getPlanId());//获取此任务下的巡检点数量
            if(isGoOn){
                //任务重启
                List<Long> isFinishedInstanceList = tCruiseTaskResultDetailDao.selectInstanceForTaskGoOn(taskId);
                for(Long finished:isFinishedInstanceList){//删除已经做过的点
                    instanceIdList.remove(finished);
                }
            }
            Integer taskCount = instanceIdList.size();
            List<TCruisePointInstanceNameDetail> instancesList = new ArrayList<>();
            if(taskCount != 0){
                 instancesList = tCruisePointInstanceDao.selectForTask(instanceIdList);//巡检点
            }

            //开始任务

            String strForCountAbnormal = "countForAbnormal:"+taskId;
            Map<String,String> mapForAbnormal = new HashMap<>();
            if( !redisTemplate.hasKey(strForCountAbnormal)){//判断任务是否做过 没做->初始化  做了->后续工作直接从redis获取
                mapForAbnormal.put("all",taskCount.toString());
                mapForAbnormal.put("abnormal","0");
                mapForAbnormal.put("normal","0");
                mapForAbnormal.put("taskStart",simpleDateFormat.format(taskStart));
                mapForAbnormal.put("overDay",tasksAreTime.toString());
                redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);
            }
            TCruiseResult tCruiseResult = new TCruiseResult();
            tCruiseResult = tCruiseResultDao.selectForTaskId(taskId);
            if(tCruiseResult == null){//判断任务是否存在
                tCruiseResult= new TCruiseResult();
                String uuid = String.valueOf(UUID.randomUUID()).replace("-", "");
                tCruiseResult.setTaskResultId(uuid);
                tCruiseResult.setTaskId(taskId);
                tCruiseResult.setAreaId(tCruiseTask.getAreaId());
                tCruiseResult.setCType(tCruiseTask.getType());
                tCruiseResult.setCState(239);//正在执行
                tCruiseResult.setTaskName(tCruiseTask.getTaskName());
                tCruiseResult.setTaskCount(taskCount);
                tCruiseResult.setTaskWait(taskCount);
                tCruiseResult.setCreateTime(date);
                tCruiseResult.setExecuteTime(simpleDateFormat.parse(simpleDateFormat.format(new Date())));
                tCruiseResult.setRemark("0");
                tCruiseResultDao.insert(tCruiseResult);//插入一条任务结果
            }

            //任务状态
            TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
            tCruiseTaskResult.setTaskResultId(tCruiseResult.getTaskResultId());
            tCruiseTaskResult.setTaskId(taskId);
            //tCruiseTaskResult.setTaskStatus(239);
            tCruiseTaskResult.setRunExecute(tCruiseTask.getIfRun().toString());
            Integer taskAbnormal = 0;//异常数量
            Integer taskNormal = 0;//正常
            List<String> analysisInstanceList = new ArrayList<>();

            try {
                SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                QuartzTask quartzTaskForAre = new QuartzTask();
                quartzTaskForAre.setJobName("检查"+tCruiseTask.getTaskName()+"-"+sd.format(new Date()));
                quartzTaskForAre.setJobGroup("jiancha");
                //SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                //String strForCountAbnormal = "countForAbnormal:"+tCruiseTask.getTaskId();
                Map<String,String> mapForGet  = redisTemplate.opsForHash().entries(strForCountAbnormal);
                Date taskStartTime = sd.parse(mapForGet.get("taskStart"));
                Long taskStartTimes = taskStartTime.getTime();
                //任务超期时间
                Map<String,Object> mapForTaskAreTime  = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
                tasksAreTime = Float.valueOf((String) mapForTaskAreTime.get("content"));
                //Long endTime = taskStartTimes + tasksAreTime*24*60*60*1000;
                //Float temp = tasksAreTime*24F*60F*60F*1000F;
                Float temp = tasksAreTime*60F*60F*1000F;
                Long endTime = taskStartTimes + temp.longValue();
                String s =sd.format(endTime);
                quartzTaskForAre.setStartTime(sd.parse(s));
                JobManager jobManager =new JobManager();
                jobManager.checkTaskIsOver(quartzTaskForAre, tCruiseTask.getTaskId());
                log.info("检查任务超期任务创建成功"+tCruiseTask.getTaskId());
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
            List<Long> robotTaskInstanceList = new ArrayList<>();
            if(robotCruiseList.size() > 0  && !isGoOn){
                List<String> robotCode = tRobotInspectionDao.selectForRobotTask(robotCruiseList);
                List<RobotTaskInstanceInfo> robotTaskInfoList = new ArrayList<>();
                log.info("robotCode   :" +robotCode);
                for (String item: robotCode){
                    RobotTaskInstanceInfo robotTaskInfo = new RobotTaskInstanceInfo();
                    robotTaskInfo.setCruiseType(tCruiseTask.getType());
                    robotTaskInfo.setTaskId(taskId);
                    robotTaskInfo.setPriority(4);//优先级 暂定4
                    robotTaskInfo.setTaskName(tCruiseTask.getTaskName());
                    robotTaskInstanceList = tRobotInspectionDao.selectRobotTaskInstanceId(instanceList,item);
                    robotTaskInfo.setInstanceList(robotTaskInstanceList);
                    robotTaskInfo.setRobotCode(item);
                    robotTaskInfoList.add(robotTaskInfo);
                }
                Map<String,List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>();
                robotTaskInfoMap.put("robotTaskInfoList",robotTaskInfoList);

                log.info("robotTaskInfoMap   :" +robotTaskInfoMap);
                //让机器人做任务
                robotTask(robotTaskInfoMap);
                //获取机器人运行状态
            }

            if(isGoOn){
                if(robotCruiseList.size() > 0) {
                    List<String> robotCode = tRobotInspectionDao.selectForRobotTask(robotCruiseList);
                    for (String item : robotCode) {
                        robotTaskInstanceList = tRobotInspectionDao.selectRobotTaskInstanceId(instanceList, item);
                        for (Long itemLong : robotTaskInstanceList) {
                            instancesList.remove(itemLong);
                        }
                    }
                }
            }

            log.info("开始巡检"+new Date()+"--"+tCruiseTask.getTaskId());

            List<String> cruiseResultIdList = new ArrayList<>();
            int countForInstance = 0;
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

                Date now = new Date();
                List<Long>  overhaul= tCruisePointInstanceDao.selectTimeIsIn(now);
                if(overhaul != null && overhaul.size()>0){//判断是否检修
                    if(overhaul.contains(item.getDeviceId())){
                        Integer taskWait = tCruiseResult.getTaskWait()-1;
                        if(taskWait == 0 ){
                            tCruiseResult.setCState(240);
                        }
                        taskAbnormal = taskAbnormal+1;
                        tCruiseResult.setTaskWait(taskWait);
                        tCruiseResultDao.update(tCruiseResult);
                        tCruiseDataResult.setCruiseResult(247);
                        tCruiseDataResult.setCruiseAbnormal(410);
                        tCruiseDataResult.setResultNum("设备检修中");
                        tCruiseDataResult.setEvaluationState(257);
                        tCruiseDataResult.setIsWarn(0);
                        tCruiseDataResultDao.insert(tCruiseDataResult);
                        tCruiseTaskResultDetail.setCruiseStatus(255);
                        tCruiseTaskResultDetail.setEndTime(new Date());
                        tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(cruiseTime));
                        tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);
                        cruiseResultIdList.add(tCruiseTaskResultDetail.getCruiseResultId());

                        Map tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                        String str = "t_cruise_task_result:"+taskId +":"+ item.getInstanceId();
                        Map tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                        tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                        tCruiseTaskResultDetailMap.put("taskId",taskId);
                        tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                        tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                        tCruiseTaskResultDetailMap.put("device_mete_id",item.getDeviceMeteId().toString());
                        tCruiseTaskResultDetailMap.put("taskName",tCruiseTask.getTaskName());
                        tCruiseTaskResultDetailMap.put("realCode",item.getRealCode());
                        tCruiseTaskResultDetailMap.put("taskCode",tCruiseTask.getTaskCode());

                        tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                        tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);
                        TCameraPreset tCameraPreset = tCameraPresetDao.selectByPrimaryId(item.getCruiseId());
                        if(item.getCruiseType() != 228){
                            tCruiseTaskResultDetailMap.put("cameraId",item.getCruiseId().toString());
                            tCruiseTaskResultDetailMap.put("robotId","");
                        }else {
                            tCruiseTaskResultDetailMap.put("cameraId","");
                            tCruiseTaskResultDetailMap.put("robotId",item.getRobotId().toString());
                        }
                        redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);
                        continue;
                    }

                }

                Map tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                String str = "t_cruise_task_result:"+taskId +":"+ item.getInstanceId();
                Map tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                tCruiseTaskResultDetailMap.put("taskId",taskId);
                tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                tCruiseTaskResultDetailMap.put("device_mete_id",item.getDeviceMeteId().toString());
                tCruiseTaskResultDetailMap.put("taskName",tCruiseTask.getTaskName());
                tCruiseTaskResultDetailMap.put("realCode",item.getRealCode());
                tCruiseTaskResultDetailMap.put("taskCode",tCruiseTask.getTaskCode());

                //tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                //tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);
                if(item.getCruiseType() != 228){
                    tCruiseTaskResultDetailMap.put("cameraId",item.getCruiseId().toString());
                    tCruiseTaskResultDetailMap.put("robotId","");
                }else {
                    tCruiseTaskResultDetailMap.put("cameraId","");
                    tCruiseTaskResultDetailMap.put("robotId",item.getRobotId().toString());
                }
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
                    String picPath = "";
                    String csvPath = "";
                    String dataPath = "";
                    TStdDeviceMete tStdDevicemete = tAlgorithmInfoDao.selectDeviceMete(item.getDeviceMeteId());
                    if(tCameraPreset != null){
                        //1.转到预置位
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("presetId", item.getCruiseId());
                        map.put("cameraId", tCameraPreset.getCameraId());
                        tCruiseTaskResultDetailMap.put("cameraId",tCameraPreset.getCameraId().toString());
                        redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);
                        log.info(map.toString());
                        Map<String,String> mapForCameraState = redisTemplate.opsForHash().entries("camera_info:"+tCameraPreset.getCameraId());
                        Integer cameraState = Integer.valueOf(mapForCameraState.get("state"));
                        boolean waitFlag = true;
                        if(cameraState == 1){//摄像头在任务中
                            int waitCount = 0;
                            while (cameraState == 1){
                                Thread.sleep(waitTime+1000);
                                log.info("任务："+tCruiseTask.getTaskName()+"在"+simpleDateFormat.format(new Date())+"时已经等待了"+(waitTime+1000)/1000+"秒");
                                Map<String,String> mapForCameraStateForGet = redisTemplate.opsForHash().entries("camera_info:"+tCameraPreset.getCameraId());
                                cameraState = Integer.valueOf(mapForCameraStateForGet.get("state"));
                                waitCount = waitCount +1;
                                if(waitCount == 30){
                                    log.info("任务："+tCruiseTask.getTaskName()+"已经等待了330秒,仍未等待到"+tCameraPreset.getPresetName()+"预置位,摄像机Id"+tCameraPreset.getCameraId()+"退出等待");
                                    waitFlag = false;
                                    break;
                                }
                            }
                        }
                        if(waitFlag){
                            mapForCameraState.put("state","1");
                            redisTemplate.opsForHash().putAll("camera_info:"+tCameraPreset.getCameraId(),mapForCameraState);
                            Result re = null;
                            HashMap<String, Object> map2 = new HashMap<>();
                            map2.put("cameraId", tCameraPreset.getCameraId());
                            if(item.getCruiseType().equals(230)){//红外专属拍照方法
                                re = redPicture(map);
                            }else {
                                move(map);
                                Thread.sleep(waitTime);//等待摄像头转到预置位
                                //2.抓图
                                 re = picture(map2);
                            }
                            mapForCameraState.put("state","0");
                            redisTemplate.opsForHash().putAll("camera_info:"+tCameraPreset.getCameraId(),mapForCameraState);
                            if(re == null){
                                isOk = "";
                            }else{
                                JSONObject jsonForRe = (JSONObject) JSON.toJSON(re.getData());
                                //todo 对于相机的返回错误分析  任务异常终止/超期
                                urlPath = (String) jsonForRe.get("urlPath");
                                absPath = (String) jsonForRe.get("absPath");
                                picPath = (String) jsonForRe.get("picPath");
                                csvPath = (String) jsonForRe.get("csvPath");
                                dataPath = (String) jsonForRe.get("dataPath");
                                isOk = re.getMessage();
                            }

                        }else {
                            isOk ="";
                        }
                    }
                    if( !"success".equals(isOk)){
                        //抓图失败 任务失败
                        taskAbnormal = taskAbnormal+1;
                        tCruiseResult = tCruiseResultDao.selectByPrimaryId(tCruiseResult.getTaskResultId());
                        Integer taskWait = tCruiseResult.getTaskWait()-1;
                        if(taskWait == 0 ){
                            tCruiseResult.setCState(240);
                        }
                        tCruiseResult.setTaskWait(taskWait);
                        tCruiseResultDao.update(tCruiseResult);
                        tCruiseDataResult.setCruiseResult(247);
                        tCruiseDataResult.setCruiseAbnormal(248);
                        tCruiseDataResult.setResultNum("抓图失败");
                        tCruiseDataResult.setEvaluationState(257);
                        tCruiseDataResult.setIsWarn(0);
                        tCruiseDataResultDao.insert(tCruiseDataResult);
                        tCruiseTaskResultDetail.setCruiseStatus(254);
                        tCruiseTaskResultDetail.setEndTime(new Date());
                        tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(cruiseTime));
                        tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);
                        cruiseResultIdList.add(tCruiseTaskResultDetail.getCruiseResultId());

                         tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                         str = "t_cruise_task_result:"+taskId +":"+ item.getInstanceId();
                         tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                        tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                        tCruiseTaskResultDetailMap.put("taskId",taskId);
                        tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                        tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                        tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                        tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);

                        redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);
                        // webSocket通知前端调用巡视监控的接口
                        Map<String,String> jasonMapOnFinished=new HashMap<>();
                        jasonMapOnFinished.put("type","finishedOneInstance");
                        jasonMapOnFinished.put("taskId",taskId);
                        String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
                        log.info("发送给前端的消息："+jsonMessage);
                        Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);

                        {
                            //巡视点结果上报站端
                            XMLBaseModel xmlBaseModel = new XMLBaseModel();
                            List<Map<String,Object>> xmlItems = new ArrayList<>();
                            Map<String,Object> xmlItem = new HashMap<>();
                            xmlBaseModel.setType("61");
                            xmlItem.put("patroldevice_code",item);
                            xmlItem.put("task_name",tCruiseTask.getTaskName());
                            xmlItem.put("task_code",tCruiseTask.getTaskCode());
                            xmlItem.put("device_name",item.getCruiseName());
                            xmlItem.put("device_id",item.getInstanceId());
                            xmlItem.put("material_id",item.getRealCode());
                            xmlItem.put("value","");
                            xmlItem.put("value_unit","");
                            xmlItem.put("unit","");
                            xmlItem.put("time",cruiseTime);
                            //todo
                            xmlItem.put("recognition_type","");
                            xmlItem.put("file_type","2");
                            xmlItem.put("file_path",urlPath);
                            xmlItem.put("rectangle","");
                            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
                            xmlItem.put("task_patrolled_id",taskId+"_"+simpleDateFormat2.format(tCruiseTask.getStartTime()));
                            xmlItem.put("data_type","0x01");
                            xmlItem.put("valid","0");

                            xmlItems.add(xmlItem);
                            xmlBaseModel.setItems(xmlItems);
                            List<XMLBaseModel> list = new ArrayList<>();
                            list.add(xmlBaseModel);
                            Map<String,List<XMLBaseModel>> cruiseResult = new HashMap<>();
                            cruiseResult.put("list",list);
                            log.info("信息上报：-"+cruiseResult);
                            Constant.otherServer(cruiseResult,Constant.TCP_URL);//江苏要求
                        }


                    }else {
                        if(item.getAnalyseType() != null || "on".equals(item.getIsAi()) || "on".equals(item.getIsJudge())){//配置了算法
                            tCruiseDataResult.setPicpath(urlPath);
                            tCruiseDataResult.setOrigpic(absPath);
                            tCruiseTaskResultDetail.setCruiseStatus(253);
                             tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                             str = "t_cruise_task_result:"+taskId +":"+ item.getInstanceId();

                             tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                            tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                            tCruiseTaskResultDetailMap.put("taskId",taskId);
                            tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                            tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                            tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                            tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);
                            //log.info("tCruiseTaskResultDetailMap" +tCruiseTaskResultDetailMap);
                            //log.info("tCruiseDataResultMap" +tCruiseDataResultMap);

                            List<TAlgorithmInfo> tAlgorithmInfoList = tAlgorithmInfoDao.selectByDeviceMeteId(item.getDeviceMeteId());
                            Integer recognitionMode = 0;
                            if(tAlgorithmInfoList != null && tAlgorithmInfoList.size()>0){
                                recognitionMode = 1;
                            }
                            if("on".equals(tStdDevicemete.getIsAi()) || "on".equals(tStdDevicemete.getIsJudge())){
                                if(recognitionMode == 1){
                                    recognitionMode = 0;
                                }else {
                                    recognitionMode = 2;
                                }
                            }
                            tCruiseTaskResultDetailMap.put("recognitionMode",recognitionMode.toString());
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
                            for (TAlgorithmInfo tAlgorithmInfo:tAlgorithmInfoList) {
                                Analysis analysis = new Analysis();
                                analysis.setTaskId(tCruiseTask.getTaskId());
                                analysis.setInstanceId(item.getInstanceId());
                                analysis.setPicPath(absPath);
                                analysis.setAnalyseType(tAlgorithmInfo.getAnalyseType());
                                analysis.setPicModelPath(picModelPath+"/"+item.getCruiseId());
                                analysis.setIsAi(tAlgorithmInfo.getIsAi());
                                List<Analysis> analysisList = new ArrayList<>();
                                analysisList.add(analysis);
                                Map<String, List<Analysis>> analysisMap  = new HashMap<>();
                                analysisMap.put("list",analysisList);
                                log.info("算法信息：    "+analysisMap);
                                if(tAlgorithmInfo.getIsAi() == 1){//0-缺陷 1-表记
                                    if(item.getCruiseType().equals(230)){//红外专属
                                        analysis.setPicPath(picPath);
                                        analysis.setCsvPath(csvPath);
                                        analysis.setDataPath(dataPath);
                                        log.info("算法信息(红外)：    "+analysisMap);
                                    }
                                    analysis(analysisMap);
                                }else {
                                    defect(analysisMap);
                                }
                            }
                            if("on".equals(tStdDevicemete.getIsAi()) || "on".equals(tStdDevicemete.getIsJudge())){
                                Analysis analysis = new Analysis();
                                analysis.setTaskId(tCruiseTask.getTaskId());
                                analysis.setInstanceId(item.getInstanceId());
                                analysis.setPicPath(absPath);
                                if("on".equals(tStdDevicemete.getIsJudge())){
                                    analysis.setAnalyseType("11");
                                }else {
                                    analysis.setAnalyseType("398");
                                }
                                analysis.setPicModelPath(picModelPath+"/"+item.getCruiseId());
                                analysis.setIsAi(0);
                                List<Analysis> analysisList = new ArrayList<>();
                                analysisList.add(analysis);
                                Map<String, List<Analysis>> analysisMap  = new HashMap<>();
                                analysisMap.put("list",analysisList);
                                log.info("算法信息：    "+analysisMap);
                                defect(analysisMap);
                            }
                            tCruiseDataResult.setPicpath(urlPath);
                            tCruiseDataResult.setOrigpic(absPath);
                            // webSocket通知前端调用巡视监控的接口
                            Map<String,String> jasonMapOnFinished=new HashMap<>();
                            jasonMapOnFinished.put("type","finishedOneInstance");
                            jasonMapOnFinished.put("taskId",tCruiseTask.getTaskId());
                            String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
                            log.info("发送给前端的消息："+jsonMessage);
                            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);
                        }else {
                            taskNormal = taskNormal+1;
                            tCruiseResult = tCruiseResultDao.selectByPrimaryId(tCruiseResult.getTaskResultId());
                            Integer taskWait = tCruiseResult.getTaskWait()-1;
                            if(taskWait == 0 ){
                                tCruiseResult.setCState(240);
                            }
                            tCruiseResult.setTaskWait(taskWait);
                            tCruiseResultDao.update(tCruiseResult);

                            tCruiseDataResult.setPicpath(urlPath);
                            tCruiseDataResult.setOrigpic(absPath);
                            tCruiseDataResult.setCruiseResult(246);

                            tCruiseDataResult.setResultNum("已拍照");
                            tCruiseDataResult.setEvaluationState(257);
                            tCruiseDataResult.setIsWarn(0);
                            tCruiseDataResultDao.insert(tCruiseDataResult);
                            tCruiseTaskResultDetail.setCruiseStatus(252);
                            tCruiseTaskResultDetail.setEndTime(new Date());
                            tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(cruiseTime));
                            tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);
                            cruiseResultIdList.add(tCruiseTaskResultDetail.getCruiseResultId());

                             tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                             str = "t_cruise_task_result:"+taskId +":"+ item.getInstanceId();
                             tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                            tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                            tCruiseTaskResultDetailMap.put("taskId",taskId);
                            tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                            tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                            tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                            tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);

                            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);

                            // webSocket通知前端调用巡视监控的接口
                            Map<String,String> jasonMapOnFinished=new HashMap<>();
                            jasonMapOnFinished.put("type","finishedOneInstance");
                            jasonMapOnFinished.put("taskId",taskId);
                            String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
                            log.info("发送给前端的消息："+jsonMessage);
                            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);

                            {
                                //巡视点结果上报站端
                                XMLBaseModel xmlBaseModel = new XMLBaseModel();
                                List<Map<String,Object>> xmlItems = new ArrayList<>();
                                Map<String,Object> xmlItem = new HashMap<>();
                                xmlBaseModel.setType("61");
                                xmlItem.put("patroldevice_code",item);
                                xmlItem.put("task_name",tCruiseTask.getTaskName());
                                xmlItem.put("task_code",tCruiseTask.getTaskCode());
                                xmlItem.put("device_name",item.getCruiseName());
                                xmlItem.put("device_id",item.getInstanceId());
                                xmlItem.put("material_id",item.getRealCode());
                                xmlItem.put("value","");
                                xmlItem.put("value_unit","");
                                xmlItem.put("unit","");
                                xmlItem.put("time",cruiseTime);
                                //todo
                                xmlItem.put("recognition_type","");
                                xmlItem.put("file_type","2");
                                xmlItem.put("file_path",urlPath);
                                xmlItem.put("rectangle","");
                                SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
                                xmlItem.put("task_patrolled_id",taskId+"_"+simpleDateFormat2.format(tCruiseTask.getStartTime()));
                                xmlItem.put("data_type","0x01");
                                xmlItem.put("valid","1");

                                xmlItems.add(xmlItem);
                                xmlBaseModel.setItems(xmlItems);
                                List<XMLBaseModel> list = new ArrayList<>();
                                list.add(xmlBaseModel);
                                Map<String,List<XMLBaseModel>> cruiseResult = new HashMap<>();
                                cruiseResult.put("list",list);
                                log.info("信息上报：-"+cruiseResult);
                                Constant.otherServer(cruiseResult,Constant.TCP_URL);//江苏要求
                            }

                        }


                    }
                    {
                        //专为检测的  4张图片一轮
                        Map<String,String> maoForFour= redisTemplate.opsForHash().entries("t_sys_param:isCheckByFour");
                        Boolean flagForFour = false;
                        if(maoForFour != null  && maoForFour.size()>0){
                            flagForFour = Boolean.valueOf(maoForFour.get("content"));
                        }
                        if(flagForFour){
                            countForInstance = countForInstance +1;
                            if(countForInstance == 4){
                                Map<String,String> maoForSleep= redisTemplate.opsForHash().entries("t_sys_param:checkWaitTimeByFour");
                                Long sleepTime = 30000L;
                                if(maoForSleep != null  && maoForSleep.size()>0){
                                    sleepTime = Long.valueOf(maoForSleep.get("content"))*1000L;
                                }
                                Map<String,String> jasonForPic=new HashMap<>();
                                jasonForPic.put("type","picChange");
                                jasonForPic.put("msg","该换图片了");
                                String picChange=JSON.toJSONString(jasonForPic);
                                log.info("发送给前端的消息：   "+picChange);
                                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonForPic);
                                log.info("4个点以巡检完，等待"+sleepTime/1000+"秒钟");
                                Thread.sleep(sleepTime);
                                countForInstance = 0;
                            }
                        }
                    }

                    {
                        //专为检测的  1张图片一轮
                        Map<String,String> maoForOne= redisTemplate.opsForHash().entries("t_sys_param:isCheckByOne");
                        Boolean flagForOne = false;
                        if(maoForOne != null  && maoForOne.size()>0){
                            flagForOne = Boolean.valueOf(maoForOne.get("content"));
                        }
                        if(flagForOne){
                            Map<String,String> maoForSleep= redisTemplate.opsForHash().entries("t_sys_param:checkWaitTimeByOne");
                            Long sleepTime = 10000L;
                            if(maoForSleep != null  && maoForSleep.size()>0){
                                sleepTime = Long.valueOf(maoForSleep.get("content"))*1000L;
                            }
                            Map<String,String> jasonForPic=new HashMap<>();
                            jasonForPic.put("type","picChange");
                            jasonForPic.put("msg","该换图片了");
                            String picChange=JSON.toJSONString(jasonForPic);
                            log.info("发送给前端的消息：   "+picChange);
                            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonForPic);
                            log.info("1个点以巡检完，等待"+sleepTime/1000+"秒钟");
                            Thread.sleep(sleepTime);
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
                        Map<String,String> jsonMap=new HashMap<>();
                        jsonMap.put("type","newTask");
                        jsonMap.put("taskId","");
                        String jsonForShut=JSON.toJSONString(jsonMap);
                        log.info("任务终止的消息：   "+jsonForShut);
                        Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jsonMap);
                    }
                    Integer abnormal = Integer.valueOf(mapForGet.get("abnormal")) + taskAbnormal;
                    Integer normal = Integer.valueOf(mapForGet.get("normal")) +taskNormal;
                    Integer all = Integer.valueOf(mapForGet.get("all"));
                    int re = abnormal+normal;
                    if(re == all){
                        //所有点都做完了
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

                        sendTaskStateToUp(tCruiseTask,1);
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

                Map<String, String> jsonForLastMap = new HashMap<>();
                jsonForLastMap.put("type", "lastOneInstance");
                jsonForLastMap.put("taskId", taskId);
                String jsonForLast = JSON.toJSONString(jsonForLastMap);
                log.info("发送给前端的消息：" + jsonForLast);
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jsonForLastMap);

                Thread.sleep(15000);
                tCruiseResult.setCState(240);
                tCruiseResultDao.update(tCruiseResult);

                sendTaskStateToUp(tCruiseTask,1);

            }else {
                mapForAbnormal.put("abnormal",abnormal.toString());
                mapForAbnormal.put("normal",normal.toString());
                redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);
            }

            tCruiseDataResultService.updateCruiseAnalyze(cruiseResultIdList);
            //任务结束生成结果，
            Analysis analysis = new Analysis();
            analysis.setTaskId(taskId);
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
        } catch (Exception e) {
            log.error("立即任务异常" + e.getMessage());
            e.printStackTrace();
        }
    }

    private Result sendTaskStateToUp(TCruiseTask tCruiseTask,Integer state){
        //任务状态上报站端
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String,Object>> items= new ArrayList<>();
        Map<String,Object> item = new HashMap<>();
        xmlBaseModel.setType("41");
        SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
        item.put("task_patrolled_id",tCruiseTask.getTaskId()+"_"+simpleDateFormat2.format(tCruiseTask.getStartTime()));
        item.put("task_name",tCruiseTask.getTaskName());
        item.put("task_code",tCruiseTask.getTaskCode());
        item.put("task_state",state);
        item.put("plan_start_time",tCruiseTask.getStartTime());
        if(tCruiseTask.getIfRun() == 172){
            try{
                CronExpression expression = new CronExpression(tCruiseTask.getDateType());
                item.put("start_time",expression.getNextValidTimeAfter(new Date()));
            }catch (Exception e){
                log.info("上报出错"+e.getMessage());
            }
        }else {
            item.put("start_time",tCruiseTask.getStartTime());
        }
        item.put("task_progress","0%");
        Integer i =0;
        Map<String,String> mapForGet = redisTemplate.opsForHash().entries("countForAbnormal:"+tCruiseTask.getTaskId());
        Integer all = Integer.valueOf(mapForGet.get("all"));
        Integer normal = Integer.valueOf(mapForGet.get("normal"));
        Integer abnormal = Integer.valueOf(mapForGet.get("abnormal"));
        i = all -normal -abnormal;


        item.put("task_estimated_time",i*60*5);
        item.put("description","");
        items.add(item);
        xmlBaseModel.setItems(items);

        List<XMLBaseModel> list = new ArrayList<>();
        list.add(xmlBaseModel);
        Map<String,List<XMLBaseModel>> map = new HashMap<>();
        map.put("list",list);
        Result re = null;
        try{
            log.info("信息上报：-"+map);
            re = Constant.otherServer(map,Constant.TCP_URL);//江苏要求
        }catch (Exception e){
            log.info("上报出错"+e.getMessage());
        }
        return re;
    }
}
