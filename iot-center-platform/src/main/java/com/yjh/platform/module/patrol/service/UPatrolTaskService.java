/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.mqtt.AlarmService;
import com.yjh.platform.common.mqtt.GetSpringUtil;
import com.yjh.platform.common.mqtt.alarmMsgBody.Alarm;
import com.yjh.platform.common.mqtt.alarmMsgBody.Defect;
import com.yjh.platform.common.mqtt.alarmMsgBody.Different;
import com.yjh.platform.common.mqtt.ftpsService;
import com.yjh.platform.common.quartz.JobManager;
import com.yjh.platform.common.quartz.QuartzTask;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.*;
import com.yjh.platform.common.utils.smUtil.Demo;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.TCruisePointInstanceNameDetail;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.UPatrolPlanAttrDao;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskAttrDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskDao;
import com.yjh.platform.module.patrol.entity.TDefectInfo;
import com.yjh.platform.module.patrol.entity.TWarnInfo;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.patrol.thread.InspectionResultThread;
import com.yjh.platform.module.patrol.thread.IsWarnAfterCruiseThread;
import com.yjh.platform.module.patrol.thread.LocalCruiseExecutThread;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDelDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.entity.TCruiseDataResult;
import com.yjh.platform.module.task.entity.TCruiseTaskResultDetail;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.threadpool.TaskExecutePool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.MultiKeyCommands;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static com.yjh.platform.module.patrol.CruiseConstant.*;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/11
 * @since [产品/模块版本] （可选）
 */
@Service
public class UPatrolTaskService {
    private Logger log = LoggerFactory.getLogger(UPatrolTaskService.class);

    public static final String PATROL_TASK_PREFIX = "patrol_task_result:";
    public static final String PATROL_SUMMARY_PREFIX = "countForAbnormal:";

    @Autowired
    private UPatrolTaskDao uPatrolTaskDao;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private TCruiseTaskDelDao tCruiseTaskDelDao;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private UPatrolTaskAttrDao uPatrolTaskAttrDao;
    @Autowired
    private UPatrolResultDao uPatrolResultDao;
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    @Autowired
    private SysUserDao sysUserDao;
    @Autowired
    private Demo demo;
    @Autowired
    private UPatrolPlanAttrDao uPatrolPlanAttrDao;
    @Autowired
    private AnalyseDataOperateService analyseDataOperateService;
    @Autowired
    private TCruisePlanDao tCruisePlanDao;
    @Autowired
    private ProcessResultToUpSystem processResultToUpSystem;

    //jobName
    @Value("${spring.QingHua.jobName}")
    private String jobName;
    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";
    private static final String ROBOT_TASK_URL = "http://iot-center-accessrobot/robot/v1/taskIssued";

    private static final byte[] LOCK_FLAG = new byte[0];

    private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat daySdf = new SimpleDateFormat("yyyy-MM-dd");

    @Transactional(rollbackFor = Exception.class)
    public String insert(UPatrolTask uPatrolTask, TCruiseTaskAdd tCruiseTaskAdd) {
        setLevel(uPatrolTask, tCruiseTaskAdd);
        try {
            if (uPatrolTask.getExecuteType() == TaskTypeEnum.CYCLE.getType()) {
                Date startTime = format.parse("2000-01-01 00:00:00");
                Date endTime = null;
                if (StringUtils.isNotEmpty(tCruiseTaskAdd.getCycleMonth())
                        && StringUtils.isNotEmpty(tCruiseTaskAdd.getCycleWeek())
                        && StringUtils.isNotEmpty(tCruiseTaskAdd.getCycleExecuteTime())){
                    startTime = DateTimeUtil.parse(tCruiseTaskAdd.getCycleStartTime());
                    endTime = DateTimeUtil.parse(tCruiseTaskAdd.getCycleEndTime());
                }
                // 间隔
                if (StringUtils.isNotEmpty(tCruiseTaskAdd.getIntervalType())
                        && StringUtils.isNotEmpty(tCruiseTaskAdd.getIntervalNumber())
                        && StringUtils.isNotEmpty(tCruiseTaskAdd.getIntervalExecuteTime())){
                    startTime = DateTimeUtil.parse(tCruiseTaskAdd.getIntervalStartTime());
                    endTime = DateTimeUtil.parse(tCruiseTaskAdd.getIntervalEndTime());
                }
                uPatrolTask.setStartTime(startTime);
                uPatrolTask.setEndTime(endTime);
                uPatrolTask.setCreateTime(new Date());
            }
        } catch (Exception e) {
            log.error("设置周期任务起始时间出错：", e);
        }
        if (Objects.isNull(uPatrolTask.getTaskId())) {
            uPatrolTask.setTaskId(String.valueOf(UUID.randomUUID()).replace("-", ""));
        }
        if (Objects.isNull(uPatrolTask.getTaskCode())) {
            uPatrolTask.setTaskCode(uPatrolTask.getTaskId());
        }

        List<Long> instanceList = insertTaskAttr(uPatrolTask, tCruiseTaskAdd);

        List<TCruisePointInstanceNameDetail> detailList = initializeTaskInfo(instanceList, uPatrolTask);
        // 找出机器人和无人机做任务的巡检点
        String res = taskToRobotOrDrone(uPatrolTask, tCruiseTaskAdd, format, detailList);
        if (StringUtils.isNotEmpty(res)) {
            return res;
        }
        setQuartzTask(uPatrolTask);

        //任务状态上报站端
        sendTaskStateToUp(uPatrolTask, 5);

        return uPatrolTask.getTaskId();
    }

    private void setLevel(UPatrolTask uPatrolTask, TCruiseTaskAdd tCruiseTaskAdd) {
        Integer ifRun = uPatrolTask.getExecuteType();
        if (tCruiseTaskAdd.getTaskLevel() == null) {
            if (Objects.equals("0", tCruiseTaskAdd.getUnionTaskStatus()) || tCruiseTaskAdd.getUnionTaskStatus() == null) {
                if (Objects.equals(TaskTypeEnum.NOW.getType(), ifRun)) {
                    uPatrolTask.setTaskLevel(3);
                } else {
                    uPatrolTask.setTaskLevel(1);
                }
            } else {
                uPatrolTask.setTaskLevel(4);
            }
        }
    }

    private List<Long> insertTaskAttr(UPatrolTask uPatrolTask, TCruiseTaskAdd tCruiseTaskAdd) {
        List<Long> instanceList = new ArrayList<>();

        List<UPatrolPlanAttr> uPatrolPlanAttrList = uPatrolPlanAttrDao.selectByPlanId(tCruiseTaskAdd.getPlanId());
        List<UPatrolTaskAttr> uPatrolTaskAttrs = new ArrayList<>();
        for (UPatrolPlanAttr uPatrolPlanAttr : uPatrolPlanAttrList) {
            UPatrolTaskAttr uPatrolTaskAttr = new UPatrolTaskAttr();
            uPatrolTaskAttr.setTaskId(uPatrolTask.getTaskId());
            uPatrolTaskAttr.setInstanceId(uPatrolPlanAttr.getInstanceId());
            uPatrolTaskAttr.setDeviceMeteId(uPatrolPlanAttr.getDeviceMeteId());
            uPatrolTaskAttr.setDeviceId(uPatrolPlanAttr.getDeviceId());
            uPatrolTaskAttr.setDeviceMeteId(uPatrolPlanAttr.getDeviceMeteId());
            uPatrolTaskAttr.setCustomId(uPatrolPlanAttr.getCustomId());
            uPatrolTaskAttr.setPointTaskId(uPatrolTaskAttr.getPointTaskId());
            instanceList.add(uPatrolPlanAttr.getInstanceId());
            uPatrolTaskAttrs.add(uPatrolTaskAttr);
            if (uPatrolTaskAttrs.size()%2000 == 0){
                this.uPatrolTaskAttrDao.batchAdd(uPatrolTaskAttrs);
                uPatrolTaskAttrs = new ArrayList<>();
            }
        }
        if (uPatrolTaskAttrs.size() > 0) {
            uPatrolTaskAttrDao.batchAdd(uPatrolTaskAttrs);
        }
        TCruisePlanCount plan = tCruisePlanDao.selectByPrimaryId(tCruiseTaskAdd.getPlanId());
        uPatrolTask.setTaskType(plan.getType());
        uPatrolTaskDao.add(uPatrolTask);
        return instanceList;
    }


    public List<TCruisePointInstanceNameDetail> initializeTaskInfo(List<Long> instanceList, UPatrolTask task)  {
        UPatrolResult uPatrolResult = new UPatrolResult();
        uPatrolResult.setTaskId(task.getTaskId())
                .setTaskName(task.getTaskName())
                .setTaskCode(task.getTaskCode())
                .setAreaId(task.getAreaId())
                .setTaskType(task.getTaskType())
                .setExecuteType(task.getExecuteType())
                .setTaskLevel(task.getTaskLevel())
                .setTaskState(238)
                .setTaskCount(instanceList.size())
                .setTaskWait(instanceList.size())
                .setRemark("0");
        uPatrolResultDao.add(uPatrolResult);

        List<TCruisePointInstanceNameDetail> detailList = tCruisePointInstanceDao.selectForTask(instanceList);
        log.info("instancesList==={}", detailList);
        for (TCruisePointInstanceNameDetail item : detailList) {
            UPatrolDataResult uPatrolDataResult = new UPatrolDataResult();
            uPatrolDataResult.setTaskId(task.getTaskId())
                    .setDeviceId(item.getDeviceId())
                    .setDeviceName(item.getDeviceName())
                    .setInstanceId(item.getInstanceId())
                    .setInstanceName(item.getInstanceName())
                    .setCruiseId(item.getCruiseId())
                    .setCruiseName(item.getCruiseName())
                    .setCruiseStatus(253)
                    .setCruiseType(item.getCruiseType());
            Map map = Object2Map.objectToMap(uPatrolDataResult, true);
            if(item.getCruiseType() != 228){
                map.put("cameraId",item.getCruiseId().toString());
                map.put("robotId","");
            }else {
                map.put("cameraId","");
                map.put("robotId",item.getRobotId().toString());
            }
            String str = PATROL_TASK_PREFIX + task.getTaskId() + ":" + item.getInstanceId();
            redisTemplate.opsForHash().putAll(str, map);
        }
        return detailList;
    }

    /**
     * 处理机器人/无人机巡视结果
     *
     * @param resultList 机器人/无人机巡视结果
     * @return void
     */
    public void robotPatrolTaskResult(List<RobotPatrolTaskResult> resultList) {
        if (resultList.isEmpty()){
            return;
        }

        for (RobotPatrolTaskResult robotPatrolTaskResult : resultList) {
            Map<String, String> infoMap = new HashMap<>(8);

            // 通过上报的任务id查询巡视主机上的任务id
            String taskCode = robotPatrolTaskResult.getTaskCode();
            String taskId = selectRealTaskByTaskCode(taskCode);
            if (StringUtils.isEmpty(taskId)) {
                taskId = taskCode;
                log.info("taskId is empty, use taskCode as taskId");
            }
            log.info("taskCode==={},taskId===={}", taskCode, taskId);
            infoMap.put("taskId", taskId);

            // 文件处理
            Map<String, String> isAlarmMap = resultFileHandler(robotPatrolTaskResult, infoMap);
            // 告警处理
            alarmHandlerAfterCruise(robotPatrolTaskResult, taskId, isAlarmMap);
            // 巡视结果处理
            InspectionResultThread cruiseResultDealThread = new InspectionResultThread(robotPatrolTaskResult, infoMap, redisTemplate, true);
            TaskExecutePool.getInstance().execute(cruiseResultDealThread);
            // 非同源告警处理
//            NonhomologousWarnThread nonhomologousWarnThread = new NonhomologousWarnThread(cruiseResultMap, redisTemplate, websocketUrl,1);
//            TaskExecutePool.getInstance().execute(nonhomologousWarnThread);
        }
    }

    /**
     * 根据巡视结果判断是否生成告警
     *
     * @param robotPatrolTaskResult 机器人/无人机巡视结果
     * @param taskId
     * @param isAlarmMap
     * @return void
     */
    private void alarmHandlerAfterCruise(RobotPatrolTaskResult robotPatrolTaskResult, String taskId, Map<String, String> isAlarmMap) {
        try {
            isAlarmMap.put("robotCode", robotPatrolTaskResult.getRobotCode());
            isAlarmMap.put("taskCode", taskId);
            isAlarmMap.put("deviceId", robotPatrolTaskResult.getDeviceId());
            isAlarmMap.put("value", robotPatrolTaskResult.getValue());
            isAlarmMap.put("deviceName", robotPatrolTaskResult.getDeviceName());
            isAlarmMap.put("recognitionType", robotPatrolTaskResult.getRecognitionType());
            isAlarmMap.put("fileType", robotPatrolTaskResult.getFileType());
            isAlarmMap.put("time", robotPatrolTaskResult.getTime());
            isAlarmMap.put("taskName", robotPatrolTaskResult.getTaskName());
            IsWarnAfterCruiseThread isWarnAfterCruiseThread = new IsWarnAfterCruiseThread(isAlarmMap, redisTemplate);
            TaskExecutePool.getInstance().execute(isWarnAfterCruiseThread);
        }catch (Exception e){
            log.error("机器人/无人机告警处理异常：", e);
        }
    }

