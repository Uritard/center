package com.yjh.accessrobot.netty.handler;

import com.alibaba.druid.util.StringUtils;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.accessrobot.common.Constant;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformPacketUtil;
import com.yjh.accessrobot.common.utils.PackageProtocolUtils.PlatformXMLUtil;
import com.yjh.accessrobot.common.utils.StaticContextAccessor;
import com.yjh.accessrobot.commons.restTemplate.ServiceRestTemplate;
import com.yjh.accessrobot.commons.result.Result;
import com.yjh.accessrobot.commons.utils.DateTimeUtil;
import com.yjh.accessrobot.module.command.entity.*;
import com.yjh.accessrobot.module.command.service.RobotService;
import com.yjh.accessrobot.netty.entiy.HandlerEnum;
import com.yjh.accessrobot.netty.server.RobotServerHandler;
import com.yjh.accessrobot.netty.thread.StandTaskDealThread;
import com.yjh.accessrobot.threadpool.TaskExecutePool;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.*;

/**
 * @author YChen
 * @date 2021/12/14
 */
@Service
@Slf4j
public class RobotTaskStatusHandler implements MessageHandlerStrategy, InitializingBean {

    @Value("${other.webSocketUrl}")
    private String websocketUrl;

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RobotService robotService;

    @Override
    public void handler(ChannelHandlerContext ctx, RobotServerHandler robotServerHandler, XMLBaseModel xmlBaseModel, long sendSessionId, long receiveSessionId) throws Exception {
        log.info("+++++++++++++++++巡视主机收到任务状态数据了+++++++++++++++++");
        // Deal with robot task status data
        String robotCode = xmlBaseModel.getSendCode();
        Map<String, Object> taskStatusMap = new HashMap<>(16);
        taskStatusMap.put("taskPatrolled_id", xmlBaseModel.getItems().get(0).get("task_patrolled_id").toString());
        String taskName = xmlBaseModel.getItems().get(0).get("task_name").toString();
        taskStatusMap.put("taskName",taskName );
        String taskId = xmlBaseModel.getItems().get(0).get("task_code").toString();
        taskStatusMap.put("taskCode", taskId);
        taskStatusMap.put("taskState", xmlBaseModel.getItems().get(0).get("task_state").toString());
        taskStatusMap.put("planStartTime", xmlBaseModel.getItems().get(0).get("plan_start_time"));
        String startTime = xmlBaseModel.getItems().get(0).get("start_time").toString();
        taskStatusMap.put("startTime", DateTimeUtil.format(DateTimeUtil.parse(startTime)));
        taskStatusMap.put("taskProgress", xmlBaseModel.getItems().get(0).get("task_progress").toString());
        taskStatusMap.put("taskEstimatedTime", xmlBaseModel.getItems().get(0).get("task_estimated_time").toString());
        taskStatusMap.put("description", xmlBaseModel.getItems().get(0).get("description").toString());

        // 判断任务是否属于机器人本体任务
        Long robotId = robotService.selectIsRobotTask(taskId);
        if (Objects.nonNull(robotId)) {
            Map<String, String> jasonMap = new HashMap<>(2);
            jasonMap.put("type", "newTask");
            jasonMap.put("taskId", taskId);
            String json = JSON.toJSONString(jasonMap);
            Constant.postUrl(websocketUrl, json);

            StandTaskDealThread standTaskDealThread = new StandTaskDealThread(redisTemplate, taskId, robotCode);
            TaskExecutePool.getInstance().execute(standTaskDealThread);

            /// 以备后面做任务超时使用
            /*Map<String,Object> mapForTaskAreTime  = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
            Float tasksAreTime = Float.valueOf((String) mapForTaskAreTime.get("content"));
            Map<String,String> mapForAbnormal = new HashMap<>();
            mapForAbnormal.put("taskStart",taskStatusMap.get("startTime").toString());
            mapForAbnormal.put("overDay",tasksAreTime.toString());
            String strForCountAbnormal = "countForAbnormal:"+taskStatusMap.get("taskCode").toString();
            redisTemplate.opsForHash().putAll(strForCountAbnormal,mapForAbnormal);*/
        }

        Map<String, Object> listMap = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskId);
        taskStatusMap.put("instanceList", listMap.get("instanceIdList"));
        log.info("taskStatusMap==" + taskStatusMap);
        redisTemplate.opsForHash().putAll("RobotTaskStatus:" + robotCode + ":" + taskId, taskStatusMap);

