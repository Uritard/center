package com.yjh.platform.common.quartz;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TCruisePointInstanceNameDetail;
import com.yjh.platform.module.task.dao.*;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.TCruiseDataResultService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lqh
 * @since 2020/12/28
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Configuration
public class TaskShutDownJob extends QuartzJobBean {
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(TaskShutDownJob.class);


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
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private TCruiseDataResultService tCruiseDataResultService;


    //表记分析
    private void analysis(Map<String, List<Analysis>> analysisMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(Constant.ALGORITHM_URL, analysisMap, String.class);
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


    public void executeInternal(JobExecutionContext context) {
        try {
            //Thread.sleep(16000);

            String taskId = context.getMergedJobDataMap().getString("taskId");
            TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);//获取任务
            TCruiseResult tCruiseResult = tCruiseResultDao.selectForTaskId(taskId);
            log.info("tCruiseResult:----"+tCruiseResult);

//            String taskId = context.getMergedJobDataMap().getString("taskId");
//            TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);//获取任务
            //给算法暂停
            Analysis analysis = new Analysis();
            analysis.setTaskId(tCruiseTask.getTaskId());
            analysis.setInstanceId(-1L);
            List<Analysis> analysisList = new ArrayList<>();
            analysisList.add(analysis);
            Map<String, List<Analysis>> analysisMap  = new HashMap<>();
            analysisMap.put("list",analysisList);
            log.info("算法信息：    "+analysisMap);
            analysis(analysisMap);
            log.info("任务终止"+tCruiseTask.getTaskId());


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

            log.info("任务终止开始操作");
            DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            //TCruiseResult tCruiseResult = tCruiseResultDao.selectForTaskId(taskId);
            //log.info("tCruiseResult:----" + tCruiseResult);

            //获取任务下的所有的点
            List<Long> instanceIdList = tCruiseTaskAttrDao.selectInstanceId(taskId);

            List<Long> isFinishedInstanceList = tCruiseTaskResultDetailDao.selectInstanceForTaskGoOn(taskId);
            for(Long finished:isFinishedInstanceList){//删除已经做过的点
                instanceIdList.remove(finished);
            }
            String strForCountAbnormal = "countForAbnormal:"+tCruiseTask.getTaskId();
            Map<String,String> mapForGet  = redisTemplate.opsForHash().entries(strForCountAbnormal);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Integer abnormal = Integer.valueOf(mapForGet.get("abnormal"));
            Integer normal = Integer.valueOf(mapForGet.get("normal")) ;
            Integer all = Integer.valueOf(mapForGet.get("all")) ;
            //检查每个点的状态
            List<TCruiseTaskResultDetail> TCTRDList = new ArrayList();
            List<TCruiseDataResult> TCDRList = new ArrayList();
            int taskWait = 0;
            List<String> cruiseResultIdList = new ArrayList<>();
            if(instanceIdList.size() != 0) {
                List<TCruisePointInstanceNameDetail> instancesList = tCruisePointInstanceDao.selectForTask(instanceIdList);//巡检点
                for (TCruisePointInstanceNameDetail item : instancesList) {
                    Map<String, String> mapForCruise = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + item.getInstanceId());
                    if (mapForCruise.size() > 0) {
                        //count = count+1;
                        if ("null".equals(mapForCruise.get("cruiseResult"))) {
                            //任务终止
                            mapForCruise.put("cruiseResult", "247");
                            //mapForCruise.put("cruiseAbnormal","251");
                            mapForCruise.put("cruiseStatus", "253");
                            mapForCruise.put("resultNum", "任务终止");
                        }
                        if ("247".equals(mapForCruise.get("cruiseResult"))) {
                            abnormal = abnormal + 1;
                            taskWait = taskWait + 1;
                        } else {
                            normal = normal + 1;
                            taskWait = taskWait + 1;
                        }
                        //TCTRD
                        log.info("TCTRD开始构建");
                        TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                        tCruiseTaskResultDetail.setCruiseResultId(mapForCruise.get("taskResultId").toString() + mapForCruise.get("instanceId").toString());
                        tCruiseTaskResultDetail.setTaskResultId(mapForCruise.get("taskResultId").toString());
                        tCruiseTaskResultDetail.setDeviceId(Long.valueOf(mapForCruise.get("deviceId").toString()));
                        tCruiseTaskResultDetail.setInstanceId(Long.valueOf(mapForCruise.get("instanceId").toString()));
                        tCruiseTaskResultDetail.setInstanceName(mapForCruise.get("instanceName"));
                        tCruiseTaskResultDetail.setDeviceName(mapForCruise.get("deviceName"));
                    if(mapForCruise.get("cruiseTime").equals("null")){
                        tCruiseTaskResultDetail.setCruiseTime(new Date());
                        mapForCruise.put("cruiseTime",simpleDateFormat.format(new Date()));
                    }else {
                        tCruiseTaskResultDetail.setCruiseTime(simpleDateFormat.parse(mapForCruise.get("cruiseTime").toString()));
                    }

                        //tCruiseTaskResultDetail.setEndTime(new Date());
                        //mapForCruise.put("endTime",simpleDateFormat.format(new Date()));
                        tCruiseTaskResultDetail.setCruiseStatus(253);
                        tCruiseTaskResultDetail.setRemark(mapForCruise.get("remark").toString());
                        TCTRDList.add(tCruiseTaskResultDetail);
                        cruiseResultIdList.add(tCruiseTaskResultDetail.getCruiseResultId());


                        //TCDR
                        log.info("TCDR开始构建");
                        TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                        tCruiseDataResult.setCruiseId(Long.valueOf(mapForCruise.get("instanceId").toString()));
                        if(mapForCruise.get("picpath") == null){
                            mapForCruise.put("picpath","null");
                        }
                        tCruiseDataResult.setPicpath(mapForCruise.get("picpath").toString());
                        tCruiseDataResult.setResultNum(mapForCruise.get("resultNum").toString());
                        //tCruiseDataResult.setResultDesc(mapForCruise.get("resultDesc").toString());
                        tCruiseDataResult.setCruiseType(Integer.valueOf(mapForCruise.get("cruiseType").toString()));
                        //tCruiseDataResult.setModifyNum(mapForCruise.get("modifyNum").toString());
                        tCruiseDataResult.setCruiseResult(Integer.valueOf(mapForCruise.get("cruiseResult")));
                        tCruiseDataResult.setOrigpic(mapForCruise.get("origpic").toString());
                        tCruiseDataResult.setCruiseName(mapForCruise.get("cruiseName"));
                        tCruiseDataResult.setCreatetime(simpleDateFormat.parse(mapForCruise.get("cruiseTime")));
                        tCruiseDataResult.setCruiseResultId(mapForCruise.get("taskResultId").toString() + mapForCruise.get("instanceId").toString());
                        tCruiseDataResult.setEvaluationState(257);
//                        if(mapForCruise.get("cruiseAbnormal") == null){
//                            tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(mapForCruise.get("cruiseAbnormal")));
//                        }
                        tCruiseDataResult.setIsWarn(0);
                        TCDRList.add(tCruiseDataResult);
                        redisTemplate.opsForHash().putAll("t_cruise_task_result:" + taskId + ":" + item.getInstanceId(), mapForCruise);

                        {
                            //巡视点结果上报站端
                            XMLBaseModel xmlBaseModel = new XMLBaseModel();
                            List<Map<String,Object>> xmlItems = new ArrayList<>();
                            Map<String,Object> xmlItem = new HashMap<>();
                            xmlBaseModel.setType("61");
                            xmlItem.put("patroldevice_code",item.getInstanceId());
                            xmlItem.put("task_name",tCruiseTask.getTaskName());
                            xmlItem.put("task_code",tCruiseTask.getTaskCode());
                            xmlItem.put("device_name",item.getCruiseName());
                            xmlItem.put("device_id",item.getInstanceId());
                            xmlItem.put("material_id",item.getRealCode());
                            xmlItem.put("value","任务终止");
                            xmlItem.put("value_unit","");
                            xmlItem.put("unit","");
                            xmlItem.put("time",simpleDateFormat.format(new Date()));
                            //todo
                            xmlItem.put("recognition_type","");
                            xmlItem.put("file_type","2");
                            xmlItem.put("file_path","");
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

                    } else {
                        //未放入缓存的点 未开始巡视的点。
                        taskWait = taskWait + 1;
                        abnormal = abnormal + 1;
                        TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                        tCruiseTaskResultDetail.setCruiseResultId(tCruiseResult.getTaskResultId() + item.getInstanceId().toString());
                        tCruiseTaskResultDetail.setTaskResultId(tCruiseResult.getTaskResultId());
                        tCruiseTaskResultDetail.setDeviceId(item.getDeviceId());
                        tCruiseTaskResultDetail.setInstanceId(item.getInstanceId());
                        tCruiseTaskResultDetail.setDeviceName(item.getDeviceName());
                        tCruiseTaskResultDetail.setInstanceName(item.getInstanceName());
                        tCruiseTaskResultDetail.setCruiseTime(new Date());
                        String cruiseTime = simpleDateFormat.format(new Date());
                        tCruiseTaskResultDetail.setCruiseStatus(253);
                        TCTRDList.add(tCruiseTaskResultDetail);
                        cruiseResultIdList.add(tCruiseTaskResultDetail.getCruiseResultId());

                        TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                        tCruiseDataResult.setCruiseResultId(tCruiseResult.getTaskResultId() + item.getInstanceId().toString());
                        tCruiseDataResult.setPicpath("null");
                        tCruiseDataResult.setCruiseId(item.getInstanceId());
                        tCruiseDataResult.setCruiseType(item.getCruiseType());
                        tCruiseDataResult.setCruiseName(item.getCruiseName());
                        tCruiseDataResult.setResultNum("任务终止");
                        tCruiseDataResult.setCruiseResult(247);
                        //tCruiseDataResult.setCruiseAbnormal(250);
                        tCruiseDataResult.setCreatetime(new Date());
                        tCruiseDataResult.setEvaluationState(257);
                        tCruiseDataResult.setIsWarn(0);

                        Map tCruiseTaskResultDetailMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseTaskResultDetail, true));
                        String str = "t_cruise_task_result:" + taskId + ":" + item.getInstanceId();
                        Map tCruiseDataResultMap = Object2Map.toStringMap(Object2Map.objectToMap(tCruiseDataResult, true));
                        tCruiseTaskResultDetailMap.putAll(tCruiseDataResultMap);
                        tCruiseTaskResultDetailMap.put("taskId", taskId);
                        //tCruiseTaskResultDetailMap.put("startTime",simpleDateFormat.format(date));
                        tCruiseTaskResultDetailMap.put("if_run", tCruiseTask.getIfRun().toString());
                        tCruiseTaskResultDetailMap.put("device_mete_id", item.getDeviceMeteId().toString());
                        tCruiseTaskResultDetailMap.put("taskName", tCruiseTask.getTaskName());
                        redisTemplate.opsForHash().putAll(str, tCruiseTaskResultDetailMap);
                        TCDRList.add(tCruiseDataResult);

                        {
                            //巡视点结果上报站端
                            XMLBaseModel xmlBaseModel = new XMLBaseModel();
                            List<Map<String,Object>> xmlItems = new ArrayList<>();
                            Map<String,Object> xmlItem = new HashMap<>();
                            xmlBaseModel.setType("61");
                            xmlItem.put("patroldevice_code",item.getInstanceId());
                            xmlItem.put("task_name",tCruiseTask.getTaskName());
                            xmlItem.put("task_code",tCruiseTask.getTaskCode());
                            xmlItem.put("device_name",item.getCruiseName());
                            xmlItem.put("device_id",item.getInstanceId());
                            xmlItem.put("material_id",item.getRealCode());
                            xmlItem.put("value","任务终止");
                            xmlItem.put("value_unit","");
                            xmlItem.put("unit","");
                            xmlItem.put("time",simpleDateFormat.format(new Date()));
                            //todo
                            xmlItem.put("recognition_type","");
                            xmlItem.put("file_type","2");
                            xmlItem.put("file_path","");
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
                    }
                }
                //将正常 异常放回redis
                if((abnormal+normal) > all){
                    if(normal > 0){
                        abnormal = all - normal;
                    }else {
                        abnormal = all;
                    }

                }
                mapForGet.put("abnormal", abnormal.toString());
                mapForGet.put("normal", normal.toString());
                redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForGet);
                //查库
                log.info("开始入库");
                if (TCDRList.size() > 0) {
                    tCruiseDataResultDao.batchInsert(TCDRList);
                }
                if (TCTRDList.size() > 0) {
                    tCruiseTaskResultDetailDao.batchInsert(TCTRDList);
                    tCruiseDataResultService.updateCruiseAnalyze(cruiseResultIdList);
                }

            }

