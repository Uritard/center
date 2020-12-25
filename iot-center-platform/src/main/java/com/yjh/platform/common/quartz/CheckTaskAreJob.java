package com.yjh.platform.common.quartz;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.websocket.WebSocketServer;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.task.dao.*;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.TAlgorithmConfDao;
import com.yjh.platform.module.user.dao.TAlgorithmInfoDao;
import com.yjh.platform.module.user.dao.TCameraPresetDao;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lqh
 * @since 2020/11/19
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Configuration
public class CheckTaskAreJob extends QuartzJobBean {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(CheckTaskAreJob.class);

    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    private TCruiseTaskAttrDao tCruiseTaskAttrDao;
    @Autowired
    private TCruiseResultDao tCruiseResultDao;
    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;
    @Autowired
    private TCruiseTaskResultDao tCruiseTaskResultDao;
    @Autowired
    private TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao;
    @Autowired
    private TCruiseDataResultDao tCruiseDataResultDao;
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;

    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";

    public void executeInternal(JobExecutionContext context) {

        try {
        String taskId = context.getMergedJobDataMap().getString("taskId");
        TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);//获取任务
        TCruiseResult tCruiseResult = tCruiseResultDao.selectForTaskId(taskId);
        log.info("tCruiseResult:----"+tCruiseResult);
        if(tCruiseResult != null && tCruiseResult.getCState() != 239){
            return;
        }
        //获取任务下的所有的点
        List<Long> instanceIdList =  tCruiseTaskAttrDao.selectInstanceId(taskId);


        //int all = instanceIdList.size();
        //int count =0;
        //获取正常和异常的点数
            //机器人任务终止
        List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(taskId);
        if(robotCodeList != null && robotCodeList.size()>0){
            Map<String,Object> robotTaskStatesMap = new HashMap<>();
            robotTaskStatesMap.put("taskId",taskId);
            robotTaskStatesMap.put("commandValue",4);
            robotTaskStatesMap.put("robotCodeList",robotCodeList);
            robotTaskStates(robotTaskStatesMap);
        }
        Thread.sleep(10000);

            //获取已经做过的点
        List<Long> isFinishedInstanceList = tCruiseTaskResultDetailDao.selectInstanceForTaskGoOn(taskId);
        for(Long finished:isFinishedInstanceList){//删除已经做过的点
            instanceIdList.remove(finished);
        }
        String strForCountAbnormal = "countForAbnormal:"+tCruiseTask.getTaskId();
        Map<String,String> mapForGet  = redisTemplate.opsForHash().entries(strForCountAbnormal);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Integer abnormal = Integer.valueOf(mapForGet.get("abnormal"));
        Integer normal = Integer.valueOf(mapForGet.get("normal")) ;
        //检查每个点的状态
        List<TCruiseTaskResultDetail> TCTRDList = new ArrayList();
            List<TCruiseDataResult> TCDRList = new ArrayList();
        for(Long item:instanceIdList){
            Map<String,String> mapForCruise  = redisTemplate.opsForHash().entries("t_cruise_task_result:"+taskId+":"+item);
            if(mapForCruise.size()>0){
                //count = count+1;
                if("null".equals(mapForCruise.get("cruiseResult")) ){
                    //这个点超时了
                    mapForCruise.put("cruiseResult","247");
                    mapForCruise.put("cruiseAbnormal","251");
                    mapForCruise.put("cruiseStatus","252");
                    mapForCruise.put("resultNum","超时");

                }
                if("247".equals(mapForCruise.get("cruiseResult"))){
                    abnormal = abnormal+1;
                }else {
                    normal = normal+1;
                }
                //TCTRD
                log.info("TCTRD开始构建");
                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                tCruiseTaskResultDetail.setCruiseResultId(mapForCruise.get("taskResultId").toString() + mapForCruise.get("instanceId").toString());
                tCruiseTaskResultDetail.setTaskResultId(mapForCruise.get("taskResultId").toString());
                tCruiseTaskResultDetail.setDeviceId(Long.valueOf(mapForCruise.get("deviceId").toString()));
                tCruiseTaskResultDetail.setInstanceId(Long.valueOf(mapForCruise.get("instanceId").toString()));
                if(mapForCruise.get("cruiseTime").equals("null")){
                    tCruiseTaskResultDetail.setCruiseTime(new Date());
                    mapForCruise.put("cruiseTime",simpleDateFormat.format(new Date()));
                }else {
                    tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(mapForCruise.get("cruiseTime").toString()));
                }

                tCruiseTaskResultDetail.setEndTime(new Date());
                mapForCruise.put("endTime",simpleDateFormat.format(new Date()));
                tCruiseTaskResultDetail.setCruiseStatus(Integer.valueOf(mapForCruise.get("cruiseStatus").toString()));
                tCruiseTaskResultDetail.setRemark(mapForCruise.get("remark").toString());
                TCTRDList.add(tCruiseTaskResultDetail);


                //TCDR
                log.info("TCDR开始构建");
                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                tCruiseDataResult.setCruiseId(Long.valueOf(mapForCruise.get("instanceId").toString()));
                tCruiseDataResult.setPicpath(mapForCruise.get("picpath").toString());
                tCruiseDataResult.setResultNum(mapForCruise.get("resultNum").toString());
                //tCruiseDataResult.setResultDesc(mapForCruise.get("resultDesc").toString());
                tCruiseDataResult.setCruiseType(Integer.valueOf(mapForCruise.get("cruiseType").toString()));
                //tCruiseDataResult.setModifyNum(mapForCruise.get("modifyNum").toString());
                tCruiseDataResult.setOrigpic(mapForCruise.get("origpic").toString());
                tCruiseDataResult.setEvaluationState(257);
                tCruiseDataResult.setCruiseResult(Integer.valueOf(mapForCruise.get("cruiseResult").toString()));
                if (mapForCruise.get("cruiseAbnormal").toString().equals("--") || mapForCruise.get("cruiseAbnormal").toString().equals("null")) {
                    tCruiseDataResult.setCruiseAbnormal(null);
                } else {
                    tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(mapForCruise.get("cruiseAbnormal").toString()));
                }
                tCruiseDataResult.setEvaluationState(257);
                tCruiseDataResult.setCreatetime(new Date());
                tCruiseDataResult.setCruiseResultId(mapForCruise.get("taskResultId").toString() + mapForCruise.get("instanceId").toString());
                tCruiseDataResult.setIsWarn(0);
                TCDRList.add(tCruiseDataResult);
                redisTemplate.opsForHash().putAll("t_cruise_task_result:"+taskId+":"+item, mapForCruise);

            }

        }
        //将正常 异常放回redis
        mapForGet.put("abnormal",abnormal.toString());
        mapForGet.put("normal",normal.toString());
        redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForGet);
        //查库
        log.info("开始入库");
        if(TCTRDList.size()>0){
            tCruiseTaskResultDetailDao.batchInsert(TCTRDList);
        }

        if(TCDRList.size()>0){
            tCruiseDataResultDao.batchInsert(TCDRList);
        }



        //给算法的信息
        Analysis analysis = new Analysis();
        analysis.setTaskId(tCruiseTask.getTaskId());
        analysis.setInstanceId(-1L);
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


        // webSocket通知前端调用巡视监控的接口
        Map<String,Object> jasonMapOnFinished=new HashMap<>();
        jasonMapOnFinished.put("type","finishedOneInstance");
        jasonMapOnFinished.put("taskId",tCruiseTask.getTaskId());
        String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
        log.info("发送给前端的消息："+jsonMessage);
        WebSocketServer.sendMsg(jsonMessage);



        Thread.sleep(15000);

        tCruiseResult.setCState(244);
        tCruiseResult.setTaskWait(0);
        tCruiseResultDao.update(tCruiseResult);

        String uuid = String.valueOf(UUID.randomUUID()).replace("-", "");//任务结果uuid
        //任务状态
        TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
        tCruiseTaskResult.setTaskResultId(uuid);
        tCruiseTaskResult.setTaskId(tCruiseTask.getTaskId());
        tCruiseTaskResult.setTaskAbnormal(abnormal);
        tCruiseTaskResult.setRunExecute(tCruiseTask.getIfRun().toString());
        tCruiseTaskResult.setCruiseTaskTime(simpleDateFormat.parse(mapForGet.get("taskStart")));
        tCruiseTaskResult.setTaskStatus(244);
        tCruiseTaskResult.setCruiseResult(247);
        tCruiseTaskResultDao.insert(tCruiseTaskResult);

        log.info("超期完毕");
        } catch (Exception e) {
            log.error("检查任务超期异常: "+e);
            e.printStackTrace();
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

    private void robotTaskStates(Map<String,Object> robotTaskStatesMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(Constant.ROBOT_TASK_STATUS_URL, robotTaskStatesMap, String.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
