package com.yjh.platform.common.quartz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.Result;
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


    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CruiseTaskJob.class);

    //相机抓图
    private static final String PICTURE_URL = "http://iot-center-accessvideo/camera/v1/capturePicture?cameraId={cameraId}";
    //相机转到预置位
    private static final String MOVE_URL = "http://iot-center-accessvideo/camera/v1/moveToPreset?presetId={presetId}&cameraId={cameraId}";
    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    //缺陷接口
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";
    //模板图片路径
    @Value("${spring.picModelPath.dir}")
    private String picModelPath;
    //等待相机转到预置位时间
    @Value("${spring.move.waitTime}")
    private Long waitTime;
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
                Map<String,Object> jasonMap=new HashMap<>();
                jasonMap.put("type","newTask");
                jasonMap.put("taskId",taskId);
                String json=JSON.toJSONString(jasonMap);
                log.info("发送给前端的消息：   "+json);
                WebSocketServer.sendMsg(json);
                log.info(taskDate+"需要执行的任务");
                log.info("开始进行任务" +new Date());
                //tCruiseTask.setTaskId(String.valueOf(UUID.randomUUID()).replace("-", ""));
                String uuid = String.valueOf(UUID.randomUUID()).replace("-", "");//任务结果uuid
                TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);//获取任务
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

            //任务状态
            TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
            tCruiseTaskResult.setTaskResultId(uuid);
            tCruiseTaskResult.setTaskId(taskId);
            //tCruiseTaskResult.setTaskStatus(239);
            tCruiseTaskResult.setRunExecute(tCruiseTask.getType().toString());

            Map<String,String> mapForAbnormal = new HashMap<>();
            mapForAbnormal.put("all",taskCount.toString());
            mapForAbnormal.put("abnormal","0");
            mapForAbnormal.put("normal","0");
            String strForCountAbnormal = "countForAbnormal:"+taskId;
            redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);

            Integer taskAbnormal = 0;//异常数量
            Integer normal = 0;//正常
            List<String> analysisInstanceList = new ArrayList<>();
            log.info("开始巡检"+new Date());
            for (TCruisePointInstance item : instancesList) {

                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                tCruiseTaskResultDetail.setCruiseResultId(uuid+item.getInstanceId().toString());
                tCruiseTaskResultDetail.setTaskResultId(uuid);
                tCruiseTaskResultDetail.setDeviceId(item.getDeviceId());
                tCruiseTaskResultDetail.setInstanceId(item.getInstanceId());
                tCruiseTaskResultDetail.setCruiseTime(new Date());
                String cruiseTime = simpleDateFormat.format(new Date());
                tCruiseTaskResultDetail.setCruiseStatus(253);

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
                    Result re = picture(map2);
                    JSONObject jsonForRe = (JSONObject) JSON.toJSON(re.getData());
                    String urlPath = (String) jsonForRe.get("urlPath");
                    String absPath = (String) jsonForRe.get("absPath");
                    if(absPath == null){
                        //抓图失败 任务失败
                        taskAbnormal = taskAbnormal+1;
                        tCruiseResult = tCruiseResultDao.selectByPrimaryId(taskId);
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
                        jasonMap.put("type","newTask");
                        jasonMap.put("finishedOneInstance",item.getInstanceId());
                        String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
                        log.info("发送给前端的消息："+jsonMessage);
                        WebSocketServer.sendMsg(json);


                    }else {
                        TAlgorithmConf tAlgorithmConf = tAlgorithmConfDao.selectByPrimaryId(item.getCruiseId());
                        if(tAlgorithmConf != null){//摄像头配置了算法
                            tCruiseDataResult.setPicpath(urlPath);
                            tCruiseDataResult.setOrigpic(picModelPath+"/"+item.getCruiseId());
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
                            if(redisTemplate.hasKey(taskId)) {
                                //System.out.println("---------> true");
                                //analysisInstanceList =  (List<String>) redisTemplate.opsForList().g(analysisInstanceList);
                                analysisInstanceList.add(item.getInstanceId().toString());
                                redisTemplate.opsForList().leftPush(taskId,item.getInstanceId().toString());
                            } else {
                                analysisInstanceList.add(item.getInstanceId().toString());
                                redisTemplate.opsForList().leftPushAll(taskId,analysisInstanceList);
                            }
                            Analysis analysis = new Analysis();
                            analysis.setTaskId(taskId);
                            analysis.setInstanceId(item.getInstanceId());
                            analysis.setPicPath(absPath);
                            TAlgorithmInfo tAlgorithmInfo = tAlgorithmInfoDao.selectByPrimaryId(tAlgorithmConf.getAlgorithmId());
                            analysis.setAnalyseType(tAlgorithmInfo.getAnalyseType());
                            analysis.setPicModelPath(picModelPath+"/"+item.getCruiseId());
                            tCruiseDataResult.setPicpath(urlPath);
                            tCruiseDataResult.setOrigpic(picModelPath+"/"+item.getCruiseId());
                            List<Analysis> analysisList = new ArrayList<>();
                            analysisList.add(analysis);
                            Map<String, List<Analysis>> analysisMap  = new HashMap<>();
                            analysisMap.put("list",analysisList);
                            if(tAlgorithmInfo.getIsAi() == 1){//0-缺陷 1-表记
                                analysis(analysisMap);
                            }else {
                                defect(analysisMap);
                            }
                        }else {
                            normal = normal+1;
                            tCruiseResult = tCruiseResultDao.selectByPrimaryId(taskId);
                            Integer taskWait = tCruiseResult.getTaskWait()-1;
                            if(taskWait == 0 ){
                                tCruiseResult.setCState(240);
                            }
                            tCruiseResult.setTaskWait(taskWait);
                            tCruiseResultDao.update(tCruiseResult);

                            tCruiseDataResult.setPicpath(urlPath);
                            tCruiseDataResult.setState(247);
                            tCruiseDataResult.setIdentifyResult(261);
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
                            jasonMap.put("type","newTask");
                            jasonMap.put("finishedOneInstance",item.getInstanceId());
                            String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
                            log.info("发送给前端的消息："+jsonMessage);
                            WebSocketServer.sendMsg(json);

                        }


                    }
                }
                if (230 == item.getCruiseType()) {//todo 红外
                }
                if (231 == item.getCruiseType()) {//todo 在线监控
                }
                if (232 == item.getCruiseType()) {//todo scala
                }

            }
            //计算所有的异常巡检点
            Map<String,String> map  = redisTemplate.opsForHash().entries(strForCountAbnormal);
            int re = Integer.valueOf(map.get("abnormal"))  + Integer.valueOf(map.get("normal"))+taskAbnormal;
            if(re == taskCount){
                //所有点都做完了
                tCruiseTaskResult.setTaskAbnormal(taskAbnormal);
                tCruiseTaskResultDao.insert(tCruiseTaskResult);
            }else {
                mapForAbnormal.put("abnormal","0");
                mapForAbnormal.put("normal","0");
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
            log.error("定时任务异常" + e);
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

}