            //终止
            tCruiseResult.setCState(242);
            tCruiseResult.setTaskWait(taskWait);
            tCruiseResultDao.update(tCruiseResult);

            TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult();
            tCruiseTaskResult.setTaskResultId(tCruiseResult.getTaskResultId());
            tCruiseTaskResult.setTaskId(tCruiseTask.getTaskId());
            tCruiseTaskResult.setTaskName(tCruiseTask.getTaskName());
            tCruiseTaskResult.setTaskAbnormal(abnormal);
            tCruiseTaskResult.setRunExecute(tCruiseTask.getIfRun().toString());
            tCruiseTaskResult.setCruiseTaskTime(format.parse(mapForGet.get("taskStart")));
            tCruiseTaskResult.setTaskStatus(242);
            tCruiseTaskResult.setCruiseResult(247);
            TCruiseTaskResult tCruiseTaskResultForIsIn = tCruiseTaskResultDao.selectByPrimaryId(tCruiseResult.getTaskResultId());
            if(tCruiseTaskResultForIsIn != null){
                tCruiseTaskResultDao.update(tCruiseTaskResult);
            }else {
                tCruiseTaskResultDao.insert(tCruiseTaskResult);
            }



            Map<String,String> jasonMapOnFinished=new HashMap<>();
            jasonMapOnFinished.put("type","newTask");
            jasonMapOnFinished.put("taskId",tCruiseTask.getTaskId());
            String jsonMessage= JSON.toJSONString(jasonMapOnFinished);
            log.info("发送给前端的消息："+jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);

        } catch (Exception e) {
            log.error("任务终止异常: " + e);
            e.printStackTrace();
        }
    }
}
