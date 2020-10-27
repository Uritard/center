package com.yjh.platform.module.task.service;

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
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import com.yjh.platform.module.user.entity.TAlgorithmInfo;
import com.yjh.platform.module.user.entity.TCameraPreset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lqh
 * @since 2020/10/27
 */
public class RunAtNowTask implements Runnable{

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
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;

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
    @Value("${spring.picModelPath.dir}")
    private String picModelPath;
    //等待相机转到预置位时间
    @Value("${spring.move.waitTime}")
    private Long waitTime;

    private TCruiseTask tCruiseTask;
    public RunAtNowTask(TCruiseTask tCruiseTask) {
        this.tCruiseTask = tCruiseTask;
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


    @Override
    public void run() {
        try {
            String taskId = tCruiseTask.getTaskId();
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM
            String taskDate = simpleDateFormat.format(tCruiseTask.getStartTime());
            Date date = null;
            try {
                date = simpleDateFormat.parse(taskDate);
            } catch (Exception e) { e.getMessage(); }

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
                //tCruiseTaskResultDetail.setCruiseTime(new Date());
                String cruiseTime = simpleDateFormat.format(new Date());
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
                Map map = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                String str = "t_cruise_task_result:"+taskId + item.getInstanceId();
                Map map2 = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                map.putAll(map2);
                map.put("taskId",taskId);
                map.put("startTime",simpleDateFormat.format(date));
                map.put("if_run",tCruiseTask.getIfRun().toString());
                map.put("endTime",simpleDateFormat.format(new Date()));
                map.put("cruiseTime",cruiseTime);

                redisTemplate.opsForHash().putAll(str, map);
                }
                log.info("完成任务执行"+new Date());
        } catch (Exception e) {
            log.error("定时任务异常" + e);
        }
    }
}
