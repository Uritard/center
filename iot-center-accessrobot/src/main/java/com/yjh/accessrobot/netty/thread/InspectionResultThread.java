package com.yjh.accessrobot.netty.thread;

import org.apache.commons.lang3.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.logs.SpringBeanUtils;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.module.command.service.RobotService;
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
 * 机器人巡视结果处理线程
 */
@lombok.extern.slf4j.Slf4j
public class CruiseResultDealThread implements Runnable{

    private static final String DATE_TIME_FORMAT_TPL = "yyyy-MM-dd HH:mm:ss";
    private SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_FORMAT_TPL);
    /**
     * 算法接口url
     */
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    /**
     * 缺陷接口url
     */
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
            log.info("开始处理巡检结果并对其标准化 >>>>>>> cruiseResultMap==={}",cruiseResultMap);
            String taskId = cruiseResultMap.get("taskCode");

            String robotCode = cruiseResultMap.getOrDefault("robotCode", "");
            if (StringUtils.isEmpty(robotCode)) {
                log.error("机器人编码为空");
                throw new RuntimeException("机器人编码为空");
            }

            TCruiseTask tCruiseTask = StaticContextAccessor.getBean(RobotService.class).selectTCruiseTask(taskId);
            Integer taskType = tCruiseTask.getTaskType();
            log.info("taskId是: {}的任务类型是:{}",taskId,taskType);
            TCruiseResult tCruiseResult = StaticContextAccessor.getBean(RobotService.class).selectTaskResultId(taskId);
            //机器人本体任务和巡视主机下发的任务分情况处理
            boolean isSelfTask = Objects.nonNull(taskType) && Objects.equals(271,taskType);

            if (isSelfTask) {
                robotSelfTask(robotCode, taskId, tCruiseResult);
            }else {
                platformTask(robotCode, taskId,tCruiseResult);
            }
        } catch (Exception e) {
            log.error("巡检结果处理失败", e.getMessage());
            throw new RuntimeException("巡检结果处理失败");
        }
    }

    /**
     * 机器人本体任务数据处理
     * @param robotCode 机器人唯一标识
     * @param taskId 任务id
     * @param tCruiseResult 任务结果初始信息
     * @return void
     */
    private void robotSelfTask(String robotCode,
                               String taskId,
                               TCruiseResult tCruiseResult) throws Exception{
        Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:"+robotCode+ ":" +taskId);
        Map<String,String> tCruiseTaskResultMap = new HashMap<>();
//        List<Long> instanceIdList = new ArrayList<>();

        //遍历在下任务时提前组装好的巡检点信息
        for (String key : robotInfoKeys) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
            if (Objects.equals(robotCode,redisInfoMap.get("robotCode"))
                    && Objects.equals(taskId,redisInfoMap.get("taskId"))
                    && Objects.equals(cruiseResultMap.get("deviceId"),redisInfoMap.get("inspectionCode"))) {
                String instanceId = redisInfoMap.get("instanceId");
//                instanceIdList.add(Long.valueOf(instanceId));

                tCruiseTaskResultMap.put("taskResultId", tCruiseResult.getTaskResultId());
                tCruiseTaskResultMap.put("taskId",redisInfoMap.get("taskId"));
                tCruiseTaskResultMap.put("taskName", tCruiseResult.getTaskName());
                tCruiseTaskResultMap.put("cruiseStatus","252");
                tCruiseTaskResultMap.put("instanceName",redisInfoMap.get("inspectionName"));
                tCruiseTaskResultMap.put("deviceName",redisInfoMap.get("deviceName"));
                tCruiseTaskResultMap.put("cruiseTime",cruiseResultMap.get("time"));

                if (StringUtils.isNotEmpty(cruiseResultMap.get("value"))){
                    tCruiseTaskResultMap.put("resultNum",cruiseResultMap.get("value"));
                    tCruiseTaskResultMap.put("cruiseResult","246");
                    tCruiseTaskResultMap.put("cruiseAbnormal","null");
                }else {
                    tCruiseTaskResultMap.put("resultNum","--");
                    if ("3".equals(cruiseResultMap.get("fileType"))){
                        tCruiseTaskResultMap.put("cruiseResult","246");
                        tCruiseTaskResultMap.put("cruiseAbnormal","null");
                    }else {
                        tCruiseTaskResultMap.put("cruiseResult","247");
                        tCruiseTaskResultMap.put("cruiseAbnormal","249");
                    }
                }
                tCruiseTaskResultMap.put("evaluationState","257");
                tCruiseTaskResultMap.put("createtime",cruiseResultMap.get("time"));
                redisTemplate.opsForHash().putAll("t_cruise_task_result:" + taskId + ":" + instanceId, tCruiseTaskResultMap);

                //做完一个点给前端推一次webSocket
                Map<String, Object> jasonMap = new HashMap<>();
                jasonMap.put("type", "finishedOneInstance");
                jasonMap.put("taskId", taskId);
                String json = JSON.toJSONString(jasonMap);
                Constant.postUrl(webSocketUrl,json);
            }
        }
        //获取机器人本体任务的状态并更新
        Map<String, Object> redisInfoMap = redisTemplate.opsForHash().entries("RobotTaskStatus:"+robotCode+":"+taskId);
        Integer taskState = Integer.valueOf(redisInfoMap.get("taskState").toString());
        Integer cState = null;
        switch (taskState){
            //已执行
            case 1: cState = 240;break;
            //正在执行
            case 2: cState = 239;break;
            //任务暂停
            case 3: cState = 241;break;
            //任务终止
            case 4: cState = 242;break;
            //任务未开始
            case 5: cState = 238;break;
            //任务超期
            case 6: cState = 244;break;
            default:break;
        }
        Set<String> cruiseKey = redisScan("t_cruise_task_result:" + taskId);
        if (robotInfoKeys.size() == cruiseKey.size()){
            cState = 240;
            TimeUnit.SECONDS.sleep(2);
            /*log.info("本体做完的点==="+instanceIdList);
            StaticContextAccessor.getBean(RobotService.class).deleteInstanceId(instanceIdList);*/
        }
        tCruiseResult.setCState(cState);
        tCruiseResult.setExecuteTime(sdf.parse(tCruiseTaskResultMap.get("createtime")));
        log.info("tCruiseResult的内容是==="+tCruiseResult);
        StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
    }
    /**
     * 巡视主机下发给机器人的任务数据处理
     * @param robotCode 机器人唯一标识
     * @param taskId 任务id
     * @param tCruiseResult 任务结果初始信息
     * @return void
     */
    private void platformTask(String robotCode,
                              String taskId,
                              TCruiseResult tCruiseResult) throws Exception{
        String strForCountAbnormal = "countForAbnormal:" + taskId;
        Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
        Integer totalNum = Integer.valueOf(abnormalCount.get("all").toString());
        Integer abnormalNum = Integer.valueOf(abnormalCount.get("abnormal").toString()) ;
        Integer normalNum  = Integer.valueOf(abnormalCount.get("normal").toString()) ;
        log.info("taskId为{}的总检测点数是==={}, 异常点数是==={}, 正常点数是===",taskId,totalNum,abnormalNum,normalNum);
        Integer abnormal = abnormalNum;
        Integer normal = normalNum;

        Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:"+robotCode+ ":" +taskId);
        Map<String,String> tCruiseTaskResultMap = new HashMap<>();
        //上报结果的巡视点,即已经做过的点
        List<Long> instanceIdList = Optional.ofNullable(Constant.flagMap.get(taskId)).orElse(new ArrayList<>());
        //遍历在下任务时提前组装好的巡检点信息
        for (String key : robotInfoKeys){
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
            if (Objects.equals(robotCode,redisInfoMap.get("robotCode"))
                    && Objects.equals(taskId,redisInfoMap.get("taskId"))
                    && Objects.equals(cruiseResultMap.get("deviceId"),redisInfoMap.get("inspectionCode"))) {
                String instanceId = redisInfoMap.get("instanceId");
                instanceIdList.add(Long.valueOf(instanceId));

                tCruiseTaskResultMap.put("cruiseTime",cruiseResultMap.get("time"));
                tCruiseTaskResultMap.put("cruiseTaskTime",redisInfoMap.get("cruiseTime"));
                tCruiseTaskResultMap.put("taskResultId",tCruiseResult.getTaskResultId());
                tCruiseTaskResultMap.put("taskName",tCruiseResult.getTaskName());
                tCruiseTaskResultMap.put("endTime",cruiseResultMap.get("time"));
                tCruiseTaskResultMap.put("cruiseStatus","252");
                if (Objects.equals("1",cruiseResultMap.get("fileType"))){
                    tCruiseTaskResultMap.put("resultPic",cruiseResultMap.get("resultPic"));
                }
                if (StringUtils.isNotEmpty(cruiseResultMap.get("value"))){
                    tCruiseTaskResultMap.put("resultNum",cruiseResultMap.get("value"));
                    tCruiseTaskResultMap.put("cruiseResult","246");
                    tCruiseTaskResultMap.put("cruiseAbnormal","null");
                    normal = normal + 1;
                    log.info("taskId为{}的该点结果正常,这次变化的normal是==={}",taskId,normal);
                }else {
                    //value无值且resultNum为--，若结果非音频文件，则为异常情况
                    tCruiseTaskResultMap.put("resultNum","--");
                    if ("3".equals(cruiseResultMap.get("fileType"))){
                        tCruiseTaskResultMap.put("cruiseResult","246");
                        tCruiseTaskResultMap.put("cruiseAbnormal","null");
                        normal = normal + 1;
                        log.info("taskId为{}的该点结果正常,这次变化的normal是==={}",taskId,normal);
                    }else {
                        tCruiseTaskResultMap.put("cruiseResult","247");
                        tCruiseTaskResultMap.put("cruiseAbnormal","249");
                        abnormal = abnormal + 1;
                        log.info("taskId为{}的该点结果异常,这次变化的abnormal是===",taskId,normal);
                    }
                }
                tCruiseTaskResultMap.put("picpath",cruiseResultMap.get("relativePath"));
                if (cruiseResultMap.containsKey("absolutePath")){
                    tCruiseTaskResultMap.put("origpic",cruiseResultMap.get("absolutePath"));
                }
                tCruiseTaskResultMap.put("evaluationState","257");
                tCruiseTaskResultMap.put("createtime",cruiseResultMap.get("time"));
                tCruiseTaskResultMap.put("isWarn","0");
                redisTemplate.opsForHash().putAll("t_cruise_task_result:" + taskId + ":" + instanceId, tCruiseTaskResultMap);

                /*reanalysisResult(instanceId,tCruiseTaskResultMap,taskId,inspectionCode);*/
                Map<String, Object> jasonMap = new HashMap<>();
                jasonMap.put("type", "finishedOneInstance");
                jasonMap.put("taskId", taskId);
                String json = JSON.toJSONString(jasonMap);
                log.info("做完一个点-前端推送：" + json);
                Constant.postUrl(webSocketUrl,json);

                Constant.flagMap.put(taskId, instanceIdList);
            }
        }
        log.info("taskId为{}的任务,准备更新的abnormal是:{},准备更新的normal是:{}" ,taskId,abnormal,normal);
        Map<String, String> mapForAbnormal = new HashMap<>();
        mapForAbnormal.put("abnormal", abnormal.toString());
        mapForAbnormal.put("normal", normal.toString());
        redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);

        taskIsFinished(taskId,robotCode,totalNum,abnormal,normal,tCruiseTaskResultMap,tCruiseResult);
    }

    /**
     * 判断机器人是否完成任务,依据为返回结果大小与下发点数大小是否相等相等
     * @param taskId 任务id
     * @param robotCode 机器人唯一标识
     * @param totalNum 总巡检点数
     * @param abnormal 异常巡检点数
     * @param normal 正常巡检点数
     * @param tCruiseTaskResultMap 机器人巡视结果map
     * @param tCruiseResult 任务结果表中已有的数据
     * @return void
     */
    private void taskIsFinished(String taskId,
                                String robotCode,
                                Integer totalNum,
                                Integer abnormal,
                                Integer normal,
                                Map<String,String> tCruiseTaskResultMap,
                                TCruiseResult tCruiseResult) throws Exception{
        //统计机器人返回任务结果的大小
        List<String> resultList = new ArrayList<>();
        Set<String> cruiseKey = redisScan("t_cruise_task_result:" + taskId);
        for (String key : cruiseKey) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
            Boolean flag = StringUtils.equals("228",redisInfoMap.get("cruiseType")) && (StringUtils.equals("246",redisInfoMap.get("cruiseResult"))|| StringUtils.equals("247",redisInfoMap.get("cruiseResult")));
            if (Boolean.TRUE.equals(flag)){
                resultList.add(redisInfoMap.get("instanceId"));
            }
        }
        log.info("机器人任务为{}返回结果个数===={}",taskId,resultList.size());

        //统计巡视主机下发给机器人的巡检点大小
        Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("RobotTaskStatus:"+robotCode+":"+taskId);
        String instanceList = redisInfoMap.get("instanceIdList");
        instanceList = instanceList.replaceAll("\\[","").replaceAll("]","");
        String[] instanceIdArray = instanceList.split(", ");
        List<String> allInstanceIdList = new ArrayList<>();
        for (String i : instanceIdArray){
            allInstanceIdList.add(i);
        }
        log.info("巡视主机下发给机器人任务为{}的巡检点个数===={}",taskId,allInstanceIdList.size());

        if(allInstanceIdList.size() == resultList.size() ) {
            log.info("-------------------TaskId为{}的任务 Finished-------------------",taskId);
            taskIsFinishedDeal(taskId,totalNum,abnormal,normal,tCruiseTaskResultMap,tCruiseResult);
        }
    }

    /**
     * 任务所有点做完,完成,并且进度为100%的处理
     * @param taskId 任务id
     * @param totalCheckPoint 总巡检点数
     * @param abnormal 异常巡检点数
     * @param normal 正常巡检点数
     * @param tCruiseTaskResultMap 机器人巡视结果map
     * @param tCruiseResult 任务结果表中已有的数据
     * @return void
     */
    public void taskIsFinishedDeal(String taskId,
                                   Integer totalCheckPoint,
                                   Integer abnormal ,
                                   Integer normal,
                                   Map<String,String> tCruiseTaskResultMap,
                                   TCruiseResult tCruiseResult) throws Exception{
        List<TCruiseDataResult> tCDRList = new ArrayList<>();
        List<TCruiseTaskResultDetail> tCTRDList = new ArrayList<>();
        List<String> cruiseResultIdList = new ArrayList<>();

        List<Long> instanceIdDoneList = Constant.flagMap.get(taskId);
        log.info("任务为{}已经做过的巡视点===={}",taskId,instanceIdDoneList);
        log.info("准备遍历的点是==={}", instanceIdDoneList);

        for (Long instanceId : instanceIdDoneList) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId.toString());
            log.info("redisInfoMap的数据是{}",redisInfoMap);
            //缓存中该巡检点有结果
            if (!StringUtils.equals("设备检修中",redisInfoMap.get("resultNum"))){
                if (StringUtils.equals("246",redisInfoMap.get("cruiseResult"))
                        || StringUtils.equals("247",redisInfoMap.get("cruiseResult"))) {
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
                            //待完善.setPicPathAnl(redisInfoMap.get("picPathAnl"))
                            .setOrigpic(redisInfoMap.get("origpic"))
                            //待完善.setOrigPicAnl(redisInfoMap.get("origPicAnl"))
                            .setEvaluationState(257)
                            .setCreatetime(sdf.parse(redisInfoMap.get("cruiseTime")))
                            .setIsWarn(0)
                            .setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")));
                    if (!"null".equals(redisInfoMap.get("cruiseAbnormal"))) {
                        tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(redisInfoMap.get("cruiseAbnormal")));
                    } else {
                        tCruiseDataResult.setCruiseAbnormal(null);
                    }
                    if (!StringUtils.equals("null",redisInfoMap.get("remark"))){
                        tCruiseDataResult.setRemark(redisInfoMap.get("remark"));
                    }else {
                        tCruiseDataResult.setRemark(null);
                    }
                    if (!StringUtils.equals("null",redisInfoMap.get("resultPic"))) {
                        tCruiseDataResult.setResultPic(redisInfoMap.get("resultPic"));
                    } else {
                        tCruiseDataResult.setResultPic(null);
                    }
                    tCruiseDataResult.setFirName("f");
                    tCDRList.add(tCruiseDataResult);
                }
            }

        }
        log.info("任务{}的tCTRDList大小是:{},tCDRList大小是: {}",taskId,tCTRDList.size(),tCDRList.size());

        int res1 = 0;
        int res2 = 0;
        if (tCTRDList != null && !tCTRDList.isEmpty()){
            res1 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseTaskResultDetail(tCTRDList);
        }
        if (tCDRList != null && !tCDRList.isEmpty()){
            res2 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseDataResult(tCDRList);
            log.info("准备传其他服务的cruiseResultIdList==="+cruiseResultIdList);
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_FINISH, cruiseResultIdList, Result.class);
        }
        log.info("插tCTRD的条数:{},插tCDR的条数:{}",res1,res2);

        //将已经做过的巡视点Map清空
        if (StringUtils.isNotEmpty(Constant.flagMap.get(taskId).toString())){
            log.info("将公共类的instanceIdList清空");
            Constant.flagMap = new HashMap<>();
        }

        //判断缓存中的异常点，如果机器人任务是最后执行，则更新缓存并更新表
        if (abnormal + normal == totalCheckPoint){
            log.info("机器人巡检点是最后一个点");
            Thread.sleep(15000);

            TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult()
                    .setTaskId(taskId)
                    .setTaskName(tCruiseTaskResultMap.get("taskName"))
                    .setTaskAlarm(0)
                    .setTaskAbnormal(abnormal)
                    .setRunExecute(tCruiseTaskResultMap.get("if_run"))
                    .setCruiseTaskTime(sdf.parse(tCruiseTaskResultMap.get("cruiseTaskTime")))
                    .setTaskResultId(tCruiseTaskResultMap.get("taskResultId"));
            log.info("任务为{}的tCruiseTaskResult内容是==={}",taskId,tCruiseTaskResult);
            StaticContextAccessor.getBean(RobotService.class).insertTCruiseTaskResult(tCruiseTaskResult);

            Integer taskWait = totalCheckPoint - normal - abnormal;
            tCruiseResult.setTaskWait(taskWait);
            tCruiseResult.setCState(240);
            tCruiseResult.setTaskCode(taskId);
            tCruiseResult.setCreateTime(sdf.parse(tCruiseTaskResultMap.get("cruiseTaskTime")));
            log.info("任务为{}的tCruiseResult内容是==={}",taskId,tCruiseResult);
            //更新TCR表
            int res = StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
            log.info("更新TCR的条数====" + res);

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
            tCruiseResult.setTaskWait(taskWait);
            tCruiseResult.setCState(239);
            tCruiseResult.setTaskCode(taskId);
            log.info("任务为{}的tCruiseResult内容是==={}",taskId,tCruiseResult);
            StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
        }
    }

    /**
     * 对机器人的巡检图片再进行算法分析
     * @param instanceId 巡视点id
     * @param tCruiseTaskResultMap  巡视点结果集
     * @param taskId 任务id
     * @param inspectionCode 机器人测点id
     * @return void
     */
    private void reanalysisResult(Long instanceId,Map<String,String> tCruiseTaskResultMap,String taskId,String inspectionCode){
        //巡检点信息
        TCruisePointInstanceDetail details = StaticContextAccessor.getBean(RobotService.class).selectForTask(instanceId);

        Map<String,Object> picModelPathMap  = redisTemplate.opsForHash().entries("t_sys_param:robotPicModelPath");
        String picModelPath = picModelPathMap.get("content").toString();

        //配置了算法
        if(details.getAnalyseType() != null || "on".equals(details.getIsAi())) {
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

            String str = "t_cruise_task_result:" + taskId + ":" + instanceId;
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
                //0-缺陷 1-表记
                if(tAlgorithmInfo.getIsAi() == 1){
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
    }

    /**
     * 表记分析
     * */
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
    /**
     * 缺陷分析
     * */
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

    /**
     * Redis数据库批量查询Key值游标
     */
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
