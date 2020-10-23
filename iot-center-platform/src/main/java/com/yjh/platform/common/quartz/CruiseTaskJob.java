package com.yjh.platform.common.quartz;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rabbitmq.tools.json.JSONUtil;
import com.yjh.platform.common.Constant;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.text.ParseException;
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
    private TCruiseTaskResultDao tCruiseTaskResultDao;
    @Autowired
    private TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao;
    @Autowired
    private TCruiseDataResultDao tCruiseDataResultDao;
    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;
    @Autowired
    private TAlgorithmConfDao tAlgorithmConfDao;
    @Autowired
    private TAlgorithmInfoDao tAlgorithmInfoDao;

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
    @Value("${picModelPath.dir}")
    private String picModelPath;
    //等待相机转到预置位时间
    @Value("${waitTime}")
    private Long waitTime;
    /**
     * 巡视任务类
     * @param context
     */
    public void executeInternal(JobExecutionContext context) {
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
            try {
                //Thread.sleep(10000);
                Map<String,Object> jasonMap=new HashMap<>();
                jasonMap.put("type","newTask");
                jasonMap.put("taskId",taskId);
                String json=JSON.toJSONString(jasonMap);
                log.info("发送给前端的消息：   "+json);
                WebSocketServer.sendMsg(json);
                log.info(taskDate+"需要执行的任务");
                log.info("正在进行任务");
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
                tCruiseResult.setExecuteTime(date);
                tCruiseResultDao.insert(tCruiseResult);//插入一条任务结果
                //任务状态
                TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
                tCruiseTaskResult.setTaskResultId(uuid);
                tCruiseTaskResult.setTaskId(taskId);
                tCruiseTaskResult.setTaskStatus(239);
                tCruiseTaskResult.setRunExecute(tCruiseTask.getType().toString());
                tCruiseTaskResultDao.insert(tCruiseTaskResult);
                //Integer taskAbnormasl = 0;//异常数量
                log.info("开始巡检");
//                List<TCruiseTaskResultDetail> resultDetailList = new LinkedList<>();//详细
//                List<TCruiseDataResult> dataResultList = new LinkedList<>();//巡检数据
                for (TCruisePointInstance item : instancesList) {
                    TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                    tCruiseTaskResultDetail.setCruiseResultId(uuid+item.getInstanceId().toString());
                    tCruiseTaskResultDetail.setTaskResultId(uuid);
                    tCruiseTaskResultDetail.setDeviceId(item.getDeviceId());
                    tCruiseTaskResultDetail.setInstanceId(item.getInstanceId());
                    tCruiseTaskResultDetail.setCruiseTime(new Date());
                    tCruiseTaskResultDetail.setCruiseStatus(252);

                    TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                    tCruiseDataResult.setCruiseResultId(uuid+item.getInstanceId().toString());
                    tCruiseDataResult.setCruiseId(item.getInstanceId());
                    //一次循环 一个巡检点
                    if (228 == item.getCruiseType()) {//todo 机器人
                    }
                    if (229 == item.getCruiseType()) {
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
                        //String picUrl = "66666";
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
                                analysis.setPicModelPath(System.getProperty(picModelPath));//模板图片暂时没有
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
                            //todo 获取算法分析的结果
                            tCruiseDataResult.setPicpath(picUrl);

                        }
                    }
                    if (230 == item.getCruiseType()) {//todo 红外
                    }
                    if (231 == item.getCruiseType()) {//todo 在线监控
                    }
                    if (232 == item.getCruiseType()) {//todo scala
                    }
                    tCruiseTaskResultDetail.setEndTime(new Date());
                    //resultDetailList.add(tCruiseTaskResultDetail);
                    Map map = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail,true));
                    String str = "t_cruise_task_result:"+tCruiseTaskResultDetail.getCruiseResultId();
                    //dataResultList.add(tCruiseDataResult);
                    Map map2 = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult,true));
                    map.putAll(map2);
                    map.put("taskId",taskId);
                    map.put("startTime",simpleDateFormat.format(date));
                    redisTemplate.opsForHash().putAll(str, map);
                    taskCount--;//一个巡检点结束
                    tCruiseResult.setTaskWait(taskCount);
                    tCruiseResultDao.update(tCruiseResult);
                    Map<String,Object> jasonMap2=new HashMap<>();
                    jasonMap2.put("type","finishedOneInstance");
                    jasonMap2.put("taskId",taskId);
                    String json2=JSON.toJSONString(jasonMap2);
                    log.info("发送给前端的消息：   "+json2);
                    WebSocketServer.sendMsg(json2);
                }
                //任务结束生成结果，
                //todo 结果的状态未作处理
                tCruiseResult.setCState(240);
                tCruiseResult.setTaskWait(0);
                tCruiseResultDao.update(tCruiseResult);
                tCruiseTaskResult.setTaskStatus(240);
                //tCruiseTaskResult.setTaskAbnormal(taskAbnormasl);
                //if (taskAbnormasl>0){tCruiseTaskResult.setCruiseResult(1);}else{tCruiseTaskResult.setCruiseResult(0);}
                tCruiseTaskResultDao.update(tCruiseTaskResult);
                log.info("完成任务执行");
            } catch (Exception e) {
                log.error("定时任务异常" + e);
            }
        }

    }
    //相机抓图
    private static String picture(HashMap map) {
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
