package com.yjh.platform.module.task.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;

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

    private Logger log = LoggerFactory.getLogger(RunAtNowTask.class);

    //相机抓图
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePicture?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}&cameraId={cameraId}";
    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    //缺陷接口
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";
    //模板图片路径
    private String picModelPath;
    //等待相机转到预置位时间
    private Long waitTime;
    //任务超期天数
    private Long tasksAreTime;

    private TCruiseTask tCruiseTask;

    public RunAtNowTask(TCruiseTask tCruiseTask,Long waitTime ,String picModelPath,RedisTemplate redisTemplate,TCruisePointInstanceDao tCruisePointInstanceDao,
                        TCameraPresetDao tCameraPresetDao,TCruiseResultDao tCruiseResultDao,TAlgorithmConfDao tAlgorithmConfDao,
                        TAlgorithmInfoDao tAlgorithmInfoDao,TCruisePlanAttrDao tCruisePlanAttrDao,TCruiseDataResultDao tCruiseDataResultDao,
                        TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao,TCruiseTaskResultDao tCruiseTaskResultDao,Boolean isGoOn,Long tasksAreTime,
                        TRobotInspectionDao tRobotInspectionDao) {
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


    @Override
    public void run() {
        try {
            //Thread.sleep(10000);
            Long timeIsOk = tasksAreTime*24*60*60*100;
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
            Map<String,Object> jasonMap=new HashMap<>();
            jasonMap.put("type","newTask");
            jasonMap.put("taskId",taskId);
            String json=JSON.toJSONString(jasonMap);
            log.info("发送给前端的消息：   "+json);
            WebSocketServer.sendMsg(json);
            log.info(taskDate+"需要执行的任务");

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
            List<TCruisePointInstance> instancesList = tCruisePointInstanceDao.selectForTask(instanceIdList);//巡检点
            //开始任务
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
                tCruiseResult.setTaskCount(taskCount);
                tCruiseResult.setTaskWait(taskCount);
                tCruiseResult.setCreateTime(date);
                tCruiseResult.setExecuteTime(simpleDateFormat.parse(simpleDateFormat.format(new Date())));
                tCruiseResultDao.insert(tCruiseResult);//插入一条任务结果
            }

            //任务状态
            TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
            tCruiseTaskResult.setTaskResultId(tCruiseResult.getTaskResultId());
            tCruiseTaskResult.setTaskId(taskId);
            //tCruiseTaskResult.setTaskStatus(239);
            tCruiseTaskResult.setRunExecute(tCruiseTask.getType().toString());
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
            Integer taskAbnormal = 0;//异常数量
            Integer taskNormal = 0;//正常
            List<String> analysisInstanceList = new ArrayList<>();

            //找出机器人做任务的巡检点
            List<Long> robotInstanceList = new ArrayList<>();
            for (TCruisePointInstance item : instancesList) {
                if (228 == item.getCruiseType()) {//todo 机器人
                    robotInstanceList.add(item.getCruiseId());
                }
            }
            log.info("robotInstanceList   :" +robotInstanceList);
            if(robotInstanceList.size() != 0){
                List<Long> robotId = tRobotInspectionDao.selectForRobotTask(robotInstanceList);
                List<RobotTaskInstanceInfo> robotTaskInfoList = new ArrayList<>();
                log.info("robotId   :" +robotId);
                for (Long item: robotId){
                    RobotTaskInstanceInfo robotTaskInfo = new RobotTaskInstanceInfo();
                    robotTaskInfo.setCruiseType(tCruiseTask.getType());
                    robotTaskInfo.setTaskId(taskId);
                    robotTaskInfo.setPriority(4);//优先级 暂定4
                    robotTaskInfo.setTaskName(tCruiseTask.getTaskName());
                    List<String> deviceList = tRobotInspectionDao.selectRobotInspectionId(robotInstanceList,item);
                    robotTaskInfo.setDeviceList(deviceList);
                    robotTaskInfoList.add(robotTaskInfo);
                }
                log.info("robotTaskInfoList   :" +robotTaskInfoList);
            }

            log.info("开始巡检"+new Date());
            for (TCruisePointInstance item : instancesList) {
                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                tCruiseTaskResultDetail.setCruiseResultId(tCruiseResult.getTaskResultId()+item.getInstanceId().toString());
                tCruiseTaskResultDetail.setTaskResultId(tCruiseResult.getTaskResultId());
                tCruiseTaskResultDetail.setDeviceId(item.getDeviceId());
                tCruiseTaskResultDetail.setInstanceId(item.getInstanceId());
                tCruiseTaskResultDetail.setCruiseTime(new Date());
                String cruiseTime = simpleDateFormat.format(new Date());
                tCruiseTaskResultDetail.setCruiseStatus(253);

                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                tCruiseDataResult.setCruiseResultId(tCruiseResult.getTaskResultId()+item.getInstanceId().toString());
                tCruiseDataResult.setCruiseId(item.getInstanceId());
                //一次循环 一个巡检点
                if (229 == item.getCruiseType() || 230 == item.getCruiseType()) {//视频 红外
                    log.info("巡检点开始巡检"+new Date());
                    tCruiseDataResult.setCruiseType(item.getCruiseType());
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
                    Result re = picture(map2);
                    JSONObject jsonForRe = (JSONObject) JSON.toJSON(re.getData());
                    //todo 对于相机的返回错误分析  任务异常终止/超期
                    String urlPath = (String) jsonForRe.get("urlPath");
                    String absPath = (String) jsonForRe.get("absPath");
                    String isOk = re.getMessage();
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
                        tCruiseDataResult.setState(250);
                        tCruiseDataResult.setIdentifyResult(262);
                        tCruiseDataResultDao.insert(tCruiseDataResult);
                        tCruiseTaskResultDetail.setCruiseStatus(254);
                        tCruiseTaskResultDetail.setEndTime(new Date());
                        tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);

                        Map tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                        String str = "t_cruise_task_result:"+taskId + item.getInstanceId();
                        Map tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                        tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                        tCruiseTaskResultDetailMap.put("taskId",taskId);
                        tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                        tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                        tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                        tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);

                        redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);
                        // webSocket通知前端调用巡视监控的接口
                        Map<String,Object> jasonMapOnFinished=new HashMap<>();
                        jasonMapOnFinished.put("type","finishedOneInstance");
                        jasonMapOnFinished.put("taskId",taskId);
                        String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
                        log.info("发送给前端的消息："+jsonMessage);
                        WebSocketServer.sendMsg(jsonMessage);


                    }else {
                        TAlgorithmConf tAlgorithmConf = tAlgorithmConfDao.selectByPrimaryId(item.getCruiseId());
                        if(tAlgorithmConf != null){//摄像头配置了算法
                            tCruiseDataResult.setPicpath(urlPath);
                            tCruiseDataResult.setOrigpic(absPath);
                            Map tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                            String str = "t_cruise_task_result:"+taskId + item.getInstanceId();

                            Map tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                            tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                            tCruiseTaskResultDetailMap.put("taskId",taskId);
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
                            analysis.setTaskId(taskId);
                            analysis.setInstanceId(item.getInstanceId());
                            analysis.setPicPath(absPath);
                            TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoDao.selectByPrimaryId(tAlgorithmConf.getAlgorithmId());
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
                            tCruiseResult = tCruiseResultDao.selectByPrimaryId(tCruiseResult.getTaskResultId());
                            Integer taskWait = tCruiseResult.getTaskWait()-1;
                            if(taskWait == 0 ){
                                tCruiseResult.setCState(240);
                            }
                            tCruiseResult.setTaskWait(taskWait);
                            tCruiseResultDao.update(tCruiseResult);

                            tCruiseDataResult.setPicpath(urlPath);
                            tCruiseDataResult.setOrigpic(absPath);
                            tCruiseDataResult.setState(247);
                            tCruiseDataResult.setIdentifyResult(261);
                            tCruiseDataResult.setResultNum("已拍照");
                            tCruiseDataResult.setEvaluationState(257);
                            tCruiseDataResultDao.insert(tCruiseDataResult);
                            tCruiseTaskResultDetail.setCruiseStatus(252);
                            tCruiseTaskResultDetail.setEndTime(new Date());
                            tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);

                            Map tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                            String str = "t_cruise_task_result:"+taskId + item.getInstanceId();
                            Map tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                            tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                            tCruiseTaskResultDetailMap.put("taskId",taskId);
                            tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                            tCruiseTaskResultDetailMap.put("if_run",tCruiseTask.getIfRun().toString());
                            tCruiseTaskResultDetailMap.put("endTime",simpleDateFormat.format(new Date()));
                            tCruiseTaskResultDetailMap.put("cruiseTime",cruiseTime);

                            redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);

                            // webSocket通知前端调用巡视监控的接口
                            Map<String,Object> jasonMapOnFinished=new HashMap<>();
                            jasonMapOnFinished.put("type","finishedOneInstance");
                            jasonMapOnFinished.put("taskId",taskId);
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
                if((taskEndTime - taskIsStart)>timeIsOk){
                    //任务超期
                    tCruiseResult.setCState(244);
                    tCruiseResultDao.update(tCruiseResult);
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
                    log.info("任务超期"+tCruiseTask.getTaskId());
                    Map<String,Object> jsonMap=new HashMap<>();
                    jsonMap.put("type","taskAre");
                    jsonMap.put("taskId",taskId);
                    String jsonForTaskAre= JSON.toJSONString(jsonMap);
                    log.info("任务超期的消息：   "+jsonForTaskAre);
                    WebSocketServer.sendMsg(jsonForTaskAre);
                    return;

                }
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
                        tCruiseTaskResultDao.insert(tCruiseTaskResult);
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
                tCruiseTaskResultDao.insert(tCruiseTaskResult);

            }else {
                mapForAbnormal.put("abnormal",abnormal.toString());
                mapForAbnormal.put("normal",normal.toString());
                redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);
            }
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
            log.error("立即任务异常" + e);
        }
    }
}
