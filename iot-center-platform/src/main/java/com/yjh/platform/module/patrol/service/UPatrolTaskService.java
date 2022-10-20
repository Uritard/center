/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.quartz.JobManager;
import com.yjh.platform.common.quartz.QuartzTask;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.FileUtil;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.common.utils.smUtil.Demo;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.TCruisePointInstanceNameDetail;
import com.yjh.platform.module.device.entity.TStdDeviceMete;
import com.yjh.platform.module.patrol.dao.UPatrolPlanAttrDao;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskAttrDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskDao;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.patrol.thread.InspectionResultThread;
import com.yjh.platform.module.patrol.thread.IsWarnAfterCruiseThread;
import com.yjh.platform.module.task.dao.TCruiseTaskDelDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.threadpool.TaskExecutePool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.yjh.platform.module.patrol.service.CruiseInspectionExecute.*;

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
    public static final String PATROL_SUMMARY_PREFIX = "patrol_point_summary:";

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
            if (uPatrolTask.getExecuteType() == 172) {
                Date startTime = format.parse("2000-01-01 00:00:00");
                uPatrolTask.setStartTime(startTime);
            }
        } catch (Exception e) {
            log.error("设置周期任务起始时间出错：", e);
        }
        if (Objects.isNull(uPatrolTask.getTaskId())) {
            uPatrolTask.setTaskId(String.valueOf(UUID.randomUUID()).replace("-", ""));
        }
        if (Objects.isNull(uPatrolTask.getTaskCode())){
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
                if (Objects.equals(173, ifRun)) {
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
            uPatrolTaskAttr.setCustomId(uPatrolPlanAttr.getCustomId());
            uPatrolTaskAttr.setPointTaskId(uPatrolTaskAttr.getPointTaskId());
            instanceList.add(uPatrolPlanAttr.getInstanceId());
            uPatrolTaskAttrs.add(uPatrolTaskAttr);
        }
        uPatrolTaskDao.add(uPatrolTask);
        this.uPatrolTaskAttrDao.batchAdd(uPatrolTaskAttrs);
        return instanceList;
    }


    public List<TCruisePointInstanceNameDetail> initializeTaskInfo(List<Long> instanceList, UPatrolTask task) {
        UPatrolResult uPatrolResult = new UPatrolResult();
        uPatrolResult.setTaskId(task.getTaskId())
                .setTaskName(task.getTaskName())
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
            Map map = Object2Map.toStringMap(Object2Map.objectToMap(uPatrolDataResult,true));
            map.put("cameraId",String.valueOf(item.getCameraId()));
            String str = "t_cruise_task_result:" + task.getTaskId() + ":" + item.getInstanceId();
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
        for (RobotPatrolTaskResult robotPatrolTaskResult : resultList) {
            Map<String, String> infoMap = new HashMap<>(5);

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
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 对机器人/无人机结果文件处理
     *
     * @param robotPatrolTaskResult 机器人/无人机巡视结果
     * @param infoMap                任务结果其他信息
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

            switch (fileType){
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
            log.error(e.getMessage(), e);
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
     * @return void
     */
    public void robotPatrolTaskStatus(List<RobotPatrolTaskStatus> statusList){
        return;
    }

    /**
     * 找出机器人和无人机的点让其做任务
     *
     * @param task    任务信息
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
                if ((228 == item.getCruiseType() || 524 == item.getCruiseType())) {
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
            switch (ifFun) {
                //立即任务
                case "173":
                    taskInfo.setFixedStartTime(format.format(new Date()));
                    break;
                //定时任务
                case "174":
                    taskInfo.setFixedStartTime(format.format(tCruiseTaskAdd.getStartTime()));
                    break;
                // 周期和间隔任务
                case "172":
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
            if (task.getExecuteType() == 173) {
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
            if (task.getExecuteType() == 172) {
                try {
                    //CronExpression expression = new CronExpression(tCruiseTask.getDateType());
                    item.put("start_time", DateTimeUtil.format(task.getStartTime()));
                } catch (Exception e) {
                    log.info("上报出错{}", e);
                }
            } else {
                item.put("start_time", DateTimeUtil.format(task.getStartTime()));
            }
            item.put("task_progress", "0%");
            Integer i = 0;
            if (state == 5) {
                i = uPatrolTaskDao.countInstance(task.getTaskId());
            } else {
                Map<String, String> mapForGet = redisTemplate.opsForHash().entries("countForAbnormal:" + task.getTaskId());
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
        if (Objects.nonNull(task.getExecuteType()) && task.getExecuteType() == 172) {
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
        } else if (Objects.nonNull(task.getExecuteType()) && task.getExecuteType() == 174) {
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
        taskResult.setTaskState(241);
        try {
            //Thread.sleep(10000);
            //机器人任务暂停
            Map<String,String> jasonMapOnFinished=new HashMap<>();
            jasonMapOnFinished.put("type","taskChange");
            jasonMapOnFinished.put("taskId",taskId);
            String jsonMessage= JSON.toJSONString(jasonMapOnFinished);
            log.info("发送给前端的消息："+jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);
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
            log.info("机器人任务启动,robotCodeList:{}",robotCodeList);
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
    private void robotTaskStates(Map<String, Object> robotTaskStatesMap) {
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
        uPatrolResult.setTaskState(241);
        try {
            //TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);

            //Thread.sleep(10000);
            //机器人任务暂停
            List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(taskId);
            log.info("机器人任务暂停,robotCodeList:{}",robotCodeList);
            if (robotCodeList != null && robotCodeList.size() > 0) {
                Map<String, Object> robotTaskStatesMap = new HashMap<>();
                robotTaskStatesMap.put("taskId", taskId);
                robotTaskStatesMap.put("commandValue", 2);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                robotTaskStates(robotTaskStatesMap);
            }
            updateTaskStateForRedis(taskId,"241");
            Map<String,String> jasonMapOnFinished=new HashMap<>();
            jasonMapOnFinished.put("type","taskChange");
            jasonMapOnFinished.put("taskId",taskId);
            String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
            log.info("发送给前端的消息："+jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);
        } catch (Exception e) {
            log.error("任务暂停异常: " + e);
            e.printStackTrace();
        }

        //任务状态上报站端
        UPatrolTask uPatrolTask = uPatrolTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(uPatrolTask, 3);

        return uPatrolResultDao.update(uPatrolResult);
    }

    private void updateTaskStateForRedis(String taskId,String state){
        String strForCountAbnormal = "countForAbnormal:"+taskId;
        Map<String,String> map = redisTemplate.opsForHash().entries(strForCountAbnormal);
        map.put("taskState",state);
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskGoOn(String taskId) throws Exception {
        UPatrolResult uPatrolResult = uPatrolTaskDao.selectForTaskId(taskId);

        //机器人任务继续
        List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(taskId);
        log.info("机器人任务继续,robotCodeList:{}",robotCodeList);
        if (robotCodeList != null && robotCodeList.size() > 0) {
            Map<String, Object> robotTaskStatesMap = new HashMap<>();
            robotTaskStatesMap.put("taskId", taskId);
            robotTaskStatesMap.put("commandValue", 3);
            robotTaskStatesMap.put("robotCodeList", robotCodeList);
            robotTaskStates(robotTaskStatesMap);
        }
        if (uPatrolResult.getTaskState() == 240 || uPatrolResult.getTaskState() == 239) {
            return 1;
        }
//        if(Constant.taskStateMap.get(taskId) != null && Constant.taskStateMap.get(taskId) == 1){
//            uPatrolResult.setTaskState(239);
//            return uPatrolResultDao.update(uPatrolResult);
//        }

        updateTaskStateForRedis(taskId,"239");
        videoTaskStart(taskId);

        uPatrolResult.setTaskState(239);
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);


        Map<String,String> jasonMapOnFinished=new HashMap<>();
        jasonMapOnFinished.put("type","taskChange");
        jasonMapOnFinished.put("taskId",taskId);
        String jsonMessage=JSON.toJSONString(jasonMapOnFinished);
        log.info("发送给前端的消息："+jsonMessage);
        Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMapOnFinished);

        //任务状态上报站端
        sendTaskStateToUp(task, 2);
        return uPatrolResultDao.update(uPatrolResult);
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskShutDown(String taskId) {
        UPatrolResult uPatrolResult = uPatrolResultDao.selectByPrimaryId(taskId);
        if (uPatrolResult.getExecuteType() == 240) {
            return 1;
        }
        uPatrolResult.setExecuteType(242);
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);

        List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(taskId);
        Map<String, Object> robotTaskStatesMap = new HashMap<>();
        robotTaskStatesMap.put("taskId", taskId);
        robotTaskStatesMap.put("commandValue", 1);
        robotTaskStatesMap.put("robotCodeList", robotCodeList);
        robotTaskStates(robotTaskStatesMap);

        updateTaskStateForRedis(taskId,"242");
        try {
           // todo 任务终止  结果处理
            log.info("任务终止创建成功=="+taskId);
        } catch (Exception e) {
            log.info("任务终止创建失败" + e);
        }

        //任务状态上报站端
        sendTaskStateToUp(task, 4);
        return uPatrolResultDao.update(uPatrolResult);
    }

    /**
     * 本地任务执行
     */
    public void videoTaskStart(String taskId) {
        Set<String> tasKeys = redisTemplate.keys(PATROL_TASK_PREFIX + taskId + ":*");
        if (CollectionUtils.isEmpty(tasKeys)) {
            log.error("patrol_task_result:{}:* 未查到任务，任务未正确初始化", taskId);
            throw new BusinessException("任务未正确初始化");
        }

        List<Map<String, String>> taskInfoList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>)connection -> {
            tasKeys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
            return null;
        });

        // 查询检修区域
        List<Long> overhaul = tCruisePointInstanceDao.selectTimeIsIn(new Date());
        // 不需要执行的点
        List<Map<String, String>> skipPointList = new ArrayList<>();
        Map<String, List<Map<String, String>>> cruiseGroupMap = new HashMap<>(32);
        Map<Long, Integer> robotOfflineMap = new HashMap<>();

        taskInfoList.forEach(m -> {
            int cruiseStatus = MapUtils.getIntValue(m, "cruiseStatus", CRUISE_STATE_UN);
            String cruiseResult = MapUtils.getString(m, "cruiseResult");
            // 已经执行点位
            if (cruiseStatus != CRUISE_STATE_UN && !CommonUtils.isEmptyOrNullstr(cruiseResult)) {
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
            if (!skipFlag && 228 == cruiseType) {
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
                m.put("createtime", dateTime);
                m.put("endTime", dateTime);
                m.put("cruiseTime", dateTime);
                skipPointList.add(m);
                return;
            }

            switch (cruiseType) {
                case CRUISE_TYPE_VIDEO: // 视频
                case CRUISE_TYPE_INFRARED: // 红外
                    String cameraId = MapUtils.getString(m, "cameraId");
                    if (StringUtils.isEmpty(cameraId)) {
                        log.error("task {} cruise data has no cameraId, {}", taskId, JSON.toJSONString(m));
                    } else {
                        String cameraIp = (String)redisTemplate.opsForHash().get("camera_info:" + cameraId, "cameraIp");
                        cruiseGroup(cruiseGroupMap, cameraIp, m);
                    }
                    break;
                case CRUISE_TYPE_VOICE: // 声纹
                    String cruiseId = MapUtils.getString(m, "cruiseId");
                    if (StringUtils.isEmpty(cruiseId)) {
                        log.error("task {} cruise data has no cruiseId, {}", taskId, JSON.toJSONString(m));
                    } else {
                        cruiseGroup(cruiseGroupMap, cruiseId, m);
                    }
                    break;
                case CRUISE_TYPE_ROBOT: // 机器人
                case CRUISE_TYPE_UAV: // 无人机
                case CRUISE_TYPE_ONLINE: // 在线监控
                default:
                    break;
            }
        });

        // skipPointList
        //     cruiseGroupMap

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
     * @param taskId 任务id
     * @param instanceId 巡视点id
     */
    public void patrolTaskResultHandler(String taskId, Long instanceId){
        String redisKeyName = "t_cruise_task_result:" + taskId + ":" + instanceId;
        Map<String, Object> cruiseResultMap = redisTemplate.opsForHash().entries(redisKeyName);

        // 判断该点巡视类型
        String cruiseType = String.valueOf(cruiseResultMap.get("cruiseType"));

        switch (cruiseType){
            case "228":
            case "521":
                // 机器人和无人机
                break;
            case "229":
            case "230":
                // 可见光和红外
                //
                break;
            case "231":
                // ToDo:在线监控
                break;
            case "232":
                // 声纹
                break;
            default:
                break;
        }

        // 获取当前redis正常异常点位个数并更新
        Integer abnormalCounts;
        Integer normalCounts;
        Integer allCounts;
        synchronized (LOCK_FLAG){
            String strForCountAbnormal = "countForAbnormal:" + taskId;
            Map<String, Object> resultCountsMap  = redisTemplate.opsForHash().entries(strForCountAbnormal);
            abnormalCounts = Integer.valueOf(String.valueOf(resultCountsMap.get("abnormal")));
            normalCounts = Integer.valueOf(String.valueOf(resultCountsMap.get("normal")));
            allCounts = Integer.valueOf(String.valueOf(resultCountsMap.get("all")));
            log.info("从redis获取的taskId为{}的总检测点数是==={}, 异常点数是==={}, 正常点数是==={}", taskId, allCounts, abnormalCounts, normalCounts);

            if (StringUtils.equals("246", String.valueOf(cruiseResultMap.get("cruiseResult")))){
                normalCounts++;
            }else {
                abnormalCounts++;
            }
            log.info("normalCounts:{}, abnormalCounts:{}", normalCounts, abnormalCounts);
            resultCountsMap.put("abnormal", String.valueOf(abnormalCounts));
            resultCountsMap.put("normal", String.valueOf(normalCounts));
            redisTemplate.opsForHash().putAll(strForCountAbnormal, resultCountsMap);
        }

        // 判断任务是否结束
        if (normalCounts + abnormalCounts != allCounts){
            return;
        }

        // 插库

        // 给上级系统上报任务状态

        return;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer selectIsAlarmByTask(String taskId, String instanceId){
        return uPatrolTaskDao.selectIsAlarmByTask(taskId, Long.valueOf(instanceId));
    }
    @Transactional(rollbackFor = Exception.class)
    public Integer updatePicPath(String taskId, String instanceId, String imagePath){
        return uPatrolTaskDao.updatePicPath(taskId, Long.valueOf(instanceId), imagePath);
    }

    /**
     * 根据巡视点id查询该测点信息
     * @param instanceId
     * @return TStdDeviceMete
     */
    public TStdDeviceMete selectDeviceMeteInfo(Long instanceId){
        return uPatrolTaskDao.selectDeviceMeteInfo(instanceId);
    }

    /**
     * 字典值查询
     * @param dictNote 说明
     * @param colName 类型
     * @return String
     */
    public String selectDictCodeByNote(String dictNote, String colName){
        return uPatrolTaskDao.selectDictCodeByNote(dictNote,colName);
    }

    /**
     * 根据机器人实物id查询机器人信息
     * @param robotCode 机器人实物id
     * @return TRobotInfo 机器人信息
     */
    public TRobotInfo selectRobotInfoByCode(String robotCode){
        return uPatrolTaskDao.selectRobotInfoByCode(robotCode);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<Map<String, Object>> taskCount(Date taskStartDate, int flag) {
        // 时间格式化处理
        Map<String, String> map = new HashMap<>();
        if (flag == 1){
            map = monthHandle(taskStartDate);
        }else {
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
            if (tCruiseTaskCount.getIfRun() == 172 && tCruiseTaskCount.getStartTime().compareTo(originTime) == 0) {
                List<Date> timeList = DateTimeUtil.cornTransTime(tCruiseTaskCount.getDateType(), dayBefore, dayAfter);
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

                taskCountMap.put("startTime", secondSdf.format(tCruiseTaskCount.getStartTime()));
                listTask.add(taskCountMap);
            }
        }
        return listTask;
    }

    private Map<String, String> monthHandle(Date taskStartDate) {
        Map<String, String> map = new HashMap<>();
        String firstDay = "";
        String lastDay = "";
        if (Objects.equals(null, taskStartDate)) {
            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, date.getMonth() - 1);
            if ((date.getMonth()) == 1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            } else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = daySdf.format(calendar.getTime()) + " 23:59:59";
            log.info("firstDay: " + firstDay);
            map.put("firstDay", firstDay);

            int lDay = 0;
            calendar.set(Calendar.MONTH, date.getMonth());
            //2月的平年瑞年天数
            if (date.getMonth() == 1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            } else {
                lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            calendar.set(Calendar.MONTH, date.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = daySdf.format(calendar.getTime()) + " 23:59:59";
            log.info("lastDay: " + lastDay);
            map.put("lastDay", lastDay);

        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, taskStartDate.getMonth() - 1);
            if ((taskStartDate.getMonth()) == 1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            } else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = daySdf.format(calendar.getTime()) + " 23:59:59";
            log.info("firstDay: " + firstDay);
            map.put("firstDay", firstDay);

            int lDay = 0;
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            //2月的平年瑞年天数
            if (taskStartDate.getMonth() == 1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            } else {
                lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            calendar.set(Calendar.YEAR, taskStartDate.getYear() + 1900);
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = daySdf.format(calendar.getTime()) + " 23:59:59";
            log.info("lastDay: " + lastDay);
            map.put("lastDay", lastDay);
        }
        return map;
    }

    private  Map<String, String> yearHandle(Date taskStartDate) {
        Map<String, String> map = new HashMap<>();
        String firstDay = "";
        String lastDay= " ";
        if (Objects.equals(null, taskStartDate)) {
            Date date = new Date();
            int year = date.getYear() + 1900;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            calendar.clear();
            calendar.set(Calendar.YEAR, year);
            Date currYearFirst = calendar.getTime();
            firstDay = daySdf.format(currYearFirst) + " 00:00:00";
            log.info("firstDay: " + firstDay);
            map.put("firstDay", firstDay);

            calendar.clear();
            calendar.set(Calendar.YEAR, year);
            calendar.roll(Calendar.DAY_OF_YEAR, -1);
            Date currYearLast = calendar.getTime();
            lastDay = daySdf.format(currYearLast) + " 23:59:59";
            log.info("lastDay: " + lastDay);
            map.put("lastDay", lastDay);

        } else {
            int year = taskStartDate.getYear() + 1900;
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(taskStartDate);

            calendar.clear();
            calendar.set(Calendar.YEAR, year);
            Date currYearFirst = calendar.getTime();
            firstDay = daySdf.format(currYearFirst) + " 00:00:00";
            log.info("firstDay: " + firstDay);
            map.put("firstDay", firstDay);

            calendar.clear();
            calendar.set(Calendar.YEAR, year);
            calendar.roll(Calendar.DAY_OF_YEAR, -1);
            Date currYearLast = calendar.getTime();
            lastDay = daySdf.format(currYearLast) + " 23:59:59";
            log.info("lastDay: " + lastDay);
            map.put("lastDay", lastDay);
        }
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
//                        System.out.println("------------:"+item.get("taskState"));
//                        System.out.println("-------------"+taskState.equals(item.get("taskState").toString()));
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
        String firstDay = "", lastDay = "";
        if (Objects.equals(null, taskStartDate)) {
//            Date date = new Date();
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(Calendar.MONTH, date.getMonth());
//            int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
//            calendar.set(Calendar.DAY_OF_MONTH, fDay);
//            firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

            Date date = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, date.getMonth() - 1);
            if ((date.getMonth()) == 1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            } else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("firstDay: " + firstDay);

            int lDay = 0;
            calendar.set(Calendar.MONTH, date.getMonth());
            //2月的平年瑞年天数
            if (date.getMonth() == 1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            } else {
                lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            calendar.set(Calendar.MONTH, date.getMonth());
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("lastDay: " + lastDay);
        } else {
//            Calendar calendar = Calendar.getInstance();
//            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
//            calendar.set(Calendar.YEAR, taskStartDate.getYear()+1900);
//            int fDay = calendar.getActualMinimum(Calendar.DAY_OF_MONTH);
//            calendar.set(Calendar.DAY_OF_MONTH, fDay);
//            firstDay = sdfF.format(calendar.getTime())+" 00:00:00";

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, taskStartDate.getMonth() - 1);
            if ((taskStartDate.getMonth()) == 1) {
                int fDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            } else {
                int fDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
                calendar.set(Calendar.DAY_OF_MONTH, fDay);
            }
            firstDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("firstDay: " + firstDay);

            int lDay = 0;
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            //2月的平年瑞年天数
            if (taskStartDate.getMonth() == 1) {
                lDay = calendar.getLeastMaximum(Calendar.DAY_OF_MONTH);
            } else {
                lDay = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            calendar.set(Calendar.MONTH, taskStartDate.getMonth());
            calendar.set(Calendar.YEAR, taskStartDate.getYear() + 1900);
            calendar.set(Calendar.DAY_OF_MONTH, lDay);
            lastDay = sdfF.format(calendar.getTime()) + " 23:59:59";
            log.info("lastDay: " + lastDay);
        }

        try {
            originTime = format.parse("2000-01-01 00:00:00");
            dayBefore = format.parse(firstDay);
            dayAfter = format.parse(lastDay);
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
            if (tCruiseTaskCount.getIfRun() == 172 && tCruiseTaskCount.getStartTime().compareTo(originTime) == 0) {
                List<Date> timeList = DateTimeUtil.cornTransTime(tCruiseTaskCount.getDateType(), dayBefore, dayAfter);
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
        List<Analysis> analysisList=new ArrayList<>();
        String taskId= RandomStringUtils.randomAlphanumeric(12);
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

        log.info("List:"+analysisList);
        //任务插库

        UPatrolTask tCruiseTask=new UPatrolTask();
        tCruiseTask.setTaskId(taskId);
        tCruiseTask.setTaskName("图像判别-"+taskId);
        tCruiseTask.setStartTime(new Date());
        int status=uPatrolTaskDao.add(tCruiseTask);

        // TODO: 2021/2/6 联调时放开 任务下发请求
//        //任务下发请求
//        Map<String,List<Analysis>> listMap=new HashMap<>();
//        listMap.put("list",analysisList);
//        defect(listMap);
        //返回自定义结果
        return status;
    }

}