        String taskStatusXmlString = PlatformXMLUtil.generateXml(RobotServerHandler.sendMessageForCommandThree(true, robotCode));
        byte[] taskStatusProtocol = PlatformPacketUtil.createPacket(Constant.sendSessionId, sendSessionId, false, taskStatusXmlString);
        RobotServerHandler.send(taskStatusProtocol, robotCode);
        log.info("巡视主机给机器人{}响应了", robotCode);

        // 任务根本没做或者没有完成,但是上报任务状态信息进度为100%(E机器人出现过,里面有很多重复代码,未优化)
        if (Objects.equals("1",taskStatusMap.get("task_state").toString())) {

            // 统计机器人返回任务结果的大小
            List<String> resultList = new ArrayList<>();
            Set<String> cruiseKey = redisScan("t_cruise_task_result:" + taskId);
            for (String key : cruiseKey) {
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                Boolean flag = org.apache.commons.lang3.StringUtils.equals("228",redisInfoMap.get("cruiseType")) && (org.apache.commons.lang3.StringUtils.equals("246",redisInfoMap.get("cruiseResult"))|| org.apache.commons.lang3.StringUtils.equals("247",redisInfoMap.get("cruiseResult")));
                if (Boolean.TRUE.equals(flag)){
                    resultList.add(redisInfoMap.get("instanceId"));
                }
            }
            log.info("机器人任务为{}返回结果个数===={}", taskId, resultList.size());

            // 统计巡视主机下发给机器人的巡检点大小
            Map<String, String> redisInfoMap2 = redisTemplate.opsForHash().entries("RobotTaskStatus:" + robotCode + ":" + taskId);
            String instanceList = redisInfoMap2.get("instanceIdList");
            instanceList = instanceList.replaceAll("\\[", "").replaceAll("]", "");
            String[] instanceIdArray = instanceList.split(", ");
            List<String> allInstanceIdList = new ArrayList<>();
            Collections.addAll(allInstanceIdList, instanceIdArray);
            log.info("巡视主机下发给机器人任务为{}的巡检点个数===={}", taskId, allInstanceIdList.size());

            if (resultList.isEmpty()) {
                neverDone(taskId, allInstanceIdList, taskName);
            }else if (resultList.size() < allInstanceIdList.size()){
                unFinished(taskId, allInstanceIdList, taskName);
            }
        }
        // 国网要求
        robotService.upToCruise(xmlBaseModel);
    }

    /**
     * 任务根本没有做,但是上报任务进度为100%
     * @param taskId 任务id
     * @param allInstanceIdList 巡视主机下发给机器人的巡检点
     * @param taskName 任务名称
     * @return void
     */
    private void neverDone(String taskId, List<String> allInstanceIdList, String taskName) throws Exception{
        String strForCountAbnormal = "countForAbnormal:" + taskId;
        Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
        Integer totalNum = Integer.valueOf(abnormalCount.get("all").toString());
        Integer abnormalNum = Integer.valueOf(abnormalCount.get("abnormal").toString()) ;
        Integer normalNum = Integer.valueOf(abnormalCount.get("normal").toString()) ;
        log.info("taskId为{}的总检测点数是==={},异常点数是==={},正常点数是==={}", taskId, totalNum, abnormalNum, normalNum);
        Integer abnormal = abnormalNum;
        Integer normal = normalNum;

        TCruiseResult tCruiseResult = robotService.selectTaskResultId(taskId);

        List<Long> instanceIdList = Constant.flagMap.get(taskId);
        log.info("已经做过的巡视点====" + instanceIdList);
        if (!instanceIdList.isEmpty()){
            for (Long instanceId : instanceIdList){
                allInstanceIdList.remove(instanceId.toString());
            }
        }
        log.info("删除已经做过的巡视点后==="+allInstanceIdList);

        List<Long> isFinishedInstanceList = StaticContextAccessor.getBean(RobotService.class).selectInstanceForTaskGoOn(taskId);
        log.info("已经入库的巡视点==="+isFinishedInstanceList);
        if (CollectionUtils.isNotEmpty(isFinishedInstanceList)) {
            for (Long instanceIdInTable : isFinishedInstanceList) {
                allInstanceIdList.remove(instanceIdInTable.toString());
            }
        }
        log.info("删除已经入库的巡视点后==="+allInstanceIdList);
        log.info("准备遍历的点是==="+allInstanceIdList);

        List<TCruiseDataResult> tcdrList = new ArrayList<>();
        List<TCruiseTaskResultDetail> tctrdList = new ArrayList<>();
        List<String> cruiseResultIdList = new ArrayList<>();

        for (String instanceId : allInstanceIdList) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId);
            if (!Objects.equals("设备检修中",redisInfoMap.get("resultNum"))) {
                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                tCruiseTaskResultDetail.setCruiseResultId(redisInfoMap.get("cruiseResultId"));
                tCruiseTaskResultDetail.setTaskResultId(redisInfoMap.get("taskResultId"));
                tCruiseTaskResultDetail.setInstanceId(Long.valueOf(redisInfoMap.get("instanceId")));
                tCruiseTaskResultDetail.setInstanceName(redisInfoMap.get("instanceName"));
                tCruiseTaskResultDetail.setCruiseTime(new Date());
                tCruiseTaskResultDetail.setCruiseTime(new Date());
                tCruiseTaskResultDetail.setEndTime(new Date());
                tCruiseTaskResultDetail.setDeviceId(Long.valueOf(redisInfoMap.get("deviceId")));
                tCruiseTaskResultDetail.setDeviceName(redisInfoMap.get("deviceName"));
                tCruiseTaskResultDetail.setCruiseStatus(Integer.valueOf(redisInfoMap.get("cruiseStatus")));
                tCruiseTaskResultDetail.setRemark(redisInfoMap.get("remark"));
                tctrdList.add(tCruiseTaskResultDetail);

                cruiseResultIdList.add(redisInfoMap.get("cruiseResultId"));

                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                tCruiseDataResult.setCruiseResultId(redisInfoMap.get("cruiseResultId"));
                tCruiseDataResult.setCruiseId(Long.valueOf(redisInfoMap.get("cruiseId")));
                tCruiseDataResult.setCruiseName(redisInfoMap.get("cruiseName"));
                tCruiseDataResult.setCruiseType(228);
                if ("null".equals(redisInfoMap.get("resultNum"))){
                    tCruiseDataResult.setResultNum("异常终止");
                }else {
                    tCruiseDataResult.setResultNum(redisInfoMap.get("resultNum"));
                }
                tCruiseDataResult.setModifyNum(redisInfoMap.get("modifyNum"));
                tCruiseDataResult.setPicpath("--");
                tCruiseDataResult.setOrigpic("--");
                tCruiseDataResult.setEvaluationState(257);
                tCruiseDataResult.setCreatetime(new Date());
                tCruiseDataResult.setIsWarn(0);
                if ("null".equals(redisInfoMap.get("cruiseResult"))) {
                    abnormal = abnormal + 1;
                    tCruiseDataResult.setCruiseResult(247);
                    log.info("这次变化的abnormal是==="+abnormal);
                }
                if (!"null".equals(redisInfoMap.get("cruiseAbnormal"))) {
                    tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(redisInfoMap.get("cruiseAbnormal")));
                }
                tCruiseDataResult.setRemark(null);
                tCruiseDataResult.setResultPic(null);
                tCruiseDataResult.setFirName("f");
                tcdrList.add(tCruiseDataResult);
            }
        }
        log.info("任务{}的tCTRDList大小是:{},tCDRList大小是: {}", taskId, tctrdList.size(), tcdrList.size());

        int res1 = 0;
        int res2 = 0;
        if (CollectionUtils.isNotEmpty(tctrdList)){
            res1 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseTaskResultDetail(tctrdList);
        }
        if (CollectionUtils.isNotEmpty(tcdrList)){
            res2 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseDataResult(tcdrList);
            log.info("准备传其他服务的cruiseResultIdList===" + cruiseResultIdList);
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_FINISH, cruiseResultIdList, Result.class);

        }
        log.info("插tCTRD的条数: " + res1 + ",插tCDR的条数: " + res2);

        log.info("准备更新的abnormal是：" + abnormal + ",准备更新的normal是: "+normal);

        Map<String, String> mapForAbnormal = new HashMap<>(5);
        mapForAbnormal.put("abnormal", abnormal.toString());
        mapForAbnormal.put("normal", normal.toString());
        // 更新异常点缓存的数据
        redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);

        /*
         * 判断缓存中的异常点，如果机器人任务是最后执行，则更新缓存并更新表
         */
        if (abnormal + normal == totalNum) {
            log.info("机器人巡检点是最后一个点");
            Thread.sleep(15000);

            TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult()
                    .setTaskId(taskId)
                    .setTaskName(taskName)
                    .setTaskAlarm(0)
                    .setTaskAbnormal(abnormal)
                    .setCruiseTaskTime(new Date())
                    .setTaskResultId(tCruiseResult.getTaskResultId());
            log.info("tCruiseTaskResult的内容是===" + tCruiseTaskResult);
            StaticContextAccessor.getBean(RobotService.class).insertTCruiseTaskResult(tCruiseTaskResult);

            Integer taskWait = totalNum - normal - abnormal;
            log.info("taskWait的值是==" + taskWait);
            tCruiseResult.setTaskWait(taskWait);
            tCruiseResult.setCState(243);
            tCruiseResult.setTaskCode(taskId);
            tCruiseResult.setCreateTime(new Date());
            log.info("tCruiseResult的内容是==={}", tCruiseResult);
            StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);

            // webSocket通知前端调用巡视监控的接口（任务完成）
            Map<String, Object> jasonMap = new HashMap<>(5);
            jasonMap.put("type", "lastOneInstance");
            jasonMap.put("taskId", taskId);
            String json = JSON.toJSONString(jasonMap);
            log.info("最后一个点-前端推送：" + json);
            Constant.postUrl(websocketUrl, json);

            for (TCruiseTaskResultDetail tctrd : tctrdList) {
                StaticContextAccessor.getBean(RobotService.class).updateIsWarn(taskId,tctrd.getInstanceId(),tctrd.getCruiseResultId());
            }

        } else {
            log.info("机器人巡检点不是最后一个点");
            Integer taskWait = totalNum - normal - abnormal;
            log.info("taskWait的值是==" + taskWait);
            tCruiseResult.setTaskWait(taskWait);
            tCruiseResult.setCState(239);
            tCruiseResult.setTaskCode(taskId);
            log.info("tCruiseResult的内容是===" + tCruiseResult);
            StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
        }
    }

    /**
     * 任务没有完成,但是上报任务进度为100%
     * @param taskId 任务id
     * @param allInstanceIdList 巡视主机下发给机器人的巡检点
     * @param taskName 任务名称
     * @return void
     */
    private void unFinished(String taskId,List<String> allInstanceIdList,String taskName) throws Exception{
        String strForCountAbnormal = "countForAbnormal:" + taskId;
        Map<String, Object> abnormalCount = redisTemplate.opsForHash().entries(strForCountAbnormal);
        Integer totalNum = Integer.valueOf(abnormalCount.get("all").toString());
        Integer abnormalNum = Integer.valueOf(abnormalCount.get("abnormal").toString()) ;
        Integer normalNum = Integer.valueOf(abnormalCount.get("normal").toString()) ;
        log.info("taskId为{}的总检测点数是==={}, 异常点数是==={}, 正常点数是==={}", taskId, totalNum, abnormalNum, normalNum);
        Integer abnormal = abnormalNum;
        Integer normal = normalNum;

        TCruiseResult tCruiseResult = robotService.selectTaskResultId(taskId);

        List<Long> instanceIdList = Constant.flagMap.get(taskId);
        log.info("已经做过的巡视点====" + instanceIdList);
        if (!instanceIdList.isEmpty()) {
            for (Long instanceId : instanceIdList) {
                allInstanceIdList.remove(instanceId.toString());
            }
        }
        log.info("删除已经做过的巡视点后===" + allInstanceIdList);

        List<Long> isFinishedInstanceList = StaticContextAccessor.getBean(RobotService.class).selectInstanceForTaskGoOn(taskId);
        log.info("已经入库的巡视点===" + isFinishedInstanceList);
        if (CollectionUtils.isNotEmpty(isFinishedInstanceList)) {
            for (Long instanceIdInTable : isFinishedInstanceList) {
                allInstanceIdList.remove(instanceIdInTable.toString());
            }
        }
        log.info("删除已经入库的巡视点后===" + allInstanceIdList);
        log.info("准备遍历的点是===" + allInstanceIdList);

        List<TCruiseDataResult> tcdrList = new ArrayList<>();
        List<TCruiseTaskResultDetail> tctrdList = new ArrayList<>();
        List<String> cruiseResultIdList = new ArrayList<>();

        for (String instanceId : allInstanceIdList) {
            Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries("t_cruise_task_result:" + taskId + ":" + instanceId);
            // 缓存中该巡检点有结果
            if (!Objects.equals("设备检修中",redisInfoMap.get("resultNum"))) {
                TCruiseTaskResultDetail tCruiseTaskResultDetail = new TCruiseTaskResultDetail();
                tCruiseTaskResultDetail.setCruiseResultId(redisInfoMap.get("cruiseResultId"));
                tCruiseTaskResultDetail.setTaskResultId(redisInfoMap.get("taskResultId"));
                tCruiseTaskResultDetail.setInstanceId(Long.valueOf(redisInfoMap.get("instanceId")));
                tCruiseTaskResultDetail.setInstanceName(redisInfoMap.get("instanceName"));
                if ("null".equals(redisInfoMap.get("cruiseTime"))) {
                    tCruiseTaskResultDetail.setCruiseTime(new Date());
                } else {
                    tCruiseTaskResultDetail.setCruiseTime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")));
                }
                if ("null".equals(redisInfoMap.get("endTime"))) {
                    tCruiseTaskResultDetail.setEndTime(new Date());
                } else {
                    tCruiseTaskResultDetail.setEndTime(DateTimeUtil.parse(redisInfoMap.get("endTime")));
                }
                tCruiseTaskResultDetail.setDeviceId(Long.valueOf(redisInfoMap.get("deviceId")));
                tCruiseTaskResultDetail.setDeviceName(redisInfoMap.get("deviceName"));
                tCruiseTaskResultDetail.setCruiseStatus(Integer.valueOf(redisInfoMap.get("cruiseStatus")));
                tCruiseTaskResultDetail.setRemark(redisInfoMap.get("remark"));
                tctrdList.add(tCruiseTaskResultDetail);

                cruiseResultIdList.add(redisInfoMap.get("cruiseResultId"));

                TCruiseDataResult tCruiseDataResult = new TCruiseDataResult();
                tCruiseDataResult.setCruiseResultId(redisInfoMap.get("cruiseResultId"));
                tCruiseDataResult.setCruiseId(Long.valueOf(redisInfoMap.get("cruiseId")));
                tCruiseDataResult.setCruiseName(redisInfoMap.get("cruiseName"));
                tCruiseDataResult.setCruiseType(228);
                if ("null".equals(redisInfoMap.get("resultNum"))){
                    tCruiseDataResult.setResultNum("异常终止");
                }else {
                    tCruiseDataResult.setResultNum(redisInfoMap.get("resultNum"));
                }
                tCruiseDataResult.setModifyNum(redisInfoMap.get("modifyNum"));
                if ("null".equals(redisInfoMap.get("picpath"))) {
                    tCruiseDataResult.setPicpath("--");
                } else {
                    tCruiseDataResult.setPicpath(redisInfoMap.get("picpath"));
                }
                if ("null".equals(redisInfoMap.get("origpic"))) {
                    tCruiseDataResult.setOrigpic("--");
                } else {
                    tCruiseDataResult.setOrigpic(redisInfoMap.get("origpic"));
                }
                tCruiseDataResult.setEvaluationState(257);
                if ("null".equals(redisInfoMap.get("cruiseTime"))) {
                    tCruiseDataResult.setCreatetime(new Date());
                } else {
                    tCruiseDataResult.setCreatetime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")));
                }
                tCruiseDataResult.setIsWarn(0);
                if ("null".equals(redisInfoMap.get("cruiseResult"))) {
                    abnormal = abnormal + 1;
                    tCruiseDataResult.setCruiseResult(247);
                    log.info("这次变化的abnormal是==="+abnormal);
                } else if ("246".equals(redisInfoMap.get("cruiseResult"))) {
                    tCruiseDataResult.setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")));
                } else if ("247".equals(redisInfoMap.get("cruiseResult"))) {
                    abnormal = abnormal + 1;
                    tCruiseDataResult.setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")));
                    log.info("这次变化的abnormal是==="+abnormal);
                }
                if (!"null".equals(redisInfoMap.get("cruiseAbnormal"))) {
                    tCruiseDataResult.setCruiseAbnormal(Integer.valueOf(redisInfoMap.get("cruiseAbnormal")));
                }
                if (!"null".equals(redisInfoMap.get("remark"))) {
                    tCruiseDataResult.setRemark(redisInfoMap.get("remark"));
                } else {
                    tCruiseDataResult.setRemark(null);
                }
                if (!"null".equals(redisInfoMap.get("resultPic"))) {
                    tCruiseDataResult.setResultPic(redisInfoMap.get("resultPic"));
                } else {
                    tCruiseDataResult.setResultPic(null);
                }
                tCruiseDataResult.setFirName("f");
                tcdrList.add(tCruiseDataResult);
            }
        }

        log.info("准备更新的abnormal是：" + abnormal + ",准备更新的normal是: "+normal);

        Map<String, String> mapForAbnormal = new HashMap<>(5);
        mapForAbnormal.put("abnormal", abnormal.toString());
        mapForAbnormal.put("normal", normal.toString());
        // 更新异常点缓存的数据
        redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);

        log.info("任务{}的tCTRDList大小是:{},tCDRList大小是: {}", taskId, tctrdList.size(), tcdrList.size());

        int res1 = 0;
        int res2 = 0;
        if (CollectionUtils.isNotEmpty(tctrdList)){
            res1 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseTaskResultDetail(tctrdList);
        }
        if (CollectionUtils.isNotEmpty(tcdrList)){
            res2 = StaticContextAccessor.getBean(RobotService.class).batchInsertCruiseDataResult(tcdrList);
            log.info("准备传其他服务的cruiseResultIdList===" + cruiseResultIdList);
            StaticContextAccessor.getBean(ServiceRestTemplate.class).postForObject(Constant.TASK_FINISH, cruiseResultIdList, Result.class);
        }
        log.info("插tCTRD的条数: " + res1 + ",插tCDR的条数: " + res2);

        // 将公共类的instanceIdList清空
        if (Constant.flagMap.get(taskId) != null && !Constant.flagMap.get(taskId).isEmpty()) {
            log.info("进来了？？？");
            for (Long instancedId : Constant.flagMap.get(taskId)) {
                allInstanceIdList.remove(instancedId.toString());
            }
        }

        /*
         * 判断缓存中的异常点，如果机器人任务是最后执行，则更新缓存并更新表
         */
        if (abnormal + normal == totalNum) {
            log.info("机器人巡检点是最后一个点");
            Thread.sleep(15000);

            TCruiseTaskResult tCruiseTaskResult = new TCruiseTaskResult()
                    .setTaskId(taskId)
                    .setTaskName(taskName)
                    .setTaskAlarm(0)
                    .setTaskAbnormal(abnormal)
                    .setCruiseTaskTime(new Date())
                    .setTaskResultId(tCruiseResult.getTaskResultId());
            log.info("tCruiseTaskResult的内容是===" + tCruiseTaskResult);
            StaticContextAccessor.getBean(RobotService.class).insertTCruiseTaskResult(tCruiseTaskResult);

            Integer taskWait = totalNum - normal - abnormal;
            log.info("taskWait的值是==" + taskWait);
            tCruiseResult.setTaskWait(taskWait);
            tCruiseResult.setCState(243);
            tCruiseResult.setTaskCode(taskId);
            tCruiseResult.setCreateTime(new Date());
            log.info("tCruiseResult的内容是===" + tCruiseResult);
            StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);

            // webSocket通知前端调用巡视监控的接口（任务完成）
            Map<String, Object> jasonMap = new HashMap<>(5);
            jasonMap.put("type", "lastOneInstance");
            jasonMap.put("taskId", taskId);
            String json = JSON.toJSONString(jasonMap);
            log.info("最后一个点-前端推送：" + json);
            Constant.postUrl(websocketUrl, json);

            for (TCruiseTaskResultDetail tctrd : tctrdList) {
                StaticContextAccessor.getBean(RobotService.class).updateIsWarn(taskId,tctrd.getInstanceId(),tctrd.getCruiseResultId());
            }

        } else {
            log.info("机器人巡检点不是最后一个点");
            Integer taskWait = totalNum - normal - abnormal;
            log.info("taskWait的值是==" + taskWait);
            tCruiseResult.setTaskWait(taskWait);
            tCruiseResult.setCState(239);
            tCruiseResult.setTaskCode(taskId);
            log.info("tCruiseResult的内容是===" + tCruiseResult);
            StaticContextAccessor.getBean(RobotService.class).updateTCruiseResult(tCruiseResult);
        }
    }

    /**
     * Redis数据库批量查询Key值游标
     * */
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

    @Override
    public void afterPropertiesSet() throws Exception {
        MessageHandlerStrategyFactory.register(HandlerEnum.ROBOT_TASK_STATUS.getCode(), this);
    }
}