    /**
     * 对机器人/无人机结果文件处理
     *
     * @param robotPatrolTaskResult 机器人/无人机巡视结果
     * @param infoMap               任务结果其他信息
     * @return Map<String, String>
     */
    private Map<String, String> resultFileHandler(RobotPatrolTaskResult robotPatrolTaskResult, Map<String, String> infoMap) {
        Map<String, String> isAlarmMap = new HashMap<>(16);
        String taskId = infoMap.get("taskId");

        String ftpImageRelative = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageRelative", "content"));
        String ftpImageAbsolute = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute", "content"));
        String ftpsFilePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content"));
        String filePathTemp = new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/" + taskId;

        try {
            // 文件路径
            String filePath = robotPatrolTaskResult.getFilePath();
            String temporaryFilePath = ftpsFilePath + "/" + filePath;
            log.info("巡视结果图片全路径==={}", temporaryFilePath);
            String fileName = filePath.trim().substring(filePath.trim().lastIndexOf("/") + 1);

            // 1.红外 2.可见光 3.音频 4.视频
            String fileType = robotPatrolTaskResult.getFileType();
            String developAbsoluteUrl = ftpImageAbsolute + "/" + filePathTemp;
            String developRelativeUrl = ftpImageRelative + "/" + filePathTemp;

            switch (fileType) {
                case "1":
                    String descFirFilePath = developAbsoluteUrl + "/FIR/" + fileName;
                    FileUtil.copyFileUsingStream(temporaryFilePath, descFirFilePath);
                    infoMap.put("relativePath", developRelativeUrl + "/FIR/" + fileName);
                    infoMap.put("absolutePath", descFirFilePath);

                    isAlarmMap.put("relativePath", developRelativeUrl + "/FIR/" + fileName);
                    isAlarmMap.put("absolutePath", descFirFilePath);
                    break;
                case "2":
                    String descCcdFilePath = developAbsoluteUrl + "/CCD/" + fileName;
                    FileUtil.copyFileUsingStream(temporaryFilePath, descCcdFilePath);
                    infoMap.put("relativePath", developRelativeUrl + "/CCD/" + fileName);
                    infoMap.put("absolutePath", descCcdFilePath);

                    isAlarmMap.put("relativePath", developRelativeUrl + "/CCD/" + fileName);
                    isAlarmMap.put("absolutePath", descCcdFilePath);
                    break;
                case "3":
                    String descAudioFilePath = developAbsoluteUrl + "/Audio/" + fileName;
                    FileUtil.copyFileUsingStream(temporaryFilePath, descAudioFilePath);
                    infoMap.put("relativePath", developRelativeUrl + "/Audio/" + fileName);
                    infoMap.put("absolutePath", descAudioFilePath);
                    break;
                case "4":
                    String descVideoFilePath = developAbsoluteUrl + "/Video/" + fileName;
                    FileUtil.copyFileUsingStream(temporaryFilePath, descVideoFilePath);
                    infoMap.put("relativePath", developRelativeUrl + "/Video/" + fileName);
                    infoMap.put("absolutePath", descVideoFilePath);
                    break;
                default:
                    break;
            }
        }catch (Exception e){
            log.error("机器人/无人机文件处理异常：", e);
        }
        return isAlarmMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public String selectRealTaskByTaskCode(String robotTaskId) {
        return tRobotInspectionDao.selectRealTaskId(robotTaskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer selectRobotType(String robotCode) {
        return tRobotInspectionDao.selectRobotType(robotCode);
    }

    /**
     * 处理机器人/无人机任务状态
     *
     * @param statusList 机器人/无人机任务状态
     *                   任务状态(1:已执行 2:正在执行 3:暂停 4:终止 5:未执行 6:超期)
     * @return void
     */
    public void robotPatrolTaskStatus(List<RobotPatrolTaskStatus> statusList) {
        statusList.forEach(robotPatrolTaskStatus -> {
            switch (robotPatrolTaskStatus.getTaskState()) {
                case "3":
                    break;
                case "2":
                    break;
                case "1":
                case "4":
                case "6":
                    //机器人任务终止
                    TaskExecutePool.getInstance().execute(
                            new Runnable() {
                                @Override
                                public void run() {
                                    dealRobotTaskShutDown(robotPatrolTaskStatus);
                                }
                            }
                    );
                    break;
                case "5":
                    break;
                default:
                    break;
            }
        });
    }

    private void dealRobotTaskShutDown(RobotPatrolTaskStatus robotPatrolTaskStatus) {
        try {
            //等待30秒
            Thread.sleep(30 * 1000);
        } catch (Exception e) {
            log.error("等待出错：", e);
        }
        String taskId = uPatrolTaskDao.selectTaskByRobotTaskCode(robotPatrolTaskStatus.getTaskCode());
        String cruiseResultKey = PATROL_TASK_PREFIX + taskId +":";
        //处理结果 获取机器人的点
        List<Map<String,String>> resultMap = redisTemplate.opsForHash().values(cruiseResultKey);
        resultMap.forEach(result ->{
            //判断是不是机器人的点以及还是否完成
            String cruiseType = result.get("cruiseType");
            String cruiseState = result.get("cruiseStatus");
            String instanceId = result.get("instanceId");
            if ("228".equals(cruiseType) && "253".equals(cruiseState)){
                //这个点 没有做
                result.put("cruiseStatus",String.valueOf(CRUISE_STATE_FAILED));//执行失败
                result.put("resultNum","机器人任务异常");
                result.put("cruiseAbnormal",String.valueOf(CruiseConstant.CRUISE_ABNORMAL_DATAABNORMAL));//数据异常
                result.put("evaluationState",String.valueOf(CruiseConstant.EVALUATION_STATE_UN));//未审核
                result.put("identifyResult",String.valueOf(CRUISE_RESULT_ABNORMAL));//异常

                String instanceKey = cruiseResultKey+instanceId;
                redisTemplate.opsForHash().putAll(instanceKey,result);

                patrolTaskResultHandler(taskId,Long.valueOf(instanceId));
            }
        });




    }

    /**
     * 找出机器人和无人机的点让其做任务
     *
     * @param task           任务信息
     * @param tCruiseTaskAdd 任务关联信息
     * @param format         时间格式
     * @param detailList     区域巡视主机上的巡视点信息
     * @return String
     */
    private String taskToRobotOrDrone(UPatrolTask task, TCruiseTaskAdd tCruiseTaskAdd, DateFormat format,
                                      List<TCruisePointInstanceNameDetail> detailList) {
        try {
            // 找出机器人和无人机做任务的巡检点
            List<Long> robotCruiseList = new ArrayList<>();
            List<Long> robotInstanceList = new ArrayList<>();
            for (TCruisePointInstanceNameDetail item : detailList) {
                if ((TypeEnum.ROBOT.getCode() == item.getCruiseType() || TypeEnum.UAV.getCode() == item.getCruiseType())) {
                    robotCruiseList.add(item.getCruiseId());
                    robotInstanceList.add(item.getInstanceId());
                }
            }
            log.info("robotCruiseList : {}", robotCruiseList);

            if (robotCruiseList.isEmpty()) {
                log.info("There are no instanceId for robot or drone to do！！！");
                return "";
            }

            List<String> robotCode = tRobotInspectionDao.selectForRobotTask(robotCruiseList);
            log.info("robotCode : {}", robotCode);
            List<RobotTaskInstanceInfo> robotTaskInfoList = new ArrayList<>();

            for (String item : robotCode) {
                Map<String, String> robotStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":61");
                Map<String, String> robotTaskStatusMap = redisTemplate.opsForHash().entries("RobotStatus:" + robotCode + ":41");
                String robotTaskStatus = robotTaskStatusMap.get("value");
                String robotPattern = robotStatusMap.get("value");
                if ("1".equals(robotTaskStatus) && "5".equals(robotPattern)) {
                    return "机器人" + robotCode + "正在执行操作任务,无法下发巡检任务！";
                }
                RobotTaskInstanceInfo robotTaskInfo = new RobotTaskInstanceInfo();
                robotTaskInfo.setCruiseType(task.getTaskType());
                robotTaskInfo.setTaskId(task.getTaskId());
                // 从巡视主机下发至机器人的任务等级都暂定3级
                robotTaskInfo.setPriority(3);
                robotTaskInfo.setTaskName(task.getTaskName());
                List<Long> robotTaskInstanceList = tRobotInspectionDao.selectRobotTaskInstanceId(robotInstanceList, item);
                robotTaskInfo.setInstanceList(robotTaskInstanceList);
                String ifFun = String.valueOf(tCruiseTaskAdd.getIfRun());
                robotTaskInfo.setIfRun(ifFun);
                robotTaskInfo.setRobotCode(item);
                robotTaskInfo.setUnionTaskStatus(tCruiseTaskAdd.getUnionTaskStatus());
                robotTaskInfo.setIsOcr(tCruiseTaskAdd.getIsOcr());

                // 根据任务信息及协议组装任务信息
                packageTaskProtocolInfo(tCruiseTaskAdd, format, robotTaskInfo, ifFun);

                robotTaskInfoList.add(robotTaskInfo);
            }

            Map<String, List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>(3);
            robotTaskInfoMap.put("robotTaskInfoList", robotTaskInfoList);
            log.info("robotTaskInfoMap = {}", robotTaskInfoMap);

            // 调用robot服务下发任务
            Result result = robotTask(robotTaskInfoMap);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    /**
     * 根据任务信息及协议组装任务信息
     *
     * @param tCruiseTaskAdd 任务关联信息
     * @param format         时间格式
     * @param taskInfo       任务信息
     * @param ifFun          任务执行类型
     */
    private void packageTaskProtocolInfo(TCruiseTaskAdd tCruiseTaskAdd, DateFormat format, RobotTaskInstanceInfo taskInfo, String ifFun) {
        try {
            CruiseConstant.TaskTypeEnum taskType = CruiseConstant.TaskTypeEnum.getEnm(Integer.valueOf(ifFun));

            switch (taskType) {
                //立即任务
                case NOW:
                    taskInfo.setFixedStartTime(format.format(new Date()));
                    break;
                //定时任务
                case TIME:
                    taskInfo.setFixedStartTime(format.format(tCruiseTaskAdd.getStartTime()));
                    break;
                // 周期和间隔任务
                case CYCLE:
                    taskInfo.setFixedStartTime("");
                    taskInfo.setCycleMonth(Optional.of(tCruiseTaskAdd.getCycleMonth()).orElse(""));
                    taskInfo.setCycleWeek(Optional.of(tCruiseTaskAdd.getCycleWeek()).orElse(""));
                    String cycleExecuteTime = tCruiseTaskAdd.getCycleExecuteTime();

                    if (StringUtils.isNotEmpty(cycleExecuteTime)) {
                        if (Integer.parseInt(tCruiseTaskAdd.getCycleExecuteTime()) < 10) {
                            cycleExecuteTime = "0" + tCruiseTaskAdd.getCycleExecuteTime() + ":00:00";
                        } else {
                            cycleExecuteTime = tCruiseTaskAdd.getCycleExecuteTime() + ":00:00";
                        }
                    }
                    taskInfo.setCycleExecuteTime(cycleExecuteTime);

                    taskInfo.setIntervalType(Optional.of(tCruiseTaskAdd.getIntervalType()).orElse(""));
                    taskInfo.setIntervalNumber(Optional.of(tCruiseTaskAdd.getIntervalNumber()).orElse(""));

                    String intervalExecuteTime = tCruiseTaskAdd.getIntervalExecuteTime();
                    intervalExecuteTime =
                            StringUtils.isNotEmpty(intervalExecuteTime) ? intervalExecuteTime.substring(11) : intervalExecuteTime;
                    taskInfo.setIntervalExecuteTime(intervalExecuteTime);

                    boolean isInterval = StringUtils.isEmpty(tCruiseTaskAdd.getIntervalType());
                    taskInfo.setCycleStartTime(isInterval ? format.format(new Date()) : "");
                    taskInfo.setCycleEndTime(isInterval ? tCruiseTaskAdd.getEndTime() : "");
                    taskInfo.setIntervalStartTime(isInterval ? "" : format.format(new Date()));
                    taskInfo.setIntervalEndTime(isInterval ? "" : tCruiseTaskAdd.getEndTime());
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public Result robotTask(Map<String, List<RobotTaskInstanceInfo>> robotTaskInfoMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                return serviceRestTemplate.postForObject(ROBOT_TASK_URL, robotTaskInfoMap, Result.class);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    private void setQuartzTask(UPatrolTask task) {
        //开启定时任务
        QuartzTask quartzTask = new QuartzTask();
        quartzTask.setJobName(task.getTaskName());
        quartzTask.setJobGroup(jobName);
        JobManager jobManager = new JobManager();
        if (task.getDateType() == null) {
            if (task.getExecuteType() == TaskTypeEnum.NOW.getType()) {
                //立即执行
                quartzTask.setStartTime(new Date());
            } else {
                //定时
                quartzTask.setStartTime(task.getStartTime());
            }
        } else {
            //周期 0 */10 * * * ?
            quartzTask.setCronExpression(task.getDateType());
            //quartzTask.setCronExpression("0 */1 * * * ?");
            log.info("quartzTask: " + quartzTask.getCronExpression());
        }
        try {
            jobManager.createTask(quartzTask, task);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void sendTaskStateToUp(UPatrolTask task, Integer state) {
        //任务状态上报站端

        try {
            XMLBaseModel xmlBaseModel = new XMLBaseModel();
            List<Map<String, Object>> items = new ArrayList<>();
            Map<String, Object> item = new HashMap<>();
            xmlBaseModel.setType("41");
            SimpleDateFormat simpleDateFormat2 = new SimpleDateFormat("yyyyMMddhhmmss");
            item.put("task_patrolled_id", task.getTaskId() + "_" + simpleDateFormat2.format(task.getStartTime()));
            item.put("task_name", task.getTaskName());
            item.put("task_code", task.getTaskCode());
            item.put("task_state", String.valueOf(state));
            item.put("plan_start_time", DateTimeUtil.format(task.getStartTime()));
            if (task.getExecuteType() == TaskTypeEnum.CYCLE.getType()) {
                try {
                    //CronExpression expression = new CronExpression(tCruiseTask.getDateType());
                    item.put("start_time", DateTimeUtil.format(task.getStartTime()));
                } catch (Exception e) {
                    log.info("上报出错", e);
                }
            } else {
                item.put("start_time", DateTimeUtil.format(task.getStartTime()));
            }
            item.put("task_progress", "0%");
            Integer i = 0;
            if (state == 5) {
                i = uPatrolTaskDao.countInstance(task.getTaskId());
            } else {
                Map<String, String> mapForGet = redisTemplate.opsForHash().entries(PATROL_SUMMARY_PREFIX + task.getTaskId());
                Integer all = Integer.valueOf(mapForGet.get("all"));
                Integer normal = Integer.valueOf(mapForGet.get("normal"));
                Integer abnormal = Integer.valueOf(mapForGet.get("abnormal"));
                i = all - normal - abnormal;
            }

            item.put("task_estimated_time", i * 60 * 5);
            item.put("description", "");
            items.add(item);
            xmlBaseModel.setItems(items);

            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            map.put("list", list);
            Result re = null;
            log.info("信息上报：-" + map);
            re = Constant.otherServer(map, Constant.TCP_URL);//江苏要求
        } catch (Exception e) {
            log.info("上报出错" + e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskConfirmation(String userId, String password, HttpServletRequest request, String identifier) throws Exception {
        String iP = request.getHeader("HTTP_X_FORWARDED_FOR");
        SysUser sysUser = sysUserDao.selectByPrimaryId(Long.valueOf(userId));
        //判断开关
        Map<String, String> map = redisTemplate.opsForHash().entries("t_sys_param:isEncryption");
        Boolean flag = Boolean.valueOf(map.get("content"));
        if (!flag) {
            //需要自己解密数据库password
            sysUser.setPassword(Demo.decryptDB(sysUser.getPassword()));
            //}
        } else {
            //全要解密
            password = demo.decryptIdentifier(password, identifier);
            sysUser.setPassword(Demo.decryptDB(sysUser.getPassword()));
            redisTemplate.delete("pubk:" + identifier);
        }
        //password = Demo.decrypt(password);
        //sysUser.setPassword(Demo.decryptDB(sysUser.getPassword()));
        if (sysUser.getPassword().equals(password)) {
            //            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            //            params.set("logType", "2");
            //            params.set("ip", iP);
            //            params.set("title", "新增任务");
            //            params.set("state", 1);
            //            params.set("userId",  Long.valueOf(userId));
            //            params.set("userName", sysUser.getUserName());
            //            params.set("requestOrigin",request.getRequestURL());
            //            params.set("requestPath",request.getRequestURI());
            //            params.set("requestMethod",request.getMethod());
            //            params.set("content", "根据用户传递的参数新增数据");
            //            LogsAspect logsAspect = new LogsAspect();
            //            logsAspect.post(params);
            return 1;
        } else {
            MultiValueMap<String, Object> params = new LinkedMultiValueMap<>();
            params.set("logType", "2");
            params.set("ip", iP);
            params.set("title", "任务下发");
            params.set("state", 2);
            params.set("userId", Long.valueOf(userId));
            params.set("userName", sysUser.getUserName());
            params.set("requestOrigin", request.getRequestURL());
            params.set("requestPath", request.getRequestURI());
            params.set("requestMethod", request.getMethod());
            params.set("content", "密码错误");
            LogsAspect logsAspect = new LogsAspect();
            logsAspect.post(params);
            throw new BusinessException(10106, "密码错误");
        }
    }


    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String taskId, String startTime) {
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);
        if (Objects.nonNull(task.getExecuteType()) && task.getExecuteType() == TaskTypeEnum.CYCLE.getType()) {
            if (!startTime.equals("-1")) {
                TCruiseTaskDel tCruiseTaskDel = new TCruiseTaskDel();
                tCruiseTaskDel.setTaskId(taskId);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//注意月份是MM
                try {
                    Date taskDate = simpleDateFormat.parse(startTime);
                    tCruiseTaskDel.setDelTime(taskDate);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }
                tCruiseTaskDel.setCreateTime(new Date());
                log.info("del task single...");
                return tCruiseTaskDelDao.insert(tCruiseTaskDel);
            } else {
                //判断当前周期任务是否已执行 --by tt 2021.3.10
                if (Objects.isNull(task.getDateType())) {
                    taskId = task.getTaskCode();
                }
                //删除整个周期任务
                log.info("del taskId..." + taskId + ", startTime; " + startTime);
                log.info("taskMap..." + Constant.taskMap);
                for (ConcurrentHashMap<String, Object> mapItem : Constant.taskMap) {
                    //找到任务Id
                    if (mapItem.get("taskId").equals(taskId)) {
                        JobManager.removeJob(mapItem.get("jobName").toString(), mapItem.get("jobGroupName").toString(), mapItem.get("triggerName").toString(), mapItem.get("triggerGroupName").toString());
//                        Constant.taskMap.remove(mapItem);
                    }
                }
                log.info("taskMap del..." + Constant.taskMap);
                log.info("del task totally...");
                tCruiseTaskDelDao.deleteByPrimaryId(taskId);
                return uPatrolTaskDao.deleteByPrimaryId(taskId);
            }
        } else if (Objects.nonNull(task.getExecuteType()) && task.getExecuteType() == TaskTypeEnum.TIME.getType()) {
            //删除定时任务
            for (ConcurrentHashMap<String, Object> mapItem : Constant.taskMap) {
                //找到任务Id
                if (mapItem.get("taskId").equals(taskId)) {
                    //删除定时任务
                    JobManager.removeJob(mapItem.get("jobName").toString(), mapItem.get("jobGroupName").toString(), mapItem.get("triggerName").toString(), mapItem.get("triggerGroupName").toString());
//                    Constant.taskMap.remove(mapItem);
                }
            }
        }
        tCruiseTaskDelDao.deleteByPrimaryId(taskId);
        return this.uPatrolTaskDao.deleteByPrimaryId(taskId);
    }

    public int taskPauseWithoutRobot(String taskId) {
        //任务暂停 不用给机器人发
        UPatrolResult taskResult = uPatrolResultDao.selectByPrimaryId(taskId);
        taskResult.setTaskState(TASK_STATE_PAUSE);
        try {
            //Thread.sleep(10000);
            //机器人任务暂停
            Map<String, String> jasonMapOnFinished = new HashMap<>();
            jasonMapOnFinished.put("type", "taskChange");
            jasonMapOnFinished.put("taskId", taskId);
            String jsonMessage = JSON.toJSONString(jasonMapOnFinished);
            log.info("发送给前端的消息：" + jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMapOnFinished);
        } catch (Exception e) {
            log.error("任务暂停异常: " + e);
            e.printStackTrace();
        }

        //任务状态上报站端
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(task, 3);

        return uPatrolResultDao.update(taskResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskStart(String taskId) {

        try {
            List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(taskId);
            log.info("机器人任务启动,robotCodeList:{}", robotCodeList);
            if (robotCodeList != null && robotCodeList.size() > 0) {
                Map<String, Object> robotTaskStatesMap = new HashMap<>();
                robotTaskStatesMap.put("taskId", taskId);
                robotTaskStatesMap.put("commandValue", 1);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                robotTaskStates(robotTaskStatesMap);
            }

        } catch (Exception e) {
            log.error("任务启动异常: " + e);
            e.printStackTrace();
        }

        //任务状态上报站端
        UPatrolTask uPatrolTask = uPatrolTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(uPatrolTask, 2);

        return 1;
    }

    public void robotTaskStates(Map<String, Object> robotTaskStatesMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(Constant.ROBOT_TASK_STATUS_URL, robotTaskStatesMap, String.class);
            }
        } catch (Exception e) {
            log.error("机器人任务控制出错：", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskPause(String taskId) {
        UPatrolResult uPatrolResult = uPatrolTaskDao.selectForTaskId(taskId);
        uPatrolResult.setTaskState(TASK_STATE_PAUSE);
        try {
            //TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);

            //Thread.sleep(10000);
            //机器人任务暂停
            List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(taskId);
            log.info("机器人任务暂停,robotCodeList:{}", robotCodeList);
            if (robotCodeList != null && robotCodeList.size() > 0) {
                Map<String, Object> robotTaskStatesMap = new HashMap<>();
                robotTaskStatesMap.put("taskId", taskId);
                robotTaskStatesMap.put("commandValue", 2);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                robotTaskStates(robotTaskStatesMap);
            }
            updateTaskStateForRedis(taskId, "241");
            Map<String, String> jasonMapOnFinished = new HashMap<>();
            jasonMapOnFinished.put("type", "taskChange");
            jasonMapOnFinished.put("taskId", taskId);
            String jsonMessage = JSON.toJSONString(jasonMapOnFinished);
            log.info("发送给前端的消息：" + jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMapOnFinished);
        } catch (Exception e) {
            log.error("任务暂停异常: " + e);
            e.printStackTrace();
        }

        //任务状态上报站端
        UPatrolTask uPatrolTask = uPatrolTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(uPatrolTask, 3);

        return uPatrolResultDao.update(uPatrolResult);
    }

    private void updateTaskStateForRedis(String taskId, String state) {
        String strForCountAbnormal = PATROL_SUMMARY_PREFIX + taskId;
        Map<String, String> map = redisTemplate.opsForHash().entries(strForCountAbnormal);
        map.put("taskState", state);
    }

    public String taskStatus(String taskId) {
        String strForCountAbnormal = PATROL_SUMMARY_PREFIX + taskId;
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(strForCountAbnormal, "taskState");
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskGoOn(String taskId) throws Exception {
        UPatrolResult uPatrolResult = uPatrolTaskDao.selectForTaskId(taskId);

        //机器人任务继续
        List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(taskId);
        log.info("机器人任务继续,robotCodeList:{}", robotCodeList);
        if (robotCodeList != null && robotCodeList.size() > 0) {
            Map<String, Object> robotTaskStatesMap = new HashMap<>();
            robotTaskStatesMap.put("taskId", taskId);
            robotTaskStatesMap.put("commandValue", 3);
            robotTaskStatesMap.put("robotCodeList", robotCodeList);
            robotTaskStates(robotTaskStatesMap);
        }
        if (uPatrolResult.getTaskState() == TASK_STATE_FINISHED || uPatrolResult.getTaskState() == TASK_STATE_EXECUTING) {
            return 1;
        }
//        if(Constant.taskStateMap.get(taskId) != null && Constant.taskStateMap.get(taskId) == 1){
//            uPatrolResult.setTaskState(239);
//            return uPatrolResultDao.update(uPatrolResult);
//        }

        updateTaskStateForRedis(taskId, "239");
        localTaskStart(taskId);

        uPatrolResult.setTaskState(TASK_STATE_EXECUTING);
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);


        Map<String, String> jasonMapOnFinished = new HashMap<>();
        jasonMapOnFinished.put("type", "taskChange");
        jasonMapOnFinished.put("taskId", taskId);
        String jsonMessage = JSON.toJSONString(jasonMapOnFinished);
        log.info("发送给前端的消息：" + jsonMessage);
        Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMapOnFinished);

        //任务状态上报站端
        sendTaskStateToUp(task, 2);
        return uPatrolResultDao.update(uPatrolResult);
    }

    /**
     * 任务终止，异步执行
     */
    @Async
    public int taskShutDown(String taskId) {
        UPatrolResult uPatrolResult = uPatrolResultDao.selectByPrimaryId(taskId);
        if (uPatrolResult.getTaskState() == TASK_STATE_FINISHED) {
            return 1;
        }
        uPatrolResult.setTaskState(TASK_STATE_INTERRUPT);
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);

        List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(taskId);
        Map<String, Object> robotTaskStatesMap = new HashMap<>();
        robotTaskStatesMap.put("taskId", taskId);
        robotTaskStatesMap.put("commandValue", 1);
        robotTaskStatesMap.put("robotCodeList", robotCodeList);
        robotTaskStates(robotTaskStatesMap);

        updateTaskStateForRedis(taskId, "242");
        try {

            Set<String> tasKeys = redisTemplate.keys(PATROL_TASK_PREFIX + taskId + ":*");
            if (CollectionUtils.isEmpty(tasKeys)) {
                log.error("patrol_task_result:{}:* 未查到任务，任务未正确初始化", taskId);
                throw new BusinessException("任务未正确初始化");
            }

            log.info("tasKeys size: {}", tasKeys.size());

            List<Map<String, String>> taskInfoList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>) connection -> {
                tasKeys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
                return null;
            });

            log.info("taskInfoList size: {}", taskInfoList.size());

            // 暂停15秒等待未接收数据完成接收
            Thread.sleep(15000);
            if(taskInfoList.size() != 0) {
                List<Map<String, String>> skipPointList = new ArrayList<>();
                for (Map<String, String> taskInfo : taskInfoList) {
                    if (MapUtils.isNotEmpty(taskInfo)) {
                        //count = count+1;
                        if (CommonUtils.isEmptyOrNullstr(taskInfo.get("cruiseResult"))) {
                            //任务终止
                            taskInfo.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                            taskInfo.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_INTERRUPT));
                            taskInfo.put("cruiseStatus", String.valueOf(CRUISE_STATE_UN));
                            taskInfo.put("resultNum", "任务终止");
                            skipPointList.add(taskInfo);
                        }
                        // todo 任务终止 上报站端
                    }
                }
                log.info("task [{}] shut down, skipPointList: {}", taskId, skipPointList.size());
                ThreadPoolUtil.PATROL_POOL.addThread(new LocalCruiseExecutThread<>(this, skipPointList, true));
            }

            log.info("任务终止成功=={}", taskId);
        } catch (Exception e) {
            log.info("任务终止失败", e);
        }

        //任务状态上报站端
        sendTaskStateToUp(task, 4);
        return uPatrolResultDao.update(uPatrolResult);
    }

    /**
     * 本地任务执行
     */
    public void localTaskStart(String taskId) {
        Set<String> tasKeys = redisTemplate.keys(PATROL_TASK_PREFIX + taskId + ":*");
        if (CollectionUtils.isEmpty(tasKeys)) {
            log.error("patrol_task_result:{}:* 未查到任务，任务未正确初始化", taskId);
            throw new BusinessException("任务未正确初始化");
        }

        log.info("tasKeys size: {}", tasKeys.size());

        List<Map<String, String>> taskInfoList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>) connection -> {
            tasKeys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
            return null;
        });

        log.info("taskInfoList size: {}", taskInfoList.size());

        // 查询检修区域
        List<Long> overhaul = tCruisePointInstanceDao.selectTimeIsIn(new Date());
        // 不需要执行的点
        List<Map<String, String>> skipPointList = new ArrayList<>();
        Map<String, List<Map<String, String>>> cruiseGroupMap = new HashMap<>(32);
        Map<Long, Integer> robotOfflineMap = new HashMap<>();

        taskInfoList.forEach(m -> {
            if(MapUtils.isEmpty(m)){
                log.error("taskInfo is empty.");
            }
            String instanceId = m.get("instanceId");
            int cruiseStatus = MapUtils.getIntValue(m, "cruiseStatus", CRUISE_STATE_UN);
            String cruiseResult = MapUtils.getString(m, "cruiseResult");
            // 已经执行点位
            if (cruiseStatus != CRUISE_STATE_UN && !CommonUtils.isEmptyOrNullstr(cruiseResult)) {
                log.warn("instance already done. task: {}, cruiseStatus: {}, cruiseResult: {}", taskId + ":" + instanceId, cruiseStatus, cruiseResult);
                return;
            }

            // 巡检点类型
            int cruiseType = MapUtils.getIntValue(m, "cruiseType");

            boolean skipFlag = false;
            // 设备检修判断
            if (CollectionUtils.isNotEmpty(overhaul) && Collections.binarySearch(overhaul, MapUtils.getLong(m, "deviceId")) >= 0) {
                m.put("resultNum", "设备检修中");
                // 异常原因，设备检修
                m.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_OVERHAUL));
                skipFlag = true;
            }
            // 机器人离线判断
            if (!skipFlag && TypeEnum.ROBOT.getCode() == cruiseType) {
                long robotId = MapUtils.getLongValue(m, "robotId");
                if (robotOffline(robotOfflineMap, robotId)) {
                    m.put("resultNum", "机器人离线,未执行");
                    if (robotOfflineMap.get(robotId) == 4) {
                        m.put("resultNum", "机器人处于检修状态,未执行");
                    }
                    // 异常原因，设备离线
                    m.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_OFFLINE));
                }
            }
            if (skipFlag) {
                // 巡视结果，异常
                m.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                // 未审核
                m.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
                m.put("isWarn", "0");
                m.put("picpath", "--");
                // 巡检数据状态，未执行
                m.put("cruiseStatus", String.valueOf(CRUISE_STATE_UN));
                String dateTime = DateTimeUtil.getDateTimeString();
                // m.put("createtime", dateTime);
                m.put("endTime", dateTime);
                m.put("cruiseTime", dateTime);
                skipPointList.add(m);
                return;
            }

            CruiseConstant.TypeEnum cruiseTypeEnum = TypeEnum.getEnum(cruiseType);
            switch (cruiseTypeEnum) {
                case VIDEO: // 视频
                case INFRARED: // 红外
                    String cameraId = MapUtils.getString(m, "cameraId");
                    if (StringUtils.isEmpty(cameraId)) {
                        log.error("task {} cruise data has no cameraId, {}", taskId, JSON.toJSONString(m));
                    } else {
                        String cameraIp = (String) redisTemplate.opsForHash().get("camera_info:" + cameraId, "cameraIp");
                        cruiseGroup(cruiseGroupMap, cameraIp, m);
                    }
                    break;
                case VOICE: // 声纹
                    String cruiseId = MapUtils.getString(m, "cruiseId");
                    if (StringUtils.isEmpty(cruiseId)) {
                        log.error("task {} cruise data has no cruiseId, {}", taskId, JSON.toJSONString(m));
                    } else {
                        cruiseGroup(cruiseGroupMap, cruiseId, m);
                    }
                    break;
                case ROBOT: // 机器人
                case UAV: // 无人机
                case ONLINE: // 在线监控
                default:
                    break;
            }
        });

        log.info("task [{}] ready, skipPointList: {}, cruiseGroupMapSize: {}", taskId, skipPointList.size(), cruiseGroupMap.size());
        ThreadPoolUtil.PATROL_POOL.addThread(new LocalCruiseExecutThread<>(this, skipPointList, true));
        for (List<Map<String, String>> pointList : cruiseGroupMap.values()){
            ThreadPoolUtil.PATROL_POOL.addThread(new LocalCruiseExecutThread<>(this, pointList, false));
        }

    }

    /**
     * 判断机器人是否离线
     */
    public boolean robotOffline(Map<Long, Integer> robotOfflineMap, long robotId) {
        if (robotId > 0 && !robotOfflineMap.containsKey(robotId)) {
            TRobotInfo tRobotInfo = tRobotInspectionDao.selectRobot(robotId);
            if (tRobotInfo != null) {
                Map<String, String> mapForRobotState =
                        redisTemplate.opsForHash().entries("RobotStatus:" + tRobotInfo.getRobotCode() + ":41");
                if ("离线".equals(tRobotInfo.getRobotStatus())) {
                    // 1 机器人离线
                    robotOfflineMap.put(robotId, 1);
                } else if ("4".equals(mapForRobotState.get("value"))) {
                    // 4 机器人检修
                    robotOfflineMap.put(robotId, 4);
                } else {
                    // 机器人在线
                    robotOfflineMap.put(robotId, 0);
                }
            }
        }
        return robotOfflineMap.getOrDefault(robotId, 0) != 0;
    }

    public void cruiseGroup(Map<String, List<Map<String, String>>> cruiseGroupMap, String key, Map<String, String> m) {
        List<Map<String, String>> cruiseList = cruiseGroupMap.computeIfAbsent(key, k -> new ArrayList<>());
        cruiseList.add(m);
    }

    /**
     * 巡视任务结果处理
     *
     * @param taskId     任务id
     * @param instanceId 巡视点id
     */
    public void patrolTaskResultHandler(String taskId, Long instanceId) {
        String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
        Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(redisKeyName);
        log.info("taskResultHandler redisKeyName: {},", redisKeyName);
        patrolTaskResultHandler(cruiseResultMap);
    }

    public void patrolTaskResultHandler(Map<String, String> cruiseResultMap) {
        String taskId = MapUtils.getString(cruiseResultMap, "taskId");

        int abnormalCounts = patrolTaskResult(taskId, MapUtils.getIntValue(cruiseResultMap, "cruiseResult"), 1);

        // 巡视结果上报上一级系统
        processResultToUpSystem.alarmAndResultToUpSystem(cruiseResultMap, null, null);

        if (abnormalCounts >= 0) {
            log.info("{}该点是任务{}最后一个点", MapUtils.getString(cruiseResultMap, "instanceId"), taskId);
            completionOfTask(taskId, abnormalCounts);
        }
    }

    public void patrolTaskResultHandler(List<Map<String, String>> cruiseResultList) {
        int size = cruiseResultList.size();
        if(CollectionUtils.isEmpty(cruiseResultList)){
            log.error("cruiseResultList is empty.");
            return;
        }
        String taskId = cruiseResultList.get(0).get("taskId");
        int abnormalCounts = patrolTaskResult(taskId, CRUISE_RESULT_ABNORMAL, size);

        if (abnormalCounts >= 0) {
            log.info("该点任务执行完成{}", taskId);
            completionOfTask(taskId, abnormalCounts);
        }
    }

    private int patrolTaskResult(String taskId, int cruiseResult, int size) {
        // 获取当前redis正常异常点位个数并更新
        int abnormalCounts;
        int normalCounts;
        int allCounts;
        synchronized (LOCK_FLAG) {
            String strForCountAbnormal = PATROL_SUMMARY_PREFIX + taskId;
            Map<String, String> resultCountsMap = redisTemplate.opsForHash().entries(strForCountAbnormal);
            abnormalCounts = NumberUtils.toInt(resultCountsMap.get("abnormal"));
            normalCounts = NumberUtils.toInt(resultCountsMap.get("normal"));
            allCounts = NumberUtils.toInt(resultCountsMap.get("all"));
            log.info("从redis获取的taskId为{}的总检测点数是==={}, 异常点数是==={}, 正常点数是==={}", taskId, allCounts, abnormalCounts, normalCounts);

            if (CRUISE_RESULT_NORMAL == cruiseResult) {
                normalCounts += size;
            } else {
                abnormalCounts += size;
            }
            log.info("task:{}, normalCounts:{}, abnormalCounts:{}", taskId, normalCounts, abnormalCounts);
            resultCountsMap.put("abnormal", String.valueOf(abnormalCounts));
            resultCountsMap.put("normal", String.valueOf(normalCounts));
            resultCountsMap.put("lastCruiseTime", DateTimeUtil.getDateTimeString());
            redisTemplate.opsForHash().putAll(strForCountAbnormal, resultCountsMap);
            redisTemplate.expire(strForCountAbnormal, 7, TimeUnit.DAYS);
        }

        // 判断任务是否结束
        if (normalCounts + abnormalCounts < allCounts) {
            return -1;
        }
        return abnormalCounts;
    }

    /**
     * 判断是否生成告警，若是则更新任务结果表
     * @param taskId 任务id
     * @param instanceId 巡检点id
     * @param cruiseDataId 点位结果id
     * @return int
     */
    public int updateIsWarn(String taskId, Long instanceId, Long cruiseDataId){
        int isWarnFlag = uPatrolTaskDao.selectIsWarnByTaskId(instanceId, taskId);
        if (isWarnFlag > 0){
            uPatrolTaskDao.updateIsWarnByCruiseDataId(cruiseDataId);
            return 1;
        }
        return 0;
    }

    /**
     * 任务所有点做完,完成,并且进度为100%的处理
     *
     * @param taskId 任务id
     * @param abnormalCounts 异常点位数
     */
    private void completionOfTask(String taskId, Integer abnormalCounts) {
        try {
            Thread.sleep(15000);
            Map<String, String> jasonMap = new HashMap<>(2);
            jasonMap.put("type", "lastOneInstance");
            jasonMap.put("taskId", taskId);
            log.info("最后一个点-前端推送：{}", JSON.toJSONString(jasonMap));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);

            // 更新upr
            UPatrolResult uPatrolResult = new UPatrolResult().setTaskId(taskId);
            uPatrolResult.setTaskState(240);
            uPatrolResult.setTaskWait(0);
            uPatrolResult.setEndTime(new Date());
            uPatrolResult.setTaskAbnormal(abnormalCounts);
            uPatrolResultDao.update(uPatrolResult);

            // 插入updr
            //            List<Long> instanceIdDoneList = Constant.flagMap.get(taskId);
            //            log.info("任务为{}已经做过的巡视点===={}", taskId, instanceIdDoneList);
            //            List<Long> inDataBaseInstanceList = selectInstanceForTaskGoOn(taskId);
            //            log.info("任务为{}已经入库的巡视点==={}", taskId, inDataBaseInstanceList);
            //            if (CollectionUtils.isNotEmpty(instanceIdDoneList)) {
            //                for (Long instanceIdInTable : inDataBaseInstanceList) {
            //                    instanceIdDoneList.remove(instanceIdInTable.toString());
            //                }
            //            }
            //            log.info("删除已经入库的巡视点后==={}", instanceIdDoneList);

            List<UPatrolDataResult> uPatrolDataResultList = new ArrayList<>();
            List<String> cruiseResultIdList = new ArrayList<>();
            Set<String> robotInfoKeys = redisScan(PATROL_TASK_PREFIX + taskId);

            for (String key : robotInfoKeys) {
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);

                boolean conditionRes = ArrayUtils.contains(new String[]{String.valueOf(CRUISE_RESULT_NORMAL), String.valueOf(CRUISE_RESULT_ABNORMAL)}, redisInfoMap.get("cruiseResult"));
                if (Boolean.TRUE.equals(conditionRes)) {
                    UPatrolDataResult uPatrolDataResult = new UPatrolDataResult();
                    uPatrolDataResult.setTaskId(taskId);
                    uPatrolDataResult.setDeviceId(Long.valueOf(redisInfoMap.get("deviceId")));
                    uPatrolDataResult.setDeviceName(redisInfoMap.get("deviceName"));
                    uPatrolDataResult.setInstanceId(Long.valueOf(redisInfoMap.get("instanceId")));
                    uPatrolDataResult.setInstanceName(redisInfoMap.get("instanceName"));
                    uPatrolDataResult.setCruiseId(Long.valueOf(redisInfoMap.get("cruiseId")));
                    uPatrolDataResult.setCruiseName(redisInfoMap.get("cruiseName"));
                    uPatrolDataResult.setCruiseTime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")));
                    uPatrolDataResult.setCruiseStatus(Integer.valueOf(redisInfoMap.get("cruiseStatus")));
                    uPatrolDataResult.setResultNum(redisInfoMap.get("resultNum"));
                    uPatrolDataResult.setPicpath(redisInfoMap.get("picpath"));
                    uPatrolDataResult.setCruiseType(Integer.valueOf(redisInfoMap.get("cruiseType")));
                    uPatrolDataResult.setOrigpic(redisInfoMap.get("origpic"));
                    uPatrolDataResult.setCruiseAbnormal(NumberUtils.toInt(redisInfoMap.get("cruiseAbnormal")));
                    uPatrolDataResult.setEvaluationState(Integer.valueOf(redisInfoMap.get("evaluationState")));
                    uPatrolDataResult.setCreatetime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")));
                    uPatrolDataResult.setIsWarn(Integer.valueOf(redisInfoMap.get("isWarn")));
                    uPatrolDataResult.setCruiseResult(Integer.valueOf(redisInfoMap.get("cruiseResult")));

                    uPatrolDataResultList.add(uPatrolDataResult);
                    cruiseResultIdList.add("");


                }
            }
            log.info("任务{}的uPatrolDataResultList大小是:{}", taskId, uPatrolDataResultList.size());

            if (CollectionUtils.isNotEmpty(uPatrolDataResultList)){
                batchInsertUPatrolDataResult(uPatrolDataResultList);
                log.info("准备传其他服务的cruiseResultIdList==={}", cruiseResultIdList);
                // todo:准备传其他服务的cruiseResultIdList
            }

            for (UPatrolDataResult up : uPatrolDataResultList){
                updateIsWarn(taskId, up.getInstanceId(), up.getCruiseDataId());
            }

            // 将已经做过的巡视点Map清空
            if (CollectionUtils.isNotEmpty(Constant.flagMap.get(taskId))){
                log.info("将公共类的instanceIdList清空");
                Constant.flagMap.remove(taskId);
            }

            //低优先任务继续
//        StaticContextAccessor.getBean(RobotService.class).lowTaskGoOn(taskId);

            // todo:给上一级系统上报任务状态
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer selectIsAlarmByTask(String taskId, String instanceId) {
        return uPatrolTaskDao.selectIsAlarmByTask(taskId, Long.valueOf(instanceId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer updatePicPath(String taskId, String instanceId, String imagePath) {
        return uPatrolTaskDao.updatePicPath(taskId, Long.valueOf(instanceId), imagePath);
    }

    /**
     * 根据巡视点id查询该测点信息
     * @param instanceId 巡视点id
     * @return TStdDeviceMete
     */
    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceMete selectDeviceMeteInfo(Long instanceId){
        return uPatrolTaskDao.selectDeviceMeteInfo(instanceId);
    }

    /**
     * 字典值查询
     *
     * @param dictNote 说明
     * @param colName  类型
     * @return String
     */
    @Transactional(rollbackFor = Exception.class)
    public String selectDictCodeByNote(String dictNote, String colName){
        return uPatrolTaskDao.selectDictCodeByNote(dictNote,colName);
    }

    /**
     * 根据机器人实物id查询机器人信息
     *
     * @param robotCode 机器人实物id
     * @return TRobotInfo 机器人信息
     */
    @Transactional(rollbackFor = Exception.class)
    public TRobotInfo selectRobotInfoByCode(String robotCode){
        return uPatrolTaskDao.selectRobotInfoByCode(robotCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> taskCount(Date taskStartDate, int flag) {
        // 时间格式化处理
        Map<String, String> map = new HashMap<>();
        if (flag == 1) {
            map = monthHandle(taskStartDate);
        } else {
            map = yearHandle(taskStartDate);
        }

        SimpleDateFormat secondSdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat secondFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Date dayBefore = new Date();
        Date dayAfter = new Date();
        Date originTime = new Date();
        try {
            originTime = secondFormat.parse("2000-01-01 00:00:00");
            dayBefore = secondFormat.parse(map.get("firstDay"));
            dayAfter = secondFormat.parse(map.get("lastDay"));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("dayBefore：{}", dayBefore);
        log.info("dayAfter：{}", dayAfter);

        List<TCruiseTaskCount> list = uPatrolTaskDao.taskCount(dayBefore, dayAfter);
        List<TCruiseTaskDel> listDel = tCruiseTaskDelDao.slectByTimeZone(dayBefore, dayAfter);
        List<Map<String, Object>> listTask = new ArrayList<>();
        Date now = new Date();
        for (TCruiseTaskCount tCruiseTaskCount : list) {
            Date dayBeforeTime = dayBefore;
            Date dayAfterTime = dayAfter;
            if (tCruiseTaskCount.getIfRun() == TaskTypeEnum.CYCLE.getType() && StringUtils.isNotEmpty(tCruiseTaskCount.getDateType())) {

                if (tCruiseTaskCount.getStartTime().after(dayBeforeTime)){
                    dayBeforeTime = tCruiseTaskCount.getStartTime();
                }
                if (Objects.nonNull(tCruiseTaskCount.getEndTime()) && tCruiseTaskCount.getEndTime().before(dayAfterTime)){
                    dayAfterTime = tCruiseTaskCount.getEndTime();
                }

                log.info("dayBefore={}，dayAfter={}", dayBeforeTime, dayAfterTime);
                List<Date> timeList = DateTimeUtil.cornTransTime(tCruiseTaskCount.getDateType(), dayBeforeTime, dayAfterTime);
                for (Date aTimeList : timeList) {
                    Map<String, Object> taskCountMap = new HashMap<>();
                    Map<String, Object> taskCountMapDel = new HashMap<>();
                    long taskTime = aTimeList.getTime();
                    if (listDel.size() > 0) {
                        for (TCruiseTaskDel tCruiseTaskDel : listDel) {
                            long taskDelTime = tCruiseTaskDel.getDelTime().getTime();
                            if (Objects.equals(tCruiseTaskDel.getTaskId(), tCruiseTaskCount.getTaskId()) && taskDelTime == taskTime) {
                                log.info("已删除的任务信息： " + tCruiseTaskCount.getTaskId() + " " + taskDelTime);
                                taskCountMapDel.put("taskId", tCruiseTaskCount.getTaskId());
                                taskCountMapDel.put("taskDelTime", taskDelTime);
                            }
                        }
                        if (taskCountMapDel.size() == 0) {
                            taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                            taskCountMap.put("total", tCruiseTaskCount.getTotal());
                            taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                            taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                            taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                            taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                            if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                                taskCountMap.put("taskState", TASK_STATE_NOT_START);
                                taskCountMap.put("taskStateName", "任务未开始");
                            } else {
                                taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                                taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());

                            }
                            if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                                taskCountMap.put("taskStatus", "-1");
                            } else {
                                taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                            }
                            taskCountMap.put("startTime", secondSdf.format(aTimeList));
                            listTask.add(taskCountMap);
                        }
                    } else {
                        taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                        taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                        taskCountMap.put("total", tCruiseTaskCount.getTotal());
                        taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                        taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                        taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                        if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                            taskCountMap.put("taskState", TASK_STATE_NOT_START);
                            taskCountMap.put("taskStateName", "任务未开始");
                        } else {
                            taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                            taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());

                        }
                        if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                            taskCountMap.put("taskStatus", "-1");
                        } else {
                            taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                        }
                        taskCountMap.put("startTime", secondSdf.format(aTimeList));
                        listTask.add(taskCountMap);
                    }
                }
            } else {
                Map<String, Object> taskCountMap = new HashMap<>();
                taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                taskCountMap.put("total", tCruiseTaskCount.getTotal());
                taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                    taskCountMap.put("taskState", TASK_STATE_NOT_START);
                    taskCountMap.put("taskStateName", "任务未开始");
                } else {
                    taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                    taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());

                }
                if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                    taskCountMap.put("taskStatus", "-1");
                } else {
                    taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                }

                taskCountMap.put("startTime", secondSdf.format(tCruiseTaskCount.getStartTime()));
                listTask.add(taskCountMap);
            }
        }
        return listTask;
    }

    private Map<String, String> monthHandle(Date taskStartDate) {
        Map<String, String> map = new HashMap<>();
        if (Objects.equals(null, taskStartDate)) {
            taskStartDate = new Date();
        }
        DateTime start = DateUtil.beginOfMonth(taskStartDate);
        DateTime end = DateUtil.endOfMonth(taskStartDate);
        map.put("firstDay", DateTimeUtil.format(start));
        map.put("lastDay", DateTimeUtil.format(end));
        return map;
    }

    private Map<String, String> yearHandle(Date taskStartDate) {
        Map<String, String> map = new HashMap<>();
        if (Objects.equals(null, taskStartDate)) {
            taskStartDate = new Date();
        }
        DateTime start = DateUtil.beginOfYear(taskStartDate);
        DateTime end = DateUtil.endOfYear(taskStartDate);
        map.put("firstDay", DateTimeUtil.format(start));
        map.put("lastDay", DateTimeUtil.format(end));
        return map;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskList> selectPointStatus(String taskId) {
        List<TCruiseTaskList> tCruiseTaskList = uPatrolTaskDao.selectPointStatus(taskId);
        return tCruiseTaskList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> taskCountByCondition(Date startTime, Date endTime, String taskState, String taskName) {
        HashMap<String, Object> map = new HashMap<>();
        if ("-1".equals(taskState)) {
            taskState = null;
        }
        map.put("taskState", taskState);
        map.put("taskName", taskName);
        List<Map<String, Object>> resultList = new ArrayList<>();
        List<Map<String, Object>> resultListAfter = null;
        List<Map<String, Object>> resultListBefore = new ArrayList<>();
        List<Map<String, Object>> resultListForAdd = new ArrayList<>();
        Date now = new Date();
        try {
            if (endTime == null) {
                Calendar calendar = new GregorianCalendar();
                calendar.add(Calendar.DAY_OF_MONTH, 0);

                //一天的开始时间 yyyy:MM:dd 00:00:00
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                Date dayStart = calendar.getTime();
                String startStr = format.format(dayStart);
                now = format.parse(startStr);
                startTime = now;
                //System.out.println("一天的开始时间: "+now);
                //一天的结束时间 yyyy:MM:dd 23:59:59
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                Date dayEnd = calendar.getTime();
                String endStr = format.format(dayEnd);
                endTime = format.parse(endStr);
                //System.out.println("一天的开始时间: "+endTime);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        map.put("startTime", startTime);
        map.put("endTime", endTime);
        resultListBefore = this.afterTaskCount(startTime, taskState, taskName);
        resultListAfter = this.afterTaskCount(endTime, taskState, taskName);

        resultListForAdd.addAll(resultListBefore);

        for (Map<String, Object> itemBefore : resultListBefore) {
            for (Map<String, Object> itemAfter : resultListAfter) {
                if (itemAfter.get("taskId").toString().equals(itemBefore.get("taskId").toString())) {
                    resultListForAdd.remove(itemBefore);
                    break;
                }
            }
        }
        resultListForAdd.addAll(resultListAfter);
        if (resultListForAdd == null || resultListForAdd.size() == 0) {
            return resultList;
        } else {
            try {
                for (Map<String, Object> item : resultListForAdd) {

                    if (startTime.compareTo(format.parse(item.get("startTime").toString())) <= 0 && endTime.compareTo(format.parse(item.get("startTime").toString())) >= 0) {
                        if (taskState != null && !taskState.equals("")) {
                            if (taskState.equals(item.get("taskState").toString())) {
                                resultList.add(item);
                                continue;
                            } else {
                                continue;
                            }
                        }
                        resultList.add(item);
                    }
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return resultList;
    }

    public List<Map<String, Object>> afterTaskCount(Date taskStartDate, String taskState, String taskName) {

        SimpleDateFormat sdfF = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat sdfF2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date dayBefore = new Date();
        Date dayAfter = new Date();
        Date originTime = new Date();
        if (Objects.equals(null, taskStartDate)) {

            taskStartDate = new Date();
        }
        DateTime firstDay = DateUtil.beginOfYear(taskStartDate);
        DateTime lastDay = DateUtil.endOfYear(taskStartDate);

        try {
            originTime = format.parse("2000-01-01 00:00:00");
            dayBefore = firstDay;
            dayAfter = lastDay;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        List<TCruiseTaskCount> list = new ArrayList<>();
        HashMap<String, Object> map = new HashMap<>();
        map.put("startTime", dayBefore);
        map.put("endTime", dayAfter);
        map.put("taskState", taskState);
        map.put("taskName", taskName);
        list = this.uPatrolTaskDao.afterTaskCount(map);
        log.info("list: " + list);
        List<TCruiseTaskDel> listDel = this.tCruiseTaskDelDao.slectByTimeZone(dayBefore, dayAfter);
        List<Map<String, Object>> listTask = new ArrayList<>();
        for (TCruiseTaskCount tCruiseTaskCount : list) {
            Date dayBeforeTime = dayBefore;
            Date dayAfterTime = dayAfter;
            if (tCruiseTaskCount.getIfRun() == 172 && tCruiseTaskCount.getStartTime().compareTo(originTime) == 0) {
                if (tCruiseTaskCount.getStartTime().after(dayBeforeTime)){
                    dayBeforeTime = tCruiseTaskCount.getStartTime();
                }
                if (Objects.nonNull(tCruiseTaskCount.getEndTime()) && tCruiseTaskCount.getEndTime().before(dayAfterTime)){
                    dayAfterTime = tCruiseTaskCount.getEndTime();
                }

                log.info("dayBefore={}，dayAfter={}", dayBeforeTime, dayAfterTime);
                List<Date> timeList = DateTimeUtil.cornTransTime(tCruiseTaskCount.getDateType(), dayBeforeTime, dayAfterTime);
                for (Date aTimeList : timeList) {
                    Map<String, Object> taskCountMap = new HashMap<>();
                    Map<String, Object> taskCountMapDel = new HashMap<>();
                    long taskTime = aTimeList.getTime();
                    if (listDel.size() > 0) {
                        for (TCruiseTaskDel tCruiseTaskDel : listDel) {
                            long taskDelTime = tCruiseTaskDel.getDelTime().getTime();
                            if (Objects.equals(tCruiseTaskDel.getTaskId(), tCruiseTaskCount.getTaskId()) && taskDelTime == taskTime) {
                                log.info("已删除的任务信息： " + tCruiseTaskCount.getTaskId() + " " + taskDelTime);
                                taskCountMapDel.put("taskId", tCruiseTaskCount.getTaskId());
                                taskCountMapDel.put("taskDelTime", taskDelTime);
                            }
                        }
                        if (taskCountMapDel.size() == 0) {
                            taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                            taskCountMap.put("total", tCruiseTaskCount.getTotal());
                            taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                            taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                            taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                            taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                            //TODO 增加redis获取任务状态，1是真
                            if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                                taskCountMap.put("taskState", 238);
                                taskCountMap.put("taskStateName", "任务未开始");
                            } else {
                                taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                                taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());

                            }
                            if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                                taskCountMap.put("taskStatus", "-1");
                            } else {
                                taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                            }
                            taskCountMap.put("startTime", sdfF2.format(aTimeList));
                            listTask.add(taskCountMap);
                        }
                    } else {
                        taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                        taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                        taskCountMap.put("total", tCruiseTaskCount.getTotal());
                        taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                        taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                        taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                        //TODO 增加redis获取任务状态，1是真
                        if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                            taskCountMap.put("taskState", 238);
                            taskCountMap.put("taskStateName", "任务未开始");
                        } else {
                            taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                            taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());

                        }
                        if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                            taskCountMap.put("taskStatus", "-1");
                        } else {
                            taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                        }
                        taskCountMap.put("startTime", sdfF2.format(aTimeList));
                        listTask.add(taskCountMap);
                    }
                }
            } else {
                Map<String, Object> taskCountMap = new HashMap<>();
                taskCountMap.put("type", tCruiseTaskCount.getIfRun());
                taskCountMap.put("typeName", tCruiseTaskCount.getTypeName());
                taskCountMap.put("planTypeName", tCruiseTaskCount.getPlanTypeName());
                taskCountMap.put("total", tCruiseTaskCount.getTotal());
                taskCountMap.put("taskId", tCruiseTaskCount.getTaskId());
                taskCountMap.put("taskName", tCruiseTaskCount.getTaskName());
                //TODO 增加redis获取任务状态，1是真
                if (Objects.equals(null, tCruiseTaskCount.getTaskState())) {
                    taskCountMap.put("taskState", 238);
                    taskCountMap.put("taskStateName", "任务未开始");
                } else {
                    taskCountMap.put("taskState", tCruiseTaskCount.getTaskState());
                    taskCountMap.put("taskStateName", tCruiseTaskCount.getTaskStateName());
                }
                if (Objects.equals(null, tCruiseTaskCount.getTaskStatus())) {
                    taskCountMap.put("taskStatus", "-1");
                } else {
                    taskCountMap.put("taskStatus", tCruiseTaskCount.getTaskStatus());
                }

                taskCountMap.put("startTime", sdfF2.format(tCruiseTaskCount.getStartTime()));
                listTask.add(taskCountMap);
            }
        }
        log.info("listTask: " + listTask);
        return listTask;
    }

    @Transactional(rollbackFor = Exception.class)
    public int executeDifferentiateTasks(List<String> images) {
        //任务封装
        List<Analysis> analysisList = new ArrayList<>();
        String taskId = RandomStringUtils.randomAlphanumeric(12);
        for (String imageUrl : images) {
            Analysis analysis = new Analysis();
            analysis.setTaskId(taskId);
            analysis.setAnalyseType("11");
            analysis.setPicPath(imageUrl);
            if (imageUrl.equals(images.get(0))) {//Normal
                analysis.setInstanceId(Long.valueOf(0));
            } else {                             //different
                analysis.setInstanceId(Long.valueOf(RandomStringUtils.randomNumeric(10)));
            }
            analysisList.add(analysis);
        }

        log.info("List:" + analysisList);
        //任务插库

        UPatrolTask tCruiseTask = new UPatrolTask();
        tCruiseTask.setTaskId(taskId);
        tCruiseTask.setTaskName("图像判别-" + taskId);
        tCruiseTask.setStartTime(new Date());
        int status = uPatrolTaskDao.add(tCruiseTask);

        // TODO: 2021/2/6 联调时放开 任务下发请求
        //        //任务下发请求
        //        Map<String,List<Analysis>> listMap=new HashMap<>();
        //        listMap.put("list",analysisList);
        //        defect(listMap);
        //返回自定义结果
        return status;
    }

    @Transactional(rollbackFor = Exception.class)
    public UPatrolTask selectByPrimaryId(String taskId) {
        return uPatrolTaskDao.selectByPrimaryId(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruisePointInstanceDetail selectForTask (Long instanceId){
        return this.uPatrolTaskDao.selectForTask(instanceId);
    }

    @Transactional(rollbackFor = Exception.class)
    public TStdDeviceMete selectDeviceMete(Long deviceMeteId){
        return this.uPatrolTaskDao.selectDeviceMete(deviceMeteId);
    }

    /**
     * 根据任务id查询已经有结果且已入库的巡视点
     * @param taskId 任务id
     * @return Long
     */
    @Transactional(rollbackFor = Exception.class)
    public List<Long> selectInstanceForTaskGoOn(String taskId){
        return uPatrolTaskDao.selectInstanceForTaskGoOn(taskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsertUPatrolDataResult(List<UPatrolDataResult> uPatrolDataResultList) {
        return this.uPatrolTaskDao.batchInsertUPatrolDataResult(uPatrolDataResultList);
    }

    /**
     * 处理算法分析后的巡视结果
     *
     * @param  resultList 算法分析返回的巡视结果
     * @return void
     */
    public void analysePatrolTaskResult(List<AnalysePatrolTaskResult> resultList){
        if (resultList.isEmpty()){
            return;
        }
        log.info("jsonResult=={}", resultList);

        try {
            for (AnalysePatrolTaskResult patrolTaskResult: resultList) {
                String taskId = patrolTaskResult.getTaskId();
                String instanceId = patrolTaskResult.getInstanceId();
                String analyseType = patrolTaskResult.getAnalyseType();
                String analyseResultImg = patrolTaskResult.getAnalyseResultImg();
                String resultValue = patrolTaskResult.getResultValue();
                String resultDesc = patrolTaskResult.getResultDesc();
                String conf = patrolTaskResult.getConf();
                String firDocPath = patrolTaskResult.getFirDocPath();

                String redisKeyName = PATROL_TASK_PREFIX + taskId + ":" + instanceId;
                Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(redisKeyName);
                log.info("读取到redis的cruiseResultMap：{}", cruiseResultMap);

                String resultImage = cruiseResultMap.get("picpath");
                if(StringUtils.isNotEmpty(analyseResultImg)) {
                    resultImage = analyseResultImg;
                }

                if ("11".equals(analyseType) && "".equals(resultImage)) {
                    continue;
                }
                TStdDeviceMete tStdDevicemete = analyseDataOperateService.selectDeviceMeteByInstanceId(NumberUtils.toLong(cruiseResultMap.get("instanceId")));

                log.info("tStdDeviceMete==={}", JSON.toJSONString(tStdDevicemete));
                String msgID = String.valueOf(UUID.randomUUID());
                if (StringUtils.equals("11", analyseType)){
                    log.info("taskId为{}instanceId为{}的点为判别的点位", taskId, instanceId);
                    distinguishHandler(msgID, analyseResultImg, resultValue, cruiseResultMap);
                }else if (StringUtils.equals("398", analyseType)){
                    log.info("taskId为{}instanceId为{}的点为缺陷的点位", taskId, instanceId);
                    defectHandler(msgID, analyseResultImg, resultValue, cruiseResultMap, tStdDevicemete);
                }else {
                    log.info("taskId为{}instanceId为{}的点为识别的点位", taskId, instanceId);
                    recognitionHandler(analyseResultImg, resultValue, cruiseResultMap, tStdDevicemete, firDocPath);
                }

                cruiseResultMap.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));
                cruiseResultMap.put("isWarn", StringUtils.isNotEmpty(cruiseResultMap.get("isWarn")) ? "0" : "1");
                cruiseResultMap.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
                log.info("cruiseResultMap==={}", cruiseResultMap);
                redisTemplate.opsForHash().putAll(redisKeyName, cruiseResultMap);

                // webSocket通知前端调用巡视监控的接口
                Map<String, String> jasonMap = new HashMap<>(3);
                jasonMap.put("type", "finishedOneInstance");
                jasonMap.put("taskId", taskId);
                log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMap));
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);

                patrolTaskResultHandler(taskId, Long.valueOf(instanceId));

                // 缺陷和判别上报算法管理平台
                defectToAlgorithmM(taskId, instanceId, msgID);
            }
        }catch (Exception e){
            log.error("处理算法分析后的巡视结果异常：", e);
        }

    }

    /**
     * 缺陷和判别上报算法管理平台
     *
     * @param taskId 任务id
     * @param instanceId 巡视点id
     * @param msgID
     */
    private void defectToAlgorithmM(String taskId, String instanceId, String msgID) {
        // msg：判别告警 defect：缺陷告警
        Set<String> differentList= redisScan( "msg:" + msgID);
        Set<String> defectList = redisScan("defect:" + msgID);
        ftpsService ftpsservice= GetSpringUtil.getBean("ftpsservice");
        String flag= ftpsservice.getFlag();
        String ftpsRemotePath = ftpsservice.getFtpsRemotePath();

        String nowTime = DateTimeUtil.getDateofFormatString();
        String yearMonth = DateTimeUtil.getMonthDateString();
        if(CollectionUtils.isNotEmpty(differentList) && ("1".equals(flag))){
            log.info("判别告警类型:开始向算法管理平台发送图片和mqtt消息");

            HashMap<String, String> nameMap = analyseDataOperateService.selectDeviceNameInfo(Long.valueOf(instanceId));
            String picF = nowTime + "_" + nameMap.get("upRegionName") + "_" + nameMap.get("deviceName") + "_" + nameMap.get("meteName") + "_";

            // 先取出算法平台返回的resultinfo中的结果图片路径
            String resultImagebak = "analyseResultImg";
            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId+":"+ instanceId);
            String deviceName = Optional.ofNullable(cruiseResultMap.get("deviceName")).orElse("");
            String origPicPath = Optional.ofNullable(cruiseResultMap.get("origpic")).orElse("");

            String remoteorigfilepath = ftpsRemotePath + "/" +"判别"+"/"+yearMonth+"/"+picF+"原图.jpg";
            //,获取结果路径.并拼接算法管理平台对应远程文件路径
            String remotefilepath=ftpsRemotePath + "/" +"判别"+"/"+yearMonth+"/"+picF+"判别告警.jpg";
            //,获取基准路径.并拼接算法管理平台所需要的基准文件路径
            TCruisePointInstance tCruisePointInstance = analyseDataOperateService.selectPointInstance(Long.valueOf(instanceId));
            String Cruiseid=String.valueOf(tCruisePointInstance.getCruiseid());   //获取巡视点位id
            String judgeBaseImagepath= redisTemplate.opsForHash().get("t_sys_param:presetImgPath","content").toString();
            judgeBaseImagepath=judgeBaseImagepath+"/"+Cruiseid+"/"+Cruiseid+".jpg"; //判定基准图路径位presetImgPath+巡视点+巡视点.jpg
            //拼接算法管理平台分析告警结果图片地址
            String remotebaseimagicpath=ftpsRemotePath + "/" +"判别"+"/"+yearMonth+"/"+picF+"判别基准.jpg";

            Iterator it = differentList.iterator();
            List<Different> defectTempList = new ArrayList<>();
            Alarm alarmDetail = new Alarm();
            while (it.hasNext()){
                String key = it.next().toString();
                Map<String, String> differentListMap= redisTemplate.opsForHash().entries(key);
                String resultValue = differentListMap.get("value");
                log.info("判别结果：{}",resultValue);
                Different different = new Different();
                String[] re = resultValue.split(",");
                if(re != null && re.length > 4){
                    different.setX1((int) NumberUtils.toDouble(re[1]));
                    different.setY1((int) NumberUtils.toDouble(re[2]));
                    different.setX2((int) NumberUtils.toDouble(re[3]));
                    different.setY2((int) NumberUtils.toDouble(re[4]));
                }else {
                    different.setX1((int) NumberUtils.toDouble(resultValue));
                    different.setY1(0);
                    different.setX2(0);
                    different.setY2(0);
                }
                defectTempList.add(different);
                alarmDetail.setBay_name(nameMap.get("upRegionName"));
                alarmDetail.setDevice_name(deviceName);
                alarmDetail.setPoint_name(nameMap.get("meteName"));
                alarmDetail.setTime(DateTimeUtil.format(new Date()));
                // 原图
                alarmDetail.setPic_raw(remoteorigfilepath);
                // 判别基准图
                alarmDetail.setPic_diff_base(remotebaseimagicpath);
                // 判别结果图
                alarmDetail.setPic_different(remotefilepath);
                // 缺陷告警图
                alarmDetail.setPic_defect("");
            }
            alarmDetail.setDifferent(defectTempList);
            // 原始图片上传
            ftpsservice.uploadFile("判别告警", origPicPath, remoteorigfilepath);
            // 判别基准图片上传
            ftpsservice.uploadFile("判别告警", judgeBaseImagepath, remotebaseimagicpath);
            // 判别结果图片上传
            ftpsservice.uploadFile("判别告警", resultImagebak, remotefilepath);
            log.info("判别预算法主机origpicpath:{}， remoteorigfilepath:{}", origPicPath, remoteorigfilepath);
            log.info("判别预算法主机judgeBaseImagepath:{}， remotebaseimagicpath:{}", judgeBaseImagepath, remotebaseimagicpath);
            log.info("判别预算法主机resultImagebak:{}， remotefilepath:{}", resultImagebak, remotefilepath);
            //可靠性 文件是否传输成功
            if( !ftpsservice.fileExits(remoteorigfilepath)){
                ftpsservice.uploadFile("判别告警", origPicPath, remoteorigfilepath);
            }
            if( !ftpsservice.fileExits(remotebaseimagicpath)){
                ftpsservice.uploadFile("判别告警", judgeBaseImagepath, remotebaseimagicpath);
            }
            if( !ftpsservice.fileExits(remotefilepath)){
                ftpsservice.uploadFile("判别告警", resultImagebak, remotefilepath);
            }
            AlarmService alarmService= GetSpringUtil.getBean("alarmService");
            alarmService.PushMsg(alarmDetail);
            log.info("判别告警发送算法管理平台结束");

            // 判别上报上一级系统
            processResultToUpSystem.defectAndDistinguishToUpSystem(cruiseResultMap, differentList);
        }

        if(CollectionUtils.isNotEmpty(defectList) && ("1".equals(flag))) {
            log.info("缺陷告警类型:开始向算法管理平台发送图片和mqtt消息");

            HashMap<String, String> nameMap = analyseDataOperateService.selectDeviceNameInfo(Long.valueOf(instanceId));
            String picF = nowTime + "_" + nameMap.get("upRegionName") + "_" + nameMap.get("deviceName") + "_" + nameMap.get("meteName") + "_";

            // 先取出算法平台返回的resultinfo中的结果图片路径
            String resultImagebak = "analyseResultImg";
            Map<String, String> cruiseResultMap = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + instanceId);
            String deviceName = Optional.ofNullable(cruiseResultMap.get("deviceName")).orElse("");
            String origPicPath = Optional.ofNullable(cruiseResultMap.get("origpic")).orElse("");

            //拼接算法管理平台原始图片推送地址
            String remoteorigfilepath = ftpsRemotePath + "/" + "缺陷" + "/" + yearMonth + "/" + picF + "原图.jpg";
            //拼接算法管理平台分析告警结果图片地址
            String remotefilepath = ftpsRemotePath + "/" + "缺陷" + "/" + yearMonth + "/" + picF + "缺陷告警.jpg";

            Iterator it = defectList.iterator();
            Alarm alarmDetail = new Alarm();
            while (it.hasNext()) {
                String key = it.next().toString();
                Map<String, String> differentListMap = redisTemplate.opsForHash().entries(key);
                // 获取返回的resultvalue值，这个值就是缺陷和判别的x,y位置信息
                String resultinfo = differentListMap.get("value");
                log.info("缺陷结果：{}", resultinfo);
                // 目前格式："wcaqm,1049.0,216.0,1211.0,389.0,0.8829"
                String[] arr1 = resultinfo.split(",");
                List<Defect> defectTempList = new ArrayList<>();
                for (int i = 0; i < arr1.length; ) {
                    Defect defect = new Defect();
                    defect.setX1((int) NumberUtils.toDouble(arr1[i + 1]));
                    defect.setY1((int) NumberUtils.toDouble(arr1[i + 2]));
                    defect.setX2((int) NumberUtils.toDouble(arr1[i + 3]));
                    defect.setY2((int) NumberUtils.toDouble(arr1[i + 4]));
                    defect.setType(arr1[i]);
                    int confidence = (int) (NumberUtils.toDouble(arr1[i + 5]) * 100);
                    defect.setConfidence(confidence);
                    defect.setDesc(differentListMap.get("defectContent") +
                            "(坐标位置 " + defect.getX1() + "," + defect.getY1() + "," + defect.getX2() + "," + defect.getY2() + ";" +
                            "置信度 " + confidence + "%)"
                    );
                    defectTempList.add(defect);
                    i = i + 6;
                }
                alarmDetail.setDefect(defectTempList);
                alarmDetail.setBay_name(nameMap.get("upRegionName"));
                alarmDetail.setDevice_name(deviceName);
                alarmDetail.setPoint_name(nameMap.get("meteName"));
                alarmDetail.setTime(DateTimeUtil.format(new Date()));
                // 原图
                alarmDetail.setPic_raw(remoteorigfilepath);
                // 判别基准图
                alarmDetail.setPic_diff_base("");
                // 判别结果图
                alarmDetail.setPic_different("");
                // 缺陷告警图
                alarmDetail.setPic_defect(remotefilepath);
            }
            // 原始图片上传
            ftpsservice.uploadFile("遥信告警", origPicPath, remoteorigfilepath);
            // 缺陷结果图片上传
            ftpsservice.uploadFile("遥信告警", resultImagebak, remotefilepath);
            if (!ftpsservice.fileExits(remoteorigfilepath)) {
                ftpsservice.uploadFile("遥信告警", origPicPath, remoteorigfilepath);
            }
            if (!ftpsservice.fileExits(remotefilepath)) {
                ftpsservice.uploadFile("遥信告警", resultImagebak, remotefilepath);
            }

            log.info("巡视主机与智能分析主机：origpicpath:{}", origPicPath);
            log.info("巡视主机与智能分析主机：remoteorigfilepath:{}", remoteorigfilepath);
            log.info("巡视主机与智能分析主机：resultImagebak:{}", resultImagebak);
            log.info("巡视主机与智能分析主机：remotefilepath:{}", remotefilepath);
            AlarmService alarmService = GetSpringUtil.getBean("alarmService");
            alarmService.PushMsg(alarmDetail);
            log.info("缺陷告警发送算法管理平台结束");

            // 缺陷上报上一级系统
            processResultToUpSystem.defectAndDistinguishToUpSystem(cruiseResultMap, differentList);
        }
    }

    /**
     * 识别结果处理
     *
     * @param analyseResultImg 巡视结果图
     * @param resultValue 巡视结果值
     * @param cruiseResultMap redis中巡视点结果信息
     * @param tStdDevicemete 测点信息
     * @return String
     */
    private String recognitionHandler(String analyseResultImg, String resultValue, Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete, String firDocPath) {
        String resultImage;
        try {
            boolean dltFlag = NumberUtils.toInt(cruiseResultMap.get("cruiseType")) == 230
                    && Objects.nonNull(cruiseResultMap.get("resultNum"))
                    && !Objects.equals("null", cruiseResultMap.get("resultNum"));

            if(StringUtils.isNotEmpty(analyseResultImg)){
                resultImage = analyseResultImg.replaceAll(
                        (String)redisTemplate.opsForHash().get("t_sys_param:meterResultImg", "content"),
                        (String)redisTemplate.opsForHash().get("t_sys_param:meterResultRealImg", "content"));
                log.info("识别图片=={}", resultImage);

                if (Boolean.TRUE.equals(dltFlag)) {
                    log.info("dltFlag {}, dlt664抓图", dltFlag);
                }else{
                    cruiseResultMap.put("picpath", resultImage);
                }
            }

            if (StringUtils.equals("数据错误", resultValue)){
                cruiseResultMap.put("resultNum", resultValue + ",缺少标定文件");
                cruiseResultMap.put("cruiseResult", "247");
                cruiseResultMap.put("cruiseAbnormal", "249");
            }else if (StringUtils.equals("未获得读数", resultValue)){
                cruiseResultMap.put("resultNum", resultValue + ",识别失败");
                cruiseResultMap.put("cruiseResult", "247");
                cruiseResultMap.put("cruiseAbnormal", "249");
            }else {
                // 正常的识别
                String resultStringValue = dltFlag ? cruiseResultMap.get("resultNum") : resultValue;
                // 红外会返回两个温度(如：12.3,2.3),所以需要这样取值
                resultStringValue = resultStringValue.split(",")[0];
                log.info("resultStringValue=={}", resultStringValue);

                normalRecognitionHandler(resultValue, cruiseResultMap, tStdDevicemete, resultStringValue, firDocPath);
            }
        }catch (Exception e){
            log.error("识别结果处理异常：", e);
        }
        return resultValue;
    }

    /**
     * 正常识别结果处理
     *
     * @param resultValue 算法分析返回的巡视结果
     * @param cruiseResultMap redis中巡视点结果信息
     * @param tStdDevicemete 测点信息
     * @param resultStringValue 处理后的结果值
     * @return void
     */
    private void normalRecognitionHandler(String resultValue, Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete, String resultStringValue, String firDocPath) {
        try {
            if (resultStringValue.matches("^([0-9]{1,})$|^([0-9]{1,}[.][0-9]*)$|^(-[0-9]{1,})$|^(-[0-9]{1,}[.][0-9]*)$|[\\u4E00-\\u9FA5]+")) {
                //判断是否为红外识别且获取FIR文件 放入缓存中
                if(StringUtils.isNotEmpty(firDocPath)) {
                    firDocPath = firDocPath.replaceAll(
                            String.valueOf(redisTemplate.opsForHash().get("t_sys_param:infraredStorePath", "content")),
                            String.valueOf(redisTemplate.opsForHash().get("t_sys_param:infraredRealPath", "content")));
                    cruiseResultMap.put("firDocPath", firDocPath);
                    File file = new File(firDocPath);
                    //放入文件名
                    cruiseResultMap.put("firName", file.getName().substring(0, file.getName().lastIndexOf(".")));
                    log.info("FIR-Doc-----:" + firDocPath);
                }

                log.info("开始告警预处理");
                String meteKind = String.valueOf(tStdDevicemete.getMeteKind());
                String stateZero = tStdDevicemete.getStateZero();
                String stateOne = tStdDevicemete.getStateOne();
                Integer alarmState = tStdDevicemete.getAlarmState();
                Float highLimit1 = tStdDevicemete.getHighLimit1();
                Float lowLimit1 = tStdDevicemete.getLowLimit1();
                Float highLimit2 = tStdDevicemete.getHighLimit2();
                Float lowLimit2 = tStdDevicemete.getLowLimit2();
                Float highLimit3 = tStdDevicemete.getHighLimit3();
                Float lowLimit3 = tStdDevicemete.getLowLimit3();
                Float highLimit4 = tStdDevicemete.getHighLimit4();
                Float lowLimit4 = tStdDevicemete.getLowLimit4();
                String meteName = tStdDevicemete.getMeteName();
                int warnFlag = analyseDataOperateService.warnSettings(meteKind, stateZero, alarmState, highLimit1, lowLimit1,
                        highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
                if (warnFlag == 1){
                    Map<String, String> warnMap = new HashMap<>();
                    String warnName = "warnInfo:" + cruiseResultMap.get("taskId") + String.valueOf(UUID.randomUUID()).replace("-", "");

                    warnMap.put("deviceId", String.valueOf(cruiseResultMap.get("deviceId")));
                    warnMap.put("customId", tStdDevicemete.getCustomId());
                    warnMap.put("instanceId", cruiseResultMap.get("instanceId"));
                    warnMap.put("stdMeteId", String.valueOf(tStdDevicemete.getDeviceMeteId()));
                    warnMap.put("taskId", cruiseResultMap.get("taskId"));
                    warnMap.put("value", resultStringValue);
                    warnMap.put("imagePath", cruiseResultMap.get("picpath"));
                    warnMap.put("confMode", "276");
                    warnMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
                    warnMap.put("defectModel", analyseDataOperateService.selectDictCode("defect_model", "其他"));

                    String alarmLevel = "";
                    switch (meteKind){
                        case "1":
                            int warnRuleFlag = analyseDataOperateService.warnJudgementTelesignaling(resultValue, stateZero, stateOne, alarmState);
                            if (warnRuleFlag == 1){
                                log.info("达到告警值(遥信)");
                                alarmLevel = String.valueOf(tStdDevicemete.getAlarmLevel());
                                warnMap.put("warnLevel", String.valueOf(tStdDevicemete.getAlarmLevel()));
                                warnMap.put("warnName", meteName + "状态异常");
                                warnMap.put("warnTime", DateTimeUtil.format(new Date()));
                                warnMap.put("outRange", "--");
                                if (alarmState == 0) {
                                    warnMap.put("warnContent", meteName + ":" + stateZero + "--" + analyseDataOperateService.selectDictNote(String.valueOf(tStdDevicemete.getAlarmLevel()), "alarm_level"));
                                } else {
                                    warnMap.put("warnContent", meteName + ":" + stateOne + "--" + analyseDataOperateService.selectDictNote(String.valueOf(tStdDevicemete.getAlarmLevel()), "alarm_level"));
                                }
                                log.info("warnMap=={}", warnMap);
                                redisTemplate.opsForHash().putAll(warnName, warnMap);

                                cruiseResultMap.put("isWarn", "1");
                                cruiseResultMap.put("resultNum", resultStringValue);
                                cruiseResultMap.put("cruiseResult", "247");
                                cruiseResultMap.put("cruiseAbnormal", "250");
                            }else {
                                log.info("未达到告警值(遥信)");
                                cruiseResultMap.put("resultNum", resultValue);
                                cruiseResultMap.put("cruiseResult", "246");
                                cruiseResultMap.put("cruiseAbnormal", "--");
                            }
                            break;
                        case "2":
                            Float resultValueMeter = NumberUtils.toFloat(resultStringValue);
                            int warnRuleMeter = analyseDataOperateService.warnJudgement(resultValueMeter, highLimit1, lowLimit1,
                                    highLimit2, lowLimit2, highLimit3, lowLimit3, highLimit4, lowLimit4);
                            log.info("warnRuleMeter=={}", warnRuleMeter);

                            if (warnRuleMeter > 0){
                                log.info("达到告警值(遥测)");
                                warnMap.put("warnName", meteName + "数据异常");
                                warnMap.put("warnTime", DateTimeUtil.format(new Date()));

                                switch (warnRuleMeter){
                                    case 1:
                                        warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "预警"));
                                        warnMap.put("warnContent", meteName + ":" + cruiseResultMap.get("resultNum") + "--" + "预警");
                                        alarmLevel = "1";
                                        warnMap.put("outRange", resultValueMeter >= highLimit1 ?
                                                String.valueOf(resultValueMeter - highLimit1) : String.valueOf(lowLimit1 - resultValueMeter));
                                        break;
                                    case 2:
                                        warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "一般告警"));
                                        warnMap.put("warnContent", meteName + ":" + cruiseResultMap.get("resultNum") + "--" + "一般告警");
                                        alarmLevel = "2";
                                        warnMap.put("outRange", resultValueMeter >= highLimit2 ?
                                                String.valueOf(resultValueMeter - highLimit2) : String.valueOf(lowLimit2 - resultValueMeter));
                                        break;
                                    case 3:
                                        warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "严重告警"));
                                        warnMap.put("warnContent", meteName + ":" + cruiseResultMap.get("resultNum") + "--" + "严重告警");
                                        alarmLevel = "3";
                                        warnMap.put("outRange", resultValueMeter >= highLimit3 ?
                                                String.valueOf(resultValueMeter - highLimit3) : String.valueOf(lowLimit3 - resultValueMeter));
                                        break;
                                    case 4:
                                        warnMap.put("warnLevel", analyseDataOperateService.selectDictCode("alarm_level", "危急告警"));
                                        warnMap.put("warnContent", meteName + ":" + cruiseResultMap.get("resultNum") + "--" + "危急告警");
                                        alarmLevel = "4";
                                        warnMap.put("outRange", resultValueMeter >= highLimit4 ?
                                                String.valueOf(resultValueMeter - highLimit4) : String.valueOf(lowLimit4 - resultValueMeter));
                                        break;
                                    default:
                                        break;
                                }
                                log.info("warnMap=={}", warnMap);
                                redisTemplate.opsForHash().putAll(warnName, warnMap);

                                cruiseResultMap.put("isWarn", "1");
                                cruiseResultMap.put("resultNum", resultValue);
                                cruiseResultMap.put("cruiseResult", "247");
                                cruiseResultMap.put("cruiseAbnormal", "250");
                            }else {
                                log.info("未达到告警值(遥测)");
                                cruiseResultMap.put("resultNum", resultValue);
                                cruiseResultMap.put("cruiseResult", "246");
                                cruiseResultMap.put("cruiseAbnormal", "--");
                            }
                            break;
                        default:
                            break;
                    }

                    Map<String, String> warningMsg = redisTemplate.opsForHash().entries(warnName);
                    log.info("warnMsg==={}", warningMsg);
                    if (!warningMsg.isEmpty()) {
                        TWarnInfo tWarnInfo =  getWarnInfo(warningMsg);
                        analyseDataOperateService.insertWarnInfo(tWarnInfo);

                        Map<String, String> infoMap = new HashMap<>(5);
                        infoMap.put("alarmLevel", String.valueOf(tWarnInfo.getWarnLevel()));
                        infoMap.put("flag", "warn");
                        infoMap.put("defectModel", String.valueOf(tWarnInfo.getDefectModel()));
                        alarmPopUp(tStdDevicemete, infoMap);

                        pushAlarmInfo(warningMsg.get("warnName"), warningMsg.get("warnContent"));

                        // 告警上报上一级系统
                        processResultToUpSystem.alarmAndResultToUpSystem(cruiseResultMap, alarmLevel, tWarnInfo);
                    }
                }else {
                    cruiseResultMap.put("resultNum", resultValue);
                    cruiseResultMap.put("cruiseResult", "246");
                    cruiseResultMap.put("cruiseAbnormal", "--");
                }
            }else {
                cruiseResultMap.put("resultNum", resultValue);
                cruiseResultMap.put("cruiseResult", "247");
                cruiseResultMap.put("cruiseAbnormal", "249");
            }
        }catch (Exception e){
            log.error("正常识别结果处理异常：", e);
        }
    }

    /**
     * 告警info信息组装
     *
     * @param warningMsg 缺陷map信息
     * @return TWarnInfo
     */
    private TWarnInfo getWarnInfo(Map<String, String> warningMsg) {
        TWarnInfo tWarnInfo = new TWarnInfo();
        tWarnInfo.setWarnLevel(NumberUtils.toInt(warningMsg.get("warnLevel")));
        tWarnInfo.setDeviceId(NumberUtils.toLong(warningMsg.get("deviceId")));
        tWarnInfo.setCunstomId(warningMsg.get("customId"));
        tWarnInfo.setInstanceId(NumberUtils.toLong(warningMsg.get("instanceId")));
        tWarnInfo.setStdMeteId(NumberUtils.toLong(warningMsg.get("stdMeteId")));
        tWarnInfo.setTaskId(warningMsg.get("taskId"));
        tWarnInfo.setValue(warningMsg.get("value"));
        tWarnInfo.setConfMode(NumberUtils.toInt(warningMsg.get("confMode")));
        tWarnInfo.setImagePath(warningMsg.get("imagePath"));
        tWarnInfo.setAlarmSource(NumberUtils.toInt(warningMsg.get("alarmSource")));
        tWarnInfo.setWarnName(warningMsg.get("warnName"));
        tWarnInfo.setOutRange(warningMsg.get("outRange"));
        tWarnInfo.setWarnContent(warningMsg.get("warnContent"));
        tWarnInfo.setDefectModel(NumberUtils.toInt(warningMsg.get("defectModel")));
        tWarnInfo.setWarnTime(DateTimeUtil.parse(warningMsg.get("warnTime")));
        return tWarnInfo;
    }

    /**
     * 缺陷结果处理
     *
     * @param msgID 随机数
     * @param analyseResultImg 巡视结果图
     * @param resultValue 巡视结果值
     * @param cruiseResultMap redis中巡视点结果信息
     * @param tStdDevicemete 测点信息
     */
    private void defectHandler(String msgID, String analyseResultImg, String resultValue, Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete)  {
        String resultImage;
        try {
            if (StringUtils.isNotEmpty(analyseResultImg)){
                resultImage = analyseResultImg.replaceAll(
                        (String)redisTemplate.opsForHash().get("t_sys_param:defectResultImg","content"),
                        (String)redisTemplate.opsForHash().get("t_sys_param:defectResultRealImg","content"));
            }else {
                resultImage = analyseResultImg.replaceAll(
                        (String)redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content"),
                        (String)redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content"));
            }
            log.info("缺陷图片=={}", resultImage);

            resultValue = analyseDataOperateService.resolveDefectResult(resultValue);
            log.info("解析的缺陷数据==={}", resultValue);

            cruiseResultMap.put("resultNum", resultValue);
            cruiseResultMap.put("picpath", resultImage);
            if (!"null".equals(resultValue) && !(resultValue.contains("device"))) {
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                cruiseResultMap.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_DEFECT));

                List<TDefectInfo> defectInfoList = new ArrayList<>();
                String[] resultArr = resultValue.split("\\s+");
                if (resultArr.length == 1) {
                    log.info("只产生了一条缺陷！！！");
                    // 缺陷信息存redis
                    String redisKeyTemp = String.valueOf(UUID.randomUUID()).replace("-", "");
                    Map<String, String> defectMap = getDefectMap(analyseResultImg, resultValue, cruiseResultMap, tStdDevicemete);
                    log.info("defectMap=={}", JSON.toJSONString(defectMap));
                    redisTemplate.opsForHash().putAll("defectInfo:" + cruiseResultMap.get("taskId") + ":" + redisKeyTemp, defectMap);
                    redisTemplate.opsForHash().putAll("defect:" + msgID + ":" + redisKeyTemp, defectMap);

                    TDefectInfo tDefectInfo = getDefectInfo(defectMap);
                    log.info("tDefectInfo=={}", JSON.toJSONString(tDefectInfo));
                    defectInfoList.add(tDefectInfo);

                    // 缺陷告警推送
                    Map<String, String> infoMap = new HashMap<>(5);
                    infoMap.put("alarmLevel", defectMap.get("defectLevel"));
                    infoMap.put("flag", "defect");
                    infoMap.put("defectModel", defectMap.get("defectType"));
                    alarmPopUp(tStdDevicemete, infoMap);

                    pushAlarmInfo(resultValue, tStdDevicemete.getMeteName() + "--" + resultValue);

                } else if (resultArr.length > 1) {
                    log.info("产生了多条缺陷！！！");
                    String defectNames = "";
                    for (String s : resultArr) {
                        // 缺陷信息存redis
                        String redisKeyTemp = String.valueOf(UUID.randomUUID()).replace("-", "");
                        Map<String, String> defectMap = getDefectMap(analyseResultImg, resultValue, cruiseResultMap, tStdDevicemete);
                        log.info("defectMap=={}", JSON.toJSONString(defectMap));
                        redisTemplate.opsForHash().putAll("defectInfo:" + cruiseResultMap.get("taskId") + ":" + redisKeyTemp, defectMap);
                        redisTemplate.opsForHash().putAll("defect:" + msgID + ":" + redisKeyTemp, defectMap);

                        TDefectInfo tDefectInfo = getDefectInfo(defectMap);
                        log.info("tDefectInfo=={}", JSON.toJSONString(tDefectInfo));
                        defectInfoList.add(tDefectInfo);

                        // 缺陷告警推送
                        Map<String, String> infoMap = new HashMap<>(5);
                        infoMap.put("alarmLevel", defectMap.get("defectLevel"));
                        infoMap.put("flag", "defect");
                        infoMap.put("defectModel", defectMap.get("defectType"));
                        alarmPopUp(tStdDevicemete, infoMap);
                        defectNames = defectNames + s + " ";
                    }

                    pushAlarmInfo(defectNames, tStdDevicemete.getMeteName() + "--" + resultValue);
                }

                // 缺陷批量实时入库
                if (CollectionUtils.isNotEmpty(defectInfoList)) {
                    analyseDataOperateService.batchInsertDefectInfo(defectInfoList);
                }
                log.info("--------缺陷入库完成-----");
            }else {
                cruiseResultMap.put("resultNum", resultValue.contains("device") ? resultValue.replaceAll("device","") : "--");
                cruiseResultMap.put("cruiseResult", String.valueOf(CRUISE_RESULT_NORMAL));
                cruiseResultMap.put("cruiseAbnormal", "--");
            }
        }catch (Exception e){
            log.error("缺陷结果处理异常：", e);
        }
    }

    /**
     * webSocket通知前端刷新告警统计数量
     *
     * @param resultValue 告警名称
     * @param warnContent 告警内容
     * @return void
     */
    private void pushAlarmInfo(String resultValue, String warnContent) {
        try {
            Map<String, String> jasonMaps = new HashMap<>(5);
            jasonMaps.put("type", "newAlarm");
            jasonMaps.put("alarmName", resultValue);
            jasonMaps.put("alarmTime", DateTimeUtil.format(new Date()));
            jasonMaps.put("alarmContent", warnContent);
            log.info("告警生成-前端推送：{}", JSON.toJSONString(jasonMaps));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 判断该测点是否设置了告警推送
     * 若是,则将配置的告警信息组成告警弹框所需内容推给前端
     *
     * @param tStdDevicemete 测点信息
     * @param infoMap 告警信息
     */
    public void alarmPopUp(TStdDeviceMete tStdDevicemete, Map<String, String> infoMap) {
        // 弹框PlanB-推送
        Map<String,String> currentWarnInfo = new HashMap<>();
        try {
            String warnId = String.valueOf(analyseDataOperateService.selectCurrentWarn());
            currentWarnInfo.put("warnId", warnId);
            currentWarnInfo.put("defectModel", infoMap.get("defectModel"));
            currentWarnInfo.put("isPop", "false");

            Integer alarmLevel = NumberUtils.toInt(infoMap.get("alarmLevel"));
            boolean isSet = StringUtils.isNotEmpty(tStdDevicemete.getAlarmNote()) && StringUtils.equals("1", tStdDevicemete.getAlarmNote());
            boolean reachDefectLevel = Objects.equals(133, alarmLevel);
            boolean reachAlarmLevel = Objects.nonNull(tStdDevicemete.getAlarmLevel()) &&
                    (alarmLevel.compareTo(tStdDevicemete.getAlarmLevel()) == 0 || alarmLevel > tStdDevicemete.getAlarmLevel());

            boolean reachWarnCondition;
            if (StringUtils.equals("warn", infoMap.get("flag"))){
                reachWarnCondition = Boolean.TRUE.equals(isSet) && Boolean.TRUE.equals(reachAlarmLevel);
            }else {
                reachWarnCondition = Boolean.TRUE.equals(isSet) && Boolean.TRUE.equals(reachDefectLevel);
            }

            if (Boolean.TRUE.equals(reachWarnCondition)){
                //webSocket通知前端调用查询告警弹框的接口
                Map<String, String> jasonMaps = new HashMap<>();
                jasonMaps.put("type", "alarmPopUp");
                jasonMaps.put("warnId", warnId);
                jasonMaps.put("defectModel", infoMap.get("defectModel"));
                log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMaps));
                currentWarnInfo.put("isPop","true");
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMaps);
            }
            redisTemplate.opsForValue().set("currentWarn", currentWarnInfo, 3, TimeUnit.MINUTES);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 缺陷info信息组装
     *
     * @param defectMap 缺陷map信息
     * @return TDefectInfo
     */
    private TDefectInfo getDefectInfo(Map<String, String> defectMap) {
        TDefectInfo tDefectInfo = new TDefectInfo();
        try {
            tDefectInfo.setDefectLevel(NumberUtils.toInt(defectMap.get("defectLevel")));
            tDefectInfo.setDefectTime(DateTimeUtil.parse(defectMap.get("defectTime")));
            tDefectInfo.setDefectType(NumberUtils.toInt(defectMap.get("defectType")));
            tDefectInfo.setDefectContent(defectMap.get("defectContent"));
            tDefectInfo.setDeviceId(NumberUtils.toLong(defectMap.get("deviceId")));
            tDefectInfo.setInstanceId(NumberUtils.toLong(defectMap.get("instanceId")));
            tDefectInfo.setCunstomId(defectMap.get("customId"));
            tDefectInfo.setStdMeteId(NumberUtils.toLong(defectMap.get("stdMeteId")));
            tDefectInfo.setConfMode(NumberUtils.toInt(defectMap.get("confMode")));
            tDefectInfo.setImagePath(defectMap.get("imagePath"));
            tDefectInfo.setAlarmSource(NumberUtils.toInt(defectMap.get("alarmSource")));
        }catch (Exception e){
            log.error("组装缺陷info信息异常：", e);
        }
        return tDefectInfo;
    }

    /**
     * 缺陷map信息组装
     *
     * @param analyseResultImg 缺陷结果图片
     * @param resultValue 缺陷结果
     * @param cruiseResultMap redis中巡视点结果信息
     * @param tStdDevicemete 测点信息
     * @return Map<String,String>
     */
    private Map<String, String> getDefectMap(String analyseResultImg, String resultValue, Map<String, String> cruiseResultMap, TStdDeviceMete tStdDevicemete) {
        Map<String, String> defectMap = new HashMap<>(16);
        try {
            defectMap.put("defectType", analyseDataOperateService.selectDictCode("defect_model", resultValue));
            defectMap.put("defectLevel", analyseDataOperateService.selectAlgorithmDefectInfo(defectMap.get("defectType")));
            defectMap.put("defectContent", resultValue);
            defectMap.put("deviceId", cruiseResultMap.get("deviceId"));
            defectMap.put("instanceId", cruiseResultMap.get("instanceId"));
            defectMap.put("customId", tStdDevicemete.getCustomId());
            defectMap.put("stdMeteId", String.valueOf(tStdDevicemete.getDeviceMeteId()));
            defectMap.put("confMode", analyseDataOperateService.selectDictCode("conf_mode", "未核查"));
            defectMap.put("imagePath", analyseResultImg);
            defectMap.put("alarmSource", analyseDataOperateService.selectDictCode("alarm_source", "主辅设备"));
            defectMap.put("defectTime", DateTimeUtil.format(new Date()));
            defectMap.put("value", resultValue);
        }catch (Exception e){
            log.error("组装缺陷map信息异常：" , e);
        }
        return defectMap;
    }

    /**
     * 判别结果处理
     *
     * @param msgID 随机数
     * @param analyseResultImg 巡视结构图
     * @param resultValue 巡视结果值
     * @param cruiseResultMap redis中巡视点结果信息
     */
    private void distinguishHandler(String msgID, String analyseResultImg, String resultValue, Map<String, String> cruiseResultMap) {
        String resultImage;
        try {
            if (StringUtils.isNotEmpty(analyseResultImg)){
                resultImage = analyseResultImg.replaceAll(
                        (String)redisTemplate.opsForHash().get("t_sys_param:judgeResultImg","content"),
                        (String)redisTemplate.opsForHash().get("t_sys_param:judgeResultRealImg","content"));
            }else {
                resultImage = analyseResultImg.replaceAll(
                        (String)redisTemplate.opsForHash().get("t_sys_param:resultImgPath", "content"),
                        (String)redisTemplate.opsForHash().get("t_sys_param:resultImgRealPath", "content"));
            }
            log.info("判别图片=={}", resultImage);

            resultValue = analyseDataOperateService.resolveDefectResult(resultValue);
            log.info("解析的判别数据==={}", resultValue);

            cruiseResultMap.put("resultNum", resultValue);
            cruiseResultMap.put("picpath", resultImage);
            cruiseResultMap.put("cruiseResultMap", StringUtils.equals("abnormal", resultValue) ?
                    String.valueOf(CRUISE_RESULT_ABNORMAL) : String.valueOf(CRUISE_RESULT_NORMAL));
            cruiseResultMap.put("cruiseAbnormal", "--");

            //判别异常
            String msgName = "msg:" + msgID + ":" + String.valueOf(UUID.randomUUID()).replace("-", "");
            redisTemplate.opsForHash().put(msgName, "value", resultValue);
        }catch (Exception e){
            log.error("判别结果处理异常：", e);
        }
    }

    /**
     * Redis数据库批量查询Key值游标
     * @param key redis的key
     * @return Set<String>
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
