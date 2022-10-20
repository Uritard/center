package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.StaticContextAccessor;
import com.yjh.platform.module.patrol.entity.TCruisePointInstanceDetail;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskResult;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author YC
 * @date 2020/12/8 9:58
 * 机器人巡视结果处理线程
 */
@lombok.extern.slf4j.Slf4j
public class InspectionResultThread implements Runnable{

    private RedisTemplate redisTemplate;
    private Boolean changeTaskStatus;
    private RobotPatrolTaskResult robotPatrolTaskResult;
    private Map<String, String> infoMap;
    private UPatrolTaskService uPatrolTaskService;

    public InspectionResultThread(RobotPatrolTaskResult robotPatrolTaskResult, Map<String, String> infoMap, RedisTemplate redisTemplate, boolean changeTaskStatus){
        this.robotPatrolTaskResult = robotPatrolTaskResult;
        this.infoMap = infoMap;
        this.redisTemplate = redisTemplate;
        this.changeTaskStatus = changeTaskStatus;
        this.uPatrolTaskService = StaticContextAccessor.getBean(UPatrolTaskService.class);
    }

    @Override
    public void run(){
        try {
            log.info("开始处理巡检结果并对其标准化 >>>>>>> robotPatrolTaskResult==={}", robotPatrolTaskResult);
            String taskId = infoMap.get("taskId");
            String instanceId;
            String robotCode = robotPatrolTaskResult.getRobotCode();

            Set<String> robotInfoKeys = redisScan("Robot_SPAndIN_Info:" + robotCode + ":" + taskId);
            // 上报结果的巡视点,即已经做过的点
            List<Long> instanceIdList = Optional.ofNullable(Constant.flagMap.get(taskId)).orElse(new ArrayList<>());
            // 遍历在下任务时提前组装好的巡检点信息
            for (String key : robotInfoKeys){
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);

                String redisKeyName = "t_cruise_task_result:" + taskId + ":";
                if (Objects.equals(robotCode, redisInfoMap.get("robotCode"))
                        && Objects.equals(taskId, redisInfoMap.get("taskId"))
                        && Objects.equals(robotPatrolTaskResult.getDeviceId(), redisInfoMap.get("inspectionCode"))) {
                    instanceId = redisInfoMap.get("instanceId");
                    instanceIdList.add(Long.valueOf(instanceId));
                    Constant.flagMap.put(taskId, instanceIdList);

                    Map<String, Object> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName + instanceId);
                    tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
                    tCruiseTaskResultMap.put("cruiseStatus", "252");

                    if (StringUtils.isNotEmpty(robotPatrolTaskResult.getValue())) {
                        tCruiseTaskResultMap.put("resultNum", robotPatrolTaskResult.getValue());
                        tCruiseTaskResultMap.put("cruiseResult", "246");
                        tCruiseTaskResultMap.put("cruiseAbnormal", "null");
                        log.info("taskId为{},instanceId为{}的该点结果正常", taskId, instanceId);
                    }else {
                        // value无值且resultNum为--，若结果非音频文件，则为异常情况
                        tCruiseTaskResultMap.put("resultNum", "--");
                        if ("3".equals(robotPatrolTaskResult.getFileType())){
                            tCruiseTaskResultMap.put("cruiseResult", "246");
                            tCruiseTaskResultMap.put("cruiseAbnormal","null");
                            log.info("taskId为{},instanceId为{}的该点结果正常", taskId, instanceId);
                        }else {
                            tCruiseTaskResultMap.put("cruiseResult","247");
                            tCruiseTaskResultMap.put("cruiseAbnormal","249");

                            log.info("taskId为{},instanceId为{}的该点结果异常", taskId, instanceId);
                        }
                    }
                    tCruiseTaskResultMap.put("picpath", infoMap.get("relativePath"));
                    tCruiseTaskResultMap.put("origpic", infoMap.containsKey("absolutePath") ? infoMap.get("absolutePath") : "");
                    tCruiseTaskResultMap.put("evaluationState", "257");
                    tCruiseTaskResultMap.put("isWarn", "0");
                    tCruiseTaskResultMap.put("recognitionType", robotPatrolTaskResult.getRecognitionType());
                    tCruiseTaskResultMap.put("fileType", robotPatrolTaskResult.getFileType());
                    tCruiseTaskResultMap.put("rectangle", robotPatrolTaskResult.getRectangle());
                    tCruiseTaskResultMap.put("confidence", "");

                    redisTemplate.opsForHash().putAll("t_cruise_task_result:" + taskId + ":" + instanceId, tCruiseTaskResultMap);
                    log.info("taskId为{}巡视点instanceId为{}的点位已更新redis", taskId, instanceId);

                    // 区分是模拟工具上报巡视结果还是真实的
                    taskResultTypeHandler(infoMap.get("absolutePath"), taskId, instanceId);

                    boolean isSelfTask = true;
                    if (Boolean.TRUE.equals(isSelfTask)){
                        // 如果是站端本体任务,需更新任务状态
                        standTaskDealHandler(taskId, robotCode, robotInfoKeys);
                    }else {
                        // 巡视主机下发的任务
                        uPatrolTaskService.patrolTaskResultHandler(taskId, Long.valueOf(instanceId));
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void standTaskDealHandler(String taskId, String robotCode, Set<String> robotInfoKeys) throws InterruptedException {
        Map<String, Object> redisInfoMap = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskId);
        Integer taskState = Integer.valueOf(String.valueOf(redisInfoMap.get("taskState")));
        Integer cState = null;
        switch (taskState){
            // 已执行
            case 1: cState = 240;break;
            // 正在执行
            case 2: cState = 239;break;
            // 任务暂停
            case 3: cState = 241;break;
            // 任务终止
            case 4: cState = 242;break;
            // 任务未开始
            case 5: cState = 238;break;
            // 任务超期
            case 6: cState = 244;break;
            default:break;
        }
        Set<String> cruiseKey = redisScan("t_cruise_task_result:" + taskId);
        if (robotInfoKeys.size() == cruiseKey.size()){
            cState = 240;
            TimeUnit.SECONDS.sleep(2);
        }
//                tCruiseResult.setCState(cState);
//                tCruiseResult.setExecuteTime(DateTimeUtil.parse(tCruiseTaskResultMap.get("createtime")));
//                log.info("tCruiseResult的内容是==={}", tCruiseResult);
//                StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
    }

    /**
     * 区分是模拟工具上报巡视结果还是真实的
     *
     * @param originPath 巡视结果文件全路径
     * @param taskId 任务id
     * @param instanceId 巡视点id
     */
    private void taskResultTypeHandler(String originPath, String taskId, String instanceId) {
        try {
            Integer robotType = uPatrolTaskService.selectRobotType(robotPatrolTaskResult.getRobotCode());
            boolean isSimulationTool = StringUtils.isNotEmpty(robotPatrolTaskResult.getFilePath())
                    && StringUtils.isEmpty(robotPatrolTaskResult.getOriginFileResultPath())
                    && StringUtils.isEmpty(robotPatrolTaskResult.getOriginFilePath())
                    // 且是E机器人
                    && Objects.equals(159, robotType);
            if (Boolean.FALSE.equals(isSimulationTool)) {
                // 真实设备上报的巡视结果
                Map<String, String> jasonMap = new HashMap<>(2);
                jasonMap.put("type", "finishedOneInstance");
                jasonMap.put("taskId", taskId);
                String json = JSON.toJSONString(jasonMap);
                log.info("做完一个点-前端推送：" + json);
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);

                Integer flag = uPatrolTaskService.selectIsAlarmByTask(taskId, instanceId);
                if (flag > 0){
                    log.info("taskId为{}巡视点instanceId为{}的点位产生了告警,需要更新图片", taskId, instanceId);
                    uPatrolTaskService.updatePicPath(taskId, instanceId, infoMap.get("relativePath"));
                }
                return;
            }
            //模拟工具上报的巡视结果

            // 复制图片到算法分析指定的路径
            String ftpFileName = originPath.trim().substring(originPath.trim().lastIndexOf("/") + 1);
            String resultImagePath = redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content") + ftpFileName;
            FileUtil.copyFileUsingStream(resultImagePath, resultImagePath);
            // 标定文件
            String picModelPath = redisTemplate.opsForHash().get("t_sys_param:picModelPath", "content") + "/" + "inspectionCode";
            // 测点信息
            TCruisePointInstanceDetail details = uPatrolTaskService.selectForTask(Long.valueOf(instanceId));
            TStdDeviceMete tStdDevicemete =uPatrolTaskService.selectDeviceMete(details.getDeviceMeteId());

            String redisKeyName = "t_cruise_task_result:" + taskId + ":";
            Map<String, Object> tCruiseTaskResultMap = redisTemplate.opsForHash().entries(redisKeyName + instanceId);
            try {
                tCruiseTaskResultMap.put("cruiseTime", robotPatrolTaskResult.getTime());
                tCruiseTaskResultMap.put("cruiseStatus", "253");
                tCruiseTaskResultMap.put("evaluationState", "257");
                tCruiseTaskResultMap.put("isWarn", "0");
                tCruiseTaskResultMap.put("origpic", resultImagePath);
                String picPath = resultImagePath.replace(
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content")),
                        String.valueOf(redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content")));
                tCruiseTaskResultMap.put("picpath", picPath);
            }catch (Exception e){
                log.error("往redis插入值错误：{}", e);
            }

            /*if ( "on".equals(details.getIsAi()) || "on".equals(details.getIsJudge())) {
                // 配置了缺陷算法

                // 测点配置的算法类型
                List<TAlgorithmInfo> tAlgorithmInfoList = StaticContextAccessor.getBean(RobotService.class).selectByDeviceMeteId(details.getDeviceMeteId());

                addRequiredInfo(tAlgorithmInfoList, taskId, instanceId, tStdDevicemete, tCruiseTaskResultMap);
                packageAndInvoke(tAlgorithmInfoList, taskId, instanceId, resultImagePath, picModelPath, tStdDevicemete);
            } else {
                // 即拍照的点位和声音
                Map<String, Integer> map = updatePointStatusNum(taskId, tCruiseTaskResultMap, details);

                // 判断该点是否为最后一个
                processResult(map, taskId, tCruiseResult, tCruiseTaskResultMap, instanceId);
            }*/


        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

//    taskIsFinished(taskId, totalNum, abnormal, normal, tCruiseTaskResultMap, tCruiseResult);

    /**
     * 判断机器人是否完成任务,依据为返回结果大小与下发点数大小是否相等相等
     * @param taskId 任务id
     * @param totalNum 总巡检点数
     * @param abnormal 异常巡检点数
     * @param normal 正常巡检点数
     * @param tCruiseTaskResultMap 机器人巡视结果map
     * @param tCruiseResult 任务结果表中已有的数据
     * @return void
     */
//    private void taskIsFinished(String taskId,
//                                Integer totalNum,
//                                Integer abnormal,
//                                Integer normal,
//                                Map<String,String> tCruiseTaskResultMap,
//                                TCruiseResult tCruiseResult){
//        try {
//            // 统计机器人返回任务结果的大小
//            List<String> resultList = new ArrayList<>();
//            Set<String> cruiseKey = redisScan("t_cruise_task_result:" + taskId);
//            for (String key : cruiseKey) {
//                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
//                Boolean flag = StringUtils.equals("228", redisInfoMap.get("cruiseType")) && (StringUtils.equals("246", redisInfoMap.get("cruiseResult")) || StringUtils.equals("247", redisInfoMap.get("cruiseResult")));
//                if (Boolean.TRUE.equals(flag)){
//                    resultList.add(redisInfoMap.get("instanceId"));
//                }
//            }
//            log.info("机器人任务为{}返回结果个数===={}", taskId, resultList.size());
//
//            // 统计巡视主机下发给机器人的巡检点大小
//            String robotTaskId = taskId;
//            if ("true".equals(redisTemplate.opsForValue().get("RobotTask.taskToRobot"))) {
//                robotTaskId = StaticContextAccessor.getBean(RobotService.class).selectTaskId(taskId);
//            }
//            // 统计巡视主机下发给机器人的巡检点大小
//            List<String> allInstanceIdList = new ArrayList<>();
//            log.info("taskRobotMap {} ", Constant.taskRobotMap.get(taskId));
//            String finalRobotTaskId = robotTaskId;
//            Constant.taskRobotMap.get(taskId).forEach((robotCode, taskStatus) -> {
//                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + finalRobotTaskId);
//                String instanceList = redisInfoMap.get("instanceIdList");
//                instanceList = instanceList.replaceAll("\\[", "").replaceAll("]", "");
//                String[] instanceIdArray = instanceList.split(", ");
//                allInstanceIdList.addAll(Arrays.stream(instanceIdArray).distinct().collect(Collectors.toList()));
//            });
//            log.info("allInstanceIdList {}" , allInstanceIdList);
//            log.info("巡视主机下发给机器人任务为{}的巡检点个数===={}", taskId, allInstanceIdList.size());
//
//            if(Objects.equals(allInstanceIdList.size(), resultList.size())) {
//                log.info("-------------------TaskId为{}的任务Finished-------------------", taskId);
//                completionOfTask(taskId, totalNum, abnormal, normal, tCruiseTaskResultMap, tCruiseResult);
//            }
//        }catch (Exception e){
//            log.error(e.getMessage(), e);
//        }
//    }
//    /**
//     * 任务所有点做完,完成,并且进度为100%的处理
//     * @param taskId 任务id
//     * @param totalCheckPoint 总巡检点数
//     * @param abnormal 异常巡检点数
//     * @param normal 正常巡检点数
//     * @param tCruiseTaskResultMap 机器人巡视结果map
//     * @param tCruiseResult 任务结果表中已有的数据
//     * @return void
//     */
//    public void completionOfTask(String taskId,
//                                   Integer totalCheckPoint,
//                                   Integer abnormal ,
//                                   Integer normal,
//                                   Map<String,String> tCruiseTaskResultMap,
//                                   TCruiseResult tCruiseResult){
//        List<TCruiseDataResult> tcdrList = new ArrayList<>();
//        List<TCruiseTaskResultDetail> tctrdList = new ArrayList<>();
//        List<String> cruiseResultIdList = new ArrayList<>();
//
//        List<Long> instanceIdDoneList = Constant.flagMap.get(taskId);
//        log.info("任务为{}已经做过的巡视点===={}", taskId, instanceIdDoneList);
//
//        List<Long> inDataBaseInstanceList = StaticContextAccessor.getBean(RobotService.class).selectInstanceForTaskGoOn(taskId);
//        log.info("已经入库的巡视点==={}", inDataBaseInstanceList);
//        if (CollectionUtils.isNotEmpty(instanceIdDoneList)) {
//            for (Long instanceIdInTable : inDataBaseInstanceList) {
//                instanceIdDoneList.remove(instanceIdInTable.toString());
//            }
//        }
//        log.info("删除已经入库的巡视点后==={}", instanceIdDoneList);
//
//        for (Long instanceId : instanceIdDoneList) {
//            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId.toString());
//            // 缓存中该巡检点有结果
//            boolean conditionRes = !StringUtils.equals("设备检修中", redisInfoMap.get("resultNum"))
//                    && (StringUtils.equals("246", redisInfoMap.get("cruiseResult"))
//                    || StringUtils.equals("247", redisInfoMap.get("cruiseResult")));
//            if (Boolean.TRUE.equals(conditionRes)){
//                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail()
//                        .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
//                        .setTaskResultId(redisInfoMap.get("taskResultId"))
//                        .setInstanceId(Long.valueOf(redisInfoMap.get("instanceId")))
//                        .setInstanceName(redisInfoMap.get("instanceName"))
//                        .setCruiseTime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")))
//                        .setEndTime(DateTimeUtil.parse(redisInfoMap.get("endTime")))
//                        .setDeviceId(Long.valueOf(redisInfoMap.get("deviceId")))
//                        .setDeviceName(redisInfoMap.get("deviceName"))
//                        .setCruiseStatus(252)
//                        .setRemark(redisInfoMap.get("remark"));
//                tctrdList.add(tCruiseTaskResultDetail);
//                cruiseResultIdList.add(redisInfoMap.get("cruiseResultId"));
//
//                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult()
//                        .setCruiseResultId(redisInfoMap.get("cruiseResultId"))
//                        .setCruiseId(Long.valueOf(redisInfoMap.get("cruiseId")))
//                        .setCruiseName(redisInfoMap.get("cruiseName"))
//                        .setCruiseType(228)
//                        .setResultNum(redisInfoMap.get("resultNum"))
//                        .setModifyNum(redisInfoMap.get("modifyNum"))
//                        .setPicpath(redisInfoMap.get("picpath"))
//                        //待完善.setPicPathAnl(redisInfoMap.get("picPathAnl"))
//                        .setOrigpic(redisInfoMap.get("origpic"))
//                        //待完善.setOrigPicAnl(redisInfoMap.get("origPicAnl"))
//                        .setConfirmPicPath(redisInfoMap.get("confirmPicPath"))
//                        .setOrigConfirmPicPath(redisInfoMap.get("origConfirmPicPath"))
//                        .setEvaluationState(257)
//                        .setCreatetime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")))
//                        .setIsWarn(0)
//                        .setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")))
//                        .setCruiseAbnormal(Boolean.TRUE.equals(!StringUtils.equals("null", redisInfoMap.get("cruiseAbnormal"))) ?
//                                Integer.valueOf(redisInfoMap.get("cruiseAbnormal")) : null)
//                        .setRemark(Boolean.TRUE.equals(!StringUtils.equals("null", redisInfoMap.get("remark"))) ?
//                                redisInfoMap.get("remark") : null)
//                        .setResultPic(Boolean.TRUE.equals(!StringUtils.equals("null", redisInfoMap.get("resultPic"))) ?
//                                redisInfoMap.get("resultPic") : null)
//                        .setFirName("f");
//                tcdrList.add(tCruiseDataResult);
//            }
//        }
//        log.info("任务{}的tCTRDList大小是:{},tCDRList大小是: {}", taskId, tctrdList.size(), tcdrList.size());
//
//        int res1 = 0;
//        int res2 = 0;
//        if (CollectionUtils.isNotEmpty(tctrdList)){
//            res1 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseTaskResultDetail(tctrdList);
//        }
//        if (CollectionUtils.isNotEmpty(tcdrList)){
//            res2 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseDataResult(tcdrList);
//            log.info("准备传其他服务的cruiseResultIdList==={}", cruiseResultIdList);
//            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_FINISH, cruiseResultIdList, Result.class);
//        }
//        log.info("插tCTRD的条数:{},插tCDR的条数:{}", res1, res2);
//
//        // 将已经做过的巡视点Map清空
//        if (CollectionUtils.isNotEmpty(Constant.flagMap.get(taskId))){
//            log.info("将公共类的instanceIdList清空");
//            Constant.flagMap.remove(taskId);
//        }
//
//        try {
//            // 判断缓存中的异常点，如果机器人任务是最后执行，则更新缓存并更新表
//            if (abnormal + normal == totalCheckPoint){
//                log.info("机器人巡检点是最后一个点");
//                Thread.sleep(15000);
//
//                TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult()
//                        .setTaskId(taskId)
//                        .setTaskName(tCruiseTaskResultMap.get("taskName"))
//                        .setTaskAlarm(0)
//                        .setTaskAbnormal(abnormal)
//                        .setRunExecute(tCruiseTaskResultMap.get("if_run"))
//                        .setCruiseTaskTime(DateTimeUtil.parse(tCruiseTaskResultMap.get("cruiseTaskTime")))
//                        .setTaskResultId(tCruiseTaskResultMap.get("taskResultId"));
//                log.info("任务为{}的tCruiseTaskResult内容是==={}", taskId, tCruiseTaskResult);
//                StaticContextAccessor.getBean(RobotService.class).insertTCruiseTaskResult(tCruiseTaskResult);
//
//                Integer taskWait = totalCheckPoint - normal - abnormal;
//                tCruiseResult.setTaskWait(taskWait);
//                if (changeTaskStatus){ //操作类任务不更新任务状态
//                    tCruiseResult.setCState(240);
//                    tCruiseResult.setTaskCode(taskId);
//                    tCruiseResult.setCreateTime(DateTimeUtil.parse(tCruiseTaskResultMap.get("cruiseTaskTime")));
//                    log.info("任务为{}的tCruiseResult内容是==={}", taskId, tCruiseResult);
//                    // 更新TCR表
//                    int res = StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
//                    log.info("更新TCR的条数====" + res);
//                    {
//                        //低优先任务继续
//                        StaticContextAccessor.getBean(RobotService.class).lowTaskGoOn(taskId);
//                    }
//                }
//
//                // webSocket通知前端调用巡视监控的接口（任务完成）
//                Map<String, Object> jasonMap = new HashMap<>(2);
//                jasonMap.put("type", "lastOneInstance");
//                jasonMap.put("taskId", taskId);
//                String json = JSON.toJSONString(jasonMap);
//                log.info("最后一个点-前端推送：" + json);
//                Constant.postUrl(webSocketUrl,json);
//
//                for (TCruiseTaskResultDetail tctrd : tctrdList){
//                   StaticContextAccessor.getBean(RobotService.class).updateIsWarn(taskId, tctrd.getInstanceId(), tctrd.getCruiseResultId());
//                }
//
//                int taskAbnormal = StaticContextAccessor.getBean(RobotService.class).selectAlarmNumByTaskId(taskId);
//                // 最后在更新一下异常数
//                TCruiseTaskResult updateResult = new TCruiseTaskResult();
//                updateResult.setTaskId(taskId);
//                updateResult.setTaskAbnormal(taskAbnormal);
//                StaticContextAccessor.getBean(RobotService.class).updateTCruiseTaskResult(updateResult);
//            }else{
//                log.info("机器人巡检点不是最后一个点");
//                Integer taskWait = totalCheckPoint - normal - abnormal;
//                tCruiseResult.setTaskWait(taskWait);
//                tCruiseResult.setCState(239);
//                tCruiseResult.setTaskCode(taskId);
//                log.info("任务为{}的tCruiseResult内容是==={}", taskId, tCruiseResult);
//                StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
//            }
//        }catch (Exception e){
//            log.error(e.getMessage(), e);
//        }
//    }

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
