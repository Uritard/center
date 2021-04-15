package com.yjh.accessrobot.netty.server;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.module.command.service.RobotService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author YC
 * @date 2020/12/8 9:58
 */
@lombok.extern.slf4j.Slf4j
public class CruiseResultDealThread implements Runnable{

    private static final String DATE_TIME_FORMAT_TPL = "yyyy-MM-dd HH:mm:ss";
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT_TPL);

    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    //缺陷接口
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";

    private RedisTemplate redisTemplate;

    private Map<String,String> cruiseResultMap;

    private String webSocketUrl;

    public CruiseResultDealThread(Map<String,String> cruiseResultMap,RedisTemplate redisTemplate,String webSocketUrl){
        this.cruiseResultMap = cruiseResultMap;
        this.redisTemplate = redisTemplate;
        this.webSocketUrl = webSocketUrl;
    }

    @Override
    public void run(){
        try {
            log.info("Process CruiseResult Starting >>>>>>> cruiseResultMap==="+cruiseResultMap);
            String taskId = cruiseResultMap.get("taskCode");

            //根据taskId查询相关内容
            TCruiseTask tCruiseTask = StaticContextAccessor.getBean(RobotService.class).selectTCruiseTask(taskId);
            log.info("taskId是: "+taskId+"的任务数据tCruiseTask是: "+tCruiseTask);
            TCruiseResult tCruiseResult = StaticContextAccessor.getBean(RobotService.class).selectTaskResultId(taskId);
            log.info("taskResultId是==="+tCruiseResult.getTaskResultId());
            /*
            * 机器人本体任务,不存结果,只做展示
            * */
            if (Objects.nonNull(tCruiseTask.getTaskType()) && tCruiseTask.getTaskType() == 271){

                Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:"+cruiseResultMap.get("robotCode")+ ":" +taskId);
                Map<String,String> tCruiseTaskResultMap = new HashMap<>();
                String str = null;
                List<Long> instanceIdList = null;
                //遍历在下任务时提前组装好的巡检点信息
                for (String key : robotInfoKeys) {
                    Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                    if (cruiseResultMap.get("robotCode").equals(redisInfoMap.get("robotCode"))
                            && cruiseResultMap.get("taskCode").equals(redisInfoMap.get("taskId"))
                            &&cruiseResultMap.get("deviceId").equals(redisInfoMap.get("inspectionCode"))) {
                        String instanceId = redisInfoMap.get("instanceId");
//                        instanceIdList.add(Long.valueOf(instanceId));

                        str = "t_cruise_task_result:" + tCruiseTask.getTaskId() + ":" + instanceId;

                        tCruiseTaskResultMap.put("taskResultId", tCruiseResult.getTaskResultId());
                        tCruiseTaskResultMap.put("taskId",redisInfoMap.get("taskId"));
                        tCruiseTaskResultMap.put("taskName", tCruiseResult.getTaskName());
                        tCruiseTaskResultMap.put("cruiseStatus","252");
                        tCruiseTaskResultMap.put("instanceName",redisInfoMap.get("inspectionName"));
                        tCruiseTaskResultMap.put("deviceName",redisInfoMap.get("deviceName"));
                        tCruiseTaskResultMap.put("cruiseTime",cruiseResultMap.get("time"));
                        tCruiseTaskResultMap.put("picpath",cruiseResultMap.get("relativePath"));
                        tCruiseTaskResultMap.put("origpic",cruiseResultMap.get("absolutePath"));
                        if (cruiseResultMap.get("fileType").equals("1")){
                            tCruiseTaskResultMap.put("resultPic",cruiseResultMap.get("resultPic"));
                        }
                        if (!"".equals(cruiseResultMap.get("value"))){
                            tCruiseTaskResultMap.put("resultNum",cruiseResultMap.get("value"));//只有值
                            tCruiseTaskResultMap.put("cruiseResult","246");
                            tCruiseTaskResultMap.put("cruiseAbnormal","null");
                        }else {
                            tCruiseTaskResultMap.put("resultNum","--");
                            if ("3".equals(cruiseResultMap.get("fileType"))){
                                tCruiseTaskResultMap.put("cruiseResult","246");
                                tCruiseTaskResultMap.put("cruiseAbnormal","null");
                            }else {
                                tCruiseTaskResultMap.put("cruiseResult","247");
                                tCruiseTaskResultMap.put("cruiseAbnormal","249");//数据异常
                            }
                        }
                        tCruiseTaskResultMap.put("evaluationState","257");
                        tCruiseTaskResultMap.put("createtime",cruiseResultMap.get("time"));

                        redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);
                        //做完一个点给前端推一次webSocket
                        Map<String, Object> jasonMap = new HashMap<>();
                        jasonMap.put("type", "finishedOneInstance");
                        jasonMap.put("taskId", taskId);
                        String json = JSON.toJSONString(jasonMap);
                        Constant.postUrl(webSocketUrl,json);
                    }
                }
                //获取机器人本体任务的状态并更新
                Map<String, Object> redisInfoMap = redisTemplate.opsForHash().entries("RobotTaskStatus:"+cruiseResultMap.get("robotCode")+":"+taskId);
                Integer taskState = Integer.valueOf(redisInfoMap.get("taskState").toString());
                Integer cState = null;
                switch (taskState){
                    case 1:
                        cState = 240;break;//已执行
                    case 2:
                        cState = 239;break;//正在执行
                    case 3:
                        cState = 241;break;//任务暂停
                    case 4:
                        cState = 242;break;//任务终止
                    case 5:
                        cState = 238;break;//任务未开始
                    case 6:
                        cState = 244;break;//任务超期
                    default:break;
                }
                Set<String> cruiseKey = redisScan("t_cruise_task_result:" + taskId);
                if (robotInfoKeys.size() == cruiseKey.size()){
                    cState = 240;
                    TimeUnit.SECONDS.sleep(2);
                    //本体任务做完，将构造的巡视点数据删除
                    /*log.info("本体做完的点==="+instanceIdList);
                    StaticContextAccessor.getBean(RobotService.class).deleteInstanceId(instanceIdList);*/
                }
                tCruiseResult.setCState(cState);
                tCruiseResult.setExecuteTime(sdf.parse(tCruiseTaskResultMap.get("createtime")));
                log.info("tCruiseResult的内容是==="+tCruiseResult);
                StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);


            }
            /*
            * 巡视主机下发给机器人的任务
            * */
            else {
                List<TCruiseDataResult> tCDRList = new ArrayList<>();
                List<TCruiseTaskResultDetail> tCTRDList = new ArrayList<>();
                List<String> cruiseResultIdList = new ArrayList<>();
                //读异常点缓存表巡检点
                String strForCountAbnormal = "countForAbnormal:" + taskId;
                Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);

                Integer totalCheckPoint = Integer.valueOf(abnormalCount.get("all").toString());
                Integer abnormalCheckPoint = Integer.valueOf(abnormalCount.get("abnormal").toString()) ;
                Integer normalCheckPoint = Integer.valueOf(abnormalCount.get("normal").toString()) ;
                log.info("总检测点数是==="+totalCheckPoint+",异常点数是==="+abnormalCheckPoint+",正常点数是===" + normalCheckPoint);

                Integer abnormal = abnormalCheckPoint;
                Integer normal = normalCheckPoint;

                Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:"+cruiseResultMap.get("robotCode")+ ":" +taskId);
                Map<String,String> tCruiseTaskResultMap = new HashMap<>();
                String str = null;
                //遍历在下任务时提前组装好的巡检点信息
                for (String key : robotInfoKeys){
                    Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                    if (cruiseResultMap.get("robotCode").equals(redisInfoMap.get("robotCode"))
                            && cruiseResultMap.get("taskCode").equals(redisInfoMap.get("taskId"))
                            &&cruiseResultMap.get("deviceId").equals(redisInfoMap.get("inspectionCode"))) {
                        String instanceId = redisInfoMap.get("instanceId");
                        String cruiseTime = redisInfoMap.get("cruiseTime");
                        String inspectionCode = redisInfoMap.get("inspectionCode");

                        str = "t_cruise_task_result:"+tCruiseTask.getTaskId() + ":" + instanceId;

                        tCruiseTaskResultMap.put("cruiseTime",cruiseResultMap.get("time"));
                        tCruiseTaskResultMap.put("cruiseTaskTime",cruiseTime);
                        tCruiseTaskResultMap.put("taskResultId",tCruiseResult.getTaskResultId());
                        tCruiseTaskResultMap.put("taskName",tCruiseResult.getTaskName());
                        tCruiseTaskResultMap.put("endTime",cruiseResultMap.get("time"));
                        tCruiseTaskResultMap.put("cruiseStatus","252");
                        if (cruiseResultMap.get("fileType").equals("1")){
                            tCruiseTaskResultMap.put("resultPic",cruiseResultMap.get("resultPic"));
                        }
                        if (!"".equals(cruiseResultMap.get("value"))){
                            tCruiseTaskResultMap.put("resultNum",cruiseResultMap.get("value"));//只有值
                            tCruiseTaskResultMap.put("cruiseResult","246");
                            tCruiseTaskResultMap.put("cruiseAbnormal","null");
                            normal = normal + 1;
                            log.info("这次变化的normal是==="+normal);
                        }else {
                            tCruiseTaskResultMap.put("resultNum","--");
                            if ("3".equals(cruiseResultMap.get("fileType"))){
                                tCruiseTaskResultMap.put("cruiseResult","246");
                                tCruiseTaskResultMap.put("cruiseAbnormal","null");
                                normal = normal + 1;
                                log.info("这次变化的normal是==="+normal);
                            }else {
                                tCruiseTaskResultMap.put("cruiseResult","247");
                                tCruiseTaskResultMap.put("cruiseAbnormal","249");//异常告警
                                abnormal = abnormal + 1;
                                log.info("这次变化的abnormal是==="+abnormal);
                            }
                        }
                        tCruiseTaskResultMap.put("picpath",cruiseResultMap.get("relativePath"));
                        tCruiseTaskResultMap.put("origpic",cruiseResultMap.get("absolutePath"));
                        tCruiseTaskResultMap.put("evaluationState","257");
                        tCruiseTaskResultMap.put("createtime",cruiseResultMap.get("time"));
                        tCruiseTaskResultMap.put("isWarn","0");

                        /*
                         * 将机器人巡检图片发给算法再分析
                         * */
                        /*{
                            TCruisePointInstanceDetail details = StaticContextAccessor.getBean(RobotService.class).selectForTask(Long.valueOf(instanceId));//巡检点信息

                            Map<String,Object> picModelPathMap  = redisTemplate.opsForHash().entries("t_sys_param:robotPicModelPath");
                            String picModelPath = picModelPathMap.get("content").toString();

                            if(details.getAnalyseType() != null || "on".equals(details.getIsAi())) {//配置了算法
                                List<TAlgorithmInfo> tAlgorithmInfoList = StaticContextAccessor.getBean(RobotService.class).selectByDeviceMeteId(details.getDeviceMeteId());
                                TStdDeviceMete tStdDevicemete = StaticContextAccessor.getBean(RobotService.class).selectDeviceMete(details.getDeviceMeteId());

                                String recognitionMode = "0";
                                if(tAlgorithmInfoList != null && tAlgorithmInfoList.size()>0){
                                    recognitionMode = "1";
                                }
                                if("on".equals(tStdDevicemete.getIsAi())){
                                    if("1".equals(recognitionMode)){
                                        recognitionMode = "0";
                                    }else {
                                        recognitionMode = "2";
                                    }
                                }
                                tCruiseTaskResultMap.put("recognitionMode",recognitionMode);
                                redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);

                                for (TAlgorithmInfo tAlgorithmInfo : tAlgorithmInfoList) {
                                    Analysis analysis = new Analysis();
                                    analysis.setTaskId(taskId);
                                    analysis.setInstanceId(details.getInstanceId());
                                    analysis.setPicPath(cruiseResultMap.get("originRobotPic"));
                                    analysis.setAnalyseType(tAlgorithmInfo.getAnalyseType());
                                    analysis.setPicModelPath(picModelPath + "/" + inspectionCode);
                                    analysis.setIsAi(tAlgorithmInfo.getIsAi());
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
                                }

                                if("on".equals(tStdDevicemete.getIsAi())){
                                    Analysis analysis = new Analysis();
                                    analysis.setTaskId(taskId);
                                    analysis.setInstanceId(details.getInstanceId());
                                    analysis.setPicPath(cruiseResultMap.get("originRobotPic"));
                                    analysis.setAnalyseType("398");
                                    analysis.setPicModelPath(picModelPath + "/" + inspectionCode);
                                    analysis.setIsAi(0);
                                    List<Analysis> analysisList = new ArrayList<>();
                                    analysisList.add(analysis);
                                    Map<String, List<Analysis>> analysisMap  = new HashMap<>();
                                    analysisMap.put("list",analysisList);
                                    log.info("算法信息：    "+analysisMap);
                                    defect(analysisMap);
                                }
                            }
                        }*/

                        redisTemplate.opsForHash().putAll(str, tCruiseTaskResultMap);
                        //做完一个点给前端推一次webSocket
                        Map<String, Object> jasonMap = new HashMap<>();
                        jasonMap.put("type", "finishedOneInstance");
                        jasonMap.put("taskId", taskId);
                        String json = JSON.toJSONString(jasonMap);
                        log.info("做完一个点-前端推送：" + json);
                        Constant.postUrl(webSocketUrl,json);

                    }

                    /*if ("null".equals(tCruiseTaskResultMap.get("cruiseAbnormal"))){
                        normal = normal + 1;
                        log.info("这次变化的normal是==="+normal);
                    }else if ("249".equals(tCruiseTaskResultMap.get("cruiseAbnormal"))){
                        abnormal = abnormal + 1;
                        log.info("这次变化的abnormal是==="+abnormal);
                    }*/
                }
                log.info("准备更新的abnormal是：" + abnormal + ",准备更新的normal是: "+normal);

                Map<String, String> mapForAbnormal = new HashMap<>();
                mapForAbnormal.put("abnormal", abnormal.toString());
                mapForAbnormal.put("normal", normal.toString());
                //更新异常点缓存的数据
                redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);

                //统计机器人返回任务结果的大小
                List<String> resultList = new ArrayList<>();
                Set<String> cruiseKey = redisScan("t_cruise_task_result:" + taskId);
                for (String key : cruiseKey) {
                    Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                    if (redisInfoMap.get("cruiseResult").equals("246") || redisInfoMap.get("cruiseResult").equals("247")){
                        resultList.add(redisInfoMap.get("instanceId"));
                    }
                }
                log.info("机器人返回任务结果的大小===="+resultList.size());

                //统计巡视主机下发给机器人的巡检点大小
                Map<String, String> redisInfoMap2 = redisTemplate.opsForHash().entries("RobotTaskStatus:"+cruiseResultMap.get("robotCode")+":"+taskId);
                String instanceList = redisInfoMap2.get("instanceIdList");
                instanceList = instanceList.replaceAll("\\[","").replaceAll("]","");
                String[] instanceIdArray = instanceList.split(", ");
                List<String> allInstanceIdList = new ArrayList<>();
                for (String i : instanceIdArray){
                    allInstanceIdList.add(i);
                }
                log.info("巡视主机下发给机器人的巡检点大小===="+allInstanceIdList.size());

                if(allInstanceIdList.size() == resultList.size() ) {
                    log.info("Task Finished......");

                    List<Long> instanceIDList = Constant.flagMap.get(taskId);//已经做过的点
                    log.info("已经做过的巡视点====" + instanceIDList);

                    //删除已经做过的点
                    if (instanceIDList != null && !instanceIDList.isEmpty()){
                        for (Long instanceId : instanceIDList){
                            allInstanceIdList.remove(instanceId.toString());
                        }
                    }
                    log.info("删除已经做过的巡视点后==="+allInstanceIdList);

                    List<Long> isFinishedInstanceList = StaticContextAccessor.getBean(RobotService.class).selectInstanceForTaskGoOn(taskId);//已经入库的点
                    log.info("已经入库的巡视点==="+isFinishedInstanceList);

                    //删除已经入库的点
                    if (isFinishedInstanceList != null && !isFinishedInstanceList.isEmpty()) {
                        for (Long instanceIdInTable : isFinishedInstanceList) {
                            allInstanceIdList.remove(instanceIdInTable.toString());
                        }
                    }
                    log.info("删除已经入库的巡视点后==="+allInstanceIdList);
                    log.info("准备遍历的点是==="+allInstanceIdList);

                    for (String instanceId : allInstanceIdList) {
                        Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId);
                        //缓存中该巡检点有结果
                        if (!redisInfoMap.get("resultNum").equals("设备检修中")){
                            if (redisInfoMap.get("cruiseResult").equals("246") || redisInfoMap.get("cruiseResult").equals("247")) {
                                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail()
                                        .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
                                        .setTaskResultId(redisInfoMap.get("taskResultId"))
                                        .setInstanceId(Long.valueOf(redisInfoMap.get("instanceId")))
                                        .setInstanceName(redisInfoMap.get("instanceName"))
                                        .setCruiseTime(sdf.parse(redisInfoMap.get("cruiseTime")))
                                        .setEndTime(sdf.parse(redisInfoMap.get("endTime")))
                                        .setDeviceId(Long.valueOf(redisInfoMap.get("deviceId")))
                                        .setDeviceName(redisInfoMap.get("deviceName"))
                                        .setCruiseStatus(252)
                                        .setRemark(redisInfoMap.get("remark"));
                                tCTRDList.add(tCruiseTaskResultDetail);
                                cruiseResultIdList.add(redisInfoMap.get("cruiseResultId"));

                                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult()
                                        .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
                                        .setCruiseId(Long.valueOf(redisInfoMap.get("cruiseId")))
                                        .setCruiseName(redisInfoMap.get("cruiseName"))
                                        .setCruiseType(228)
                                        .setResultNum(redisInfoMap.get("resultNum"))
                                        .setModifyNum(redisInfoMap.get("modifyNum"))
                                        .setPicpath(redisInfoMap.get("picpath"))
    //待完善                             .setPicPathAnl(redisInfoMap.get("picPathAnl"))
                                        .setOrigpic(redisInfoMap.get("origpic"))
    //待完善                             .setOrigPicAnl(redisInfoMap.get("origPicAnl"))
                                        .setEvaluationState(257)
                                        .setCreatetime(sdf.parse(redisInfoMap.get("cruiseTime")))
                                        .setIsWarn(Integer.valueOf(redisInfoMap.get("isWarn")))
                                        .setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")));
                                if (!"null".equals(redisInfoMap.get("cruiseAbnormal"))) {
                                    tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(redisInfoMap.get("cruiseAbnormal")));
                                } else {
                                    tCruiseDataResult.setCruiseAbnormal(null);
                                }
                                if (!"null".equals(redisInfoMap.get("remark"))){
                                    tCruiseDataResult.setRemark(redisInfoMap.get("remark"));
                                }else {
                                    tCruiseDataResult.setRemark(null);
                                }
                                if (!"null".equals(redisInfoMap.get("resultPic"))) {
                                    tCruiseDataResult.setResultPic(redisInfoMap.get("resultPic"));
                                } else {
                                    tCruiseDataResult.setResultPic(null);
                                }
                                tCruiseDataResult.setFirName("f");
                                tCDRList.add(tCruiseDataResult);
                            }
                        }

                    }

                    log.info("tCTRDList的内容是===" + tCTRDList+",大小size是: "+tCTRDList.size());
                    log.info("tCDRList的内容是===" + tCDRList+",大小size是: "+tCDRList.size());

                    int res1 = 0;
                    int res2 = 0;
                    if (tCTRDList != null && !tCTRDList.isEmpty()){
                        res1 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseTaskResultDetail(tCTRDList);
                    }
                    if (tCDRList != null && !tCDRList.isEmpty()){
                        res2 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseDataResult(tCDRList);
                        log.info("准备传其他服务的cruiseResultIdList==="+cruiseResultIdList);
                        StaticContextAccessor.getBean(RobotService.class).otherServer(cruiseResultIdList);
                    }
                    log.info("插tCTRD的条数: "+res1+",插tCDR的条数: "+res2);

                    //将公共类的instanceIdList清空
                    if (Constant.flagMap.get(taskId) != null && !Constant.flagMap.get(taskId).isEmpty()){
                        log.info("进来了？？？");
                        for (Long instancedId : Constant.flagMap.get(taskId)){
                            allInstanceIdList.remove(instancedId.toString());
                        }
                    }
                    /*
                    判断缓存中的异常点，如果机器人任务是最后执行，则更新缓存并更新表
                    */
                    if (abnormal + normal == totalCheckPoint){
                        log.info("机器人巡检点是最后一个点");
                        Thread.sleep(15000);

                        TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult()
                                .setTaskId(taskId)
                                .setTaskName(tCruiseTaskResultMap.get("taskName"))
                                .setTaskAlarm(0)
                                .setTaskAbnormal(abnormal)
                                .setRunExecute(String.valueOf(tCruiseTask.getIfRun()))
                                .setCruiseTaskTime(sdf.parse(tCruiseTaskResultMap.get("cruiseTaskTime")))
                                .setTaskResultId(tCruiseTaskResultMap.get("taskResultId"));
                        log.info("tCruiseTaskResult的内容是==="+tCruiseTaskResult);
                        StaticContextAccessor.getBean(RobotService.class).insertTCruiseTaskResult(tCruiseTaskResult);

                        Integer taskWait = totalCheckPoint - normal - abnormal;
                        log.info("taskWait的值是=="+ taskWait);
                        tCruiseResult.setTaskWait(taskWait);
                        tCruiseResult.setCState(240);
                        tCruiseResult.setTaskCode(taskId);
                        tCruiseResult.setCreateTime(sdf.parse(tCruiseTaskResultMap.get("cruiseTaskTime")));
                        log.info("tCruiseResult的内容是==="+tCruiseResult);
                        StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);

                        // webSocket通知前端调用巡视监控的接口（任务完成）
                        Map<String, Object> jasonMap = new HashMap<>();
                        jasonMap.put("type", "lastOneInstance");
                        jasonMap.put("taskId",taskId);
                        String json = JSON.toJSONString(jasonMap);
                        log.info("最后一个点-前端推送：" + json);
                        Constant.postUrl(webSocketUrl,json);

                        for (TCruiseTaskResultDetail tctrd : tCTRDList){
                            int resNum = StaticContextAccessor.getBean(RobotService.class).selectIsWarn(tctrd.getInstanceId(),taskId);
                            if (resNum > 0){
                                StaticContextAccessor.getBean(RobotService.class).updateIsWarn(tctrd.getCruiseResultId());
                            }
                        }

                    }else{
                        log.info("机器人巡检点不是最后一个点");
                        Integer taskWait = totalCheckPoint - normal - abnormal;
                        log.info("taskWait的值是=="+ taskWait);
                        tCruiseResult.setTaskWait(taskWait);
                        tCruiseResult.setCState(239);
                        tCruiseResult.setTaskCode(taskId);
                        log.info("tCruiseResult的内容是==="+tCruiseResult);
                        StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
                    }
                }
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

    //Redis数据库批量查询Key值游标
    public Set<String> redisScan(String key) {
        return (Set<String>) redisTemplate.execute((RedisCallback<Set<String>>) connection -> {
            Set<String> keys = Sets.newHashSet();

            JedisCommands commands = (JedisCommands) connection.getNativeConnection();
            MultiKeyCommands multiKeyCommands = (MultiKeyCommands) commands;

            ScanParams scanParams = new ScanParams();
            scanParams.match("*" + key + "*");
            scanParams.count(1000);
            ScanResult<String> scan = multiKeyCommands.scan("0", scanParams);
            while (null != scan.getStringCursor()) {
                keys.addAll(scan.getResult());
                if (!StringUtils.equals("0", scan.getStringCursor())) {
                    scan = multiKeyCommands.scan(scan.getStringCursor(), scanParams);
                    continue;
                } else {
                    break;
                }
            }

            return keys;
        });
    }
}
