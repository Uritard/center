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
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.Object2Map;
import com.yjh.platform.common.utils.smUtil.Demo;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.TCruisePointInstanceNameDetail;
import com.yjh.platform.module.patrol.dao.UPatrolPlanAttrDao;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskAttrDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskDao;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.patrol.thread.InspectionResultThread;
import com.yjh.platform.module.patrol.thread.IsWarnAfterCruiseThread;
import com.yjh.platform.module.task.dao.*;
import com.yjh.platform.module.task.dao.TCruiseTaskDelDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.threadpool.TaskExecutePool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

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


    /**
     * websocket路径
     */
    @Value("${other.webSocketUrl}")
    private String websocketUrl;
    //模板图片路径
    private String picModelPath;
    //等待相机转到预置位时间
    private Long waitTime;
    ;
    //jobName
    @Value("${spring.QingHua.jobName}")
    private String jobName;
    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";
    private static final String ROBOT_TASK_URL = "http://iot-center-accessrobot/robot/v1/taskIssued";

    private static final byte[] LOCK_FLAG = new byte[0];


    DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
        uPatrolTask.setTaskCode(uPatrolTask.getTaskId());
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
            InspectionResultThread cruiseResultDealThread = new InspectionResultThread(robotPatrolTaskResult, infoMap, redisTemplate, websocketUrl, true);
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
            IsWarnAfterCruiseThread isWarnAfterCruiseThread = new IsWarnAfterCruiseThread(isAlarmMap, redisTemplate, websocketUrl, stationCode);
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
        String filePathTemp = new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/" + taskId + "/";

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

            String temporaryFilePath = ftpsFilePath + "/" + robotPatrolTaskResult.getFilePath();
            log.info("temporaryFilePath==={}", temporaryFilePath);
            switch (fileType){
                case "1":
                    copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "FIR");
                    infoMap.put("relativePath", developRelativeUrl + "FIR" + "/" + fileName);
                    infoMap.put("absolutePath", developAbsoluteUrl + "FIR" + "/" + fileName);

                    isAlarmMap.put("relativePath", developRelativeUrl + "FIR" + "/" + fileName);
                    isAlarmMap.put("absolutePath", developAbsoluteUrl + "FIR" + "/" + fileName);
                    break;
                case "2":
                    copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "Infrared");
                    infoMap.put("relativePath", developRelativeUrl + "CCD" + "/" + fileName);
                    infoMap.put("absolutePath", developAbsoluteUrl + "CCD" + "/" + fileName);

                    isAlarmMap.put("relativePath", developRelativeUrl + "CCD" + "/" + fileName);
                    isAlarmMap.put("absolutePath", developAbsoluteUrl + "CCD" + "/" + fileName);
                    break;
                case "3":
                    copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "Audio");
                    infoMap.put("relativePath", developRelativeUrl + "Audio" + "/" + fileName);
                    infoMap.put("absolutePath", developAbsoluteUrl + "Audio" + "/" + fileName);
                    break;
                case "4":
                    copyFileToDevelop(temporaryFilePath, developAbsoluteUrl + "Video");
                    infoMap.put("relativePath", developRelativeUrl + "Video" + "/" + fileName);
                    infoMap.put("absolutePath", developAbsoluteUrl + "Video" + "/" + fileName);
                    break;
                default:
                    break;
            }
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return isAlarmMap;
    }

            String fileType = robotPatrolTaskResult.getFileType();
            String originFilePath = robotPatrolTaskResult.getOriginFilePath();
            if (StringUtils.isNotEmpty(originFilePath) && StringUtils.equals("1", fileType)) {
                // 红外原图
                String[] originNameArray = originFilePath.split("/");
                String infraredOriginName = originNameArray[originNameArray.length - 1];
                String temporaryInfraredOriginPath = ftpsFilePath + "/" + originFilePath;
            }
    /**
     * 将ftp服务器上的文件复制到开发环境
     */
    public static void copyFileToDevelop(String source,String aim){
        File ff=new File(aim);
        if (!ff.exists()){
            ff.setWritable(true, false);
            ff.mkdirs();
        }
        try {
            String url = "cp " + source + " "+aim;
            Runtime.getRuntime().exec(url);
        }catch (Exception e){
            e.printStackTrace();
        }
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
        //return 1;
        //todo 密码的解密
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
            log.error(e.getMessage(), e);
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

        //todo  任务怎么继续？

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
            tasKeys.stream().filter(String::isEmpty).forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
            return null;
        });

        taskInfoList.forEach(m -> {
            // String cameraId =
        });

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

}
