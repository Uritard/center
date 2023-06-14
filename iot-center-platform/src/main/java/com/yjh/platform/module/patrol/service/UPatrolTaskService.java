/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.LogsAspect;
import com.yjh.platform.common.logs.LogsRecord;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.quartz.JobManager;
import com.yjh.platform.common.quartz.QuartzTask;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.*;
import com.yjh.platform.common.utils.smUtil.Demo;
import com.yjh.platform.configuration.ApplicationProperties;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.RobotTaskMessage;
import com.yjh.platform.module.device.entity.TCruisePointInstanceNameDetail;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.RobotProxy;
import com.yjh.platform.module.patrol.dao.*;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.patrol.thread.CruiseRetryThread;
import com.yjh.platform.module.patrol.thread.LocalCruiseExecutThread;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDelDao;
import com.yjh.platform.module.task.dao.TPeriodModelDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.TRobotInfo;
import com.yjh.platform.scheduled.ScheduledMapConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.quartz.CronExpression;
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
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    public static final String ROBOT_OR_DRONE_TASK = "robotOrDroneTask:";
    public static final String TASK_PRIORITY_REDIS_KEY = "task_priority_config:";
    public static final String TASK_LOWER_REDIS_KEY = "lowPatrolTask:";
    public static final String TASK_RETRY_SUFFIX = "_遗漏点位重试任务";
    public static final String TASK_RETRY_PREFIX = "taskRetry:";
    public static final Map<String, Object> MAP_LOCK = new ConcurrentHashMap<>();

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
    private TCruisePlanDao tCruisePlanDao;
    @Autowired
    private ProcessResultToUpSystem processResultToUpSystem;
    @Autowired
    private TPeriodModelDao tPeriodModelDao;
    @Autowired
    private UPatrolDataResultService uPatrolDataResultService;

    @Autowired
    private UPatrolDataResultDao uPatrolDataResultDao;
    @Autowired
    private TRobotInfoDao tRobotInfoDao;
    @Autowired
    private JobManager jobManager;
    @Autowired
    private ApplicationProperties applicationProperties;

    @Autowired
    private RobotProxy robotProxy;

    @Autowired
    private LogsRecord logsRecord;
    //jobName
    @Value("${spring.QingHua.jobName}")
    private String jobName;

    private static final String ROBOT_TASK_URL = "http://iot-center-accessrobot/robot/v1/taskIssued";

    private static final byte[] LOCK_FLAG = new byte[0];

    private final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * 一键启动所有任务 压测用
     * @param createUserId
     * @return
     */
    public String startAllTask(Long createUserId) {
        List<TCruiseTaskAdd> tCruiseTaskAdds = tCruisePlanDao.selectAllTask(createUserId);
        tCruiseTaskAdds.forEach(tCruiseTaskAdd -> {
            this.addTask(tCruiseTaskAdd, true);
        });
        return "all task started";
    }
    /**
     * 任务下发，在外层处理设置定时器逻辑，不走事物，否则会导致定时器延时
     * @param tCruiseTaskAdd 任务组装参数
     * @param issueFlag 是否往下级节点发送
     * @return 任务执行ID
     */
    public Map<String,Object> addTask(TCruiseTaskAdd tCruiseTaskAdd, Boolean issueFlag){
        // insert 需要走事物，使用 AopContext.currentProxy 获取当前代理，走事物处理
        UPatrolTaskService proxy = SpringBeanUtils.getBean(UPatrolTaskService.class);
        assert proxy != null;
        UPatrolTask uPatrolTask = proxy.insert(tCruiseTaskAdd, issueFlag);

        // 设置定时器，不走事物逻辑，否则会延时
        setQuartzTask(uPatrolTask);

        String stationCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
        Map<String,Object> taskMap = new HashMap<>();
        String taskPatrolledId = stationCode + "_" + uPatrolTask.getTaskCode() + "_" + DateTimeUtil.format3(uPatrolTask.getStartTime());
        taskMap.put("taskId", uPatrolTask.getTaskId());
        taskMap.put("taskPatrolledId", taskPatrolledId);
        return taskMap;
    }

    @Transactional(rollbackFor = Exception.class)
    public UPatrolTask insert(TCruiseTaskAdd tCruiseTaskAdd, Boolean issueFlag) {
        UPatrolTask uPatrolTask = dealTaskInfo(tCruiseTaskAdd);

        // 设置任务优先级
        setLevel(uPatrolTask, tCruiseTaskAdd);
        try {
            uPatrolTask.setCreateTime(new Date());
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

        List<TCruisePointInstanceNameDetail> detailList = initializeNextTaskInfo(uPatrolTask, instanceList);
        //任务启动只创建任务不往下发
        if (issueFlag) {
            // 找出下级设备或下级节点的点让其做任务
            String res = taskToEdgeOrDevice(uPatrolTask, tCruiseTaskAdd, format, detailList);
            if (StringUtils.isNotEmpty(res)) {
                throw new BusinessException(ResultCodeEnum.CODE10001.getCode(), res);
            }
        }

        return uPatrolTask;
    }

    private UPatrolTask dealTaskInfo(TCruiseTaskAdd tCruiseTaskAdd){
        UPatrolTask uPatrolTask = new UPatrolTask();
        if (tCruiseTaskAdd.getIfRun() == 172) {
            String cronExpressionDate = "";
            Long periodId = tCruiseTaskAdd.getPeriodId();
            if (Objects.nonNull(periodId)) {
                TPeriodModel tPeriodModel = tPeriodModelDao.selectByPrimaryId(periodId);
                cronExpressionDate = tPeriodModel.getCronExpression();
            } else {
                String cycleMonth = tCruiseTaskAdd.getCycleMonth();
                String cycleWeek = tCruiseTaskAdd.getCycleWeek();
                String cycleExecuteTime = tCruiseTaskAdd.getCycleExecuteTime();

                String intervalNumber = tCruiseTaskAdd.getIntervalNumber();
                String intervalExecuteTime = tCruiseTaskAdd.getIntervalExecuteTime();
                String intervalType = tCruiseTaskAdd.getIntervalType();

                cycleMonth = StringUtils.equals("1,2,3,4,5,6,7,8,9,10,11,12", cycleMonth) ? "*" : cycleMonth;
                cycleWeek = StringUtils.equals("2,3,4,5,6,7,1", cycleWeek) ? "*" : cycleWeek;
                // 周期
                if (StringUtils.isNotEmpty(cycleMonth) && StringUtils.isNotEmpty(cycleWeek) && StringUtils.isNotEmpty(cycleExecuteTime)) {
                    cronExpressionDate = String.format("0 %s %s ? %s %s", 0, cycleExecuteTime, cycleMonth, cycleWeek);
                }
                // 间隔
                if (StringUtils.isNotEmpty(intervalType) && StringUtils.isNotEmpty(intervalNumber) && StringUtils.isNotEmpty(intervalExecuteTime)) {
                    String hour = intervalExecuteTime.startsWith("0") ? intervalExecuteTime.substring(1, 2) : intervalExecuteTime.substring(0,2);
                    String min = intervalExecuteTime.substring(3, 5).startsWith("0") ? intervalExecuteTime.substring(4, 5) : intervalExecuteTime.substring(3, 5);
                    String second = intervalExecuteTime.substring(6, 8).startsWith("0") ? intervalExecuteTime.substring(7, 8) : intervalExecuteTime.substring(6, 8);

                    // 天: 秒 分 时 */日 * ?
                    if (StringUtils.equals("2", intervalType)) {
                        cronExpressionDate = String.format("%s %s %s */%s * ?", second, min, hour, intervalNumber);
                    }
                    // 时: 秒 分 */时 * * ？
                    else {
                        cronExpressionDate = String.format("%s %s %s/%s * * ?", second, min, 0,intervalNumber);
                    }
                }
                log.info("cronExpressionDate==================: {}", cronExpressionDate);
            }
            if (CronExpression.isValidExpression(cronExpressionDate)) {
                tCruiseTaskAdd.setDateType(cronExpressionDate);
                uPatrolTask.setDateType(cronExpressionDate);
            } else {
                throw new BusinessException(ResultCodeEnum.CODE10005.getName());
            }
        } else {
            if (Objects.nonNull(tCruiseTaskAdd.getStartTime()) && !Objects.equals("", tCruiseTaskAdd.getStartTime())) {
                uPatrolTask.setStartTime(tCruiseTaskAdd.getStartTime());
            } else {
                uPatrolTask.setStartTime(new Date());
            }
            uPatrolTask.setEndTime(new Date());
        }
        if (Objects.nonNull(tCruiseTaskAdd.getTaskCode())) {
            uPatrolTask.setTaskCode(tCruiseTaskAdd.getTaskCode());
            if (Objects.nonNull(tCruiseTaskAdd.getTaskId())) {
                uPatrolTask.setTaskId(tCruiseTaskAdd.getTaskId());
            }else {
                uPatrolTask.setTaskId(tCruiseTaskAdd.getTaskCode());
            }
        }
        if (tCruiseTaskAdd.getAreaId() == null){
            String areaId =  redisTemplate.opsForHash().entries("t_sys_param:edgeCode").get("content").toString();
            tCruiseTaskAdd.setAreaId(areaId);
        }
        uPatrolTask.setTaskName(tCruiseTaskAdd.getTaskName())
                .setPlanId(tCruiseTaskAdd.getPlanId())
                .setTaskCode(tCruiseTaskAdd.getTaskCode())
                .setAreaId(tCruiseTaskAdd.getAreaId())
                .setTaskType(tCruiseTaskAdd.getType())
                .setExecuteType(tCruiseTaskAdd.getIfRun())
                .setCreateUserId(tCruiseTaskAdd.getCreateUserId())
                .setRobotId(tCruiseTaskAdd.getRobotId());

        return uPatrolTask;
    }

    private void setLevel(UPatrolTask uPatrolTask, TCruiseTaskAdd tCruiseTaskAdd) {
        String level1 = (String)redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY + "901", "level");
        String level2 = (String)redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY + "902", "level");
        String level3 = (String)redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY + "903", "level");
        String level4 = (String)redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY + "904", "level");
        Integer ifRun = uPatrolTask.getExecuteType();
        if (tCruiseTaskAdd.getTaskLevel() == null || tCruiseTaskAdd.getTaskLevel() == 0) {
            if(Constant.isUpSystem()) {
                // 上级系统
                uPatrolTask.setTaskLevel(NumberUtils.toInt(level2, 2));
                return;
            }
            if (Objects.equals("0", tCruiseTaskAdd.getUnionTaskStatus()) || tCruiseTaskAdd.getUnionTaskStatus() == null) {
                if (Objects.equals(TaskTypeEnum.NOW.getType(), ifRun)) {
                    uPatrolTask.setTaskLevel(NumberUtils.toInt(level3, 3));
                } else {
                    uPatrolTask.setTaskLevel(NumberUtils.toInt(level1, 1));
                }
            } else {
                // 联动任务
                uPatrolTask.setTaskLevel(NumberUtils.toInt(level4, 4));
            }
        } else {
            uPatrolTask.setTaskLevel(tCruiseTaskAdd.getTaskLevel());
        }
    }

    private List<Long> insertTaskAttr(UPatrolTask uPatrolTask, TCruiseTaskAdd tCruiseTaskAdd) {
        List<Long> instanceList = new ArrayList<>();
        List<UPatrolTaskAttr> uPatrolTaskAttrs = new ArrayList<>();
        if (Objects.nonNull(tCruiseTaskAdd.getPlanId())) {
            List<UPatrolPlanAttr> uPatrolPlanAttrList = uPatrolPlanAttrDao.selectByPlanId(tCruiseTaskAdd.getPlanId());
            for (UPatrolPlanAttr uPatrolPlanAttr : uPatrolPlanAttrList) {
                UPatrolTaskAttr uPatrolTaskAttr = new UPatrolTaskAttr();
                uPatrolTaskAttr.setTaskId(uPatrolTask.getTaskId());
                uPatrolTaskAttr.setInstanceId(uPatrolPlanAttr.getInstanceId());
                uPatrolTaskAttr.setDeviceMeteId(uPatrolPlanAttr.getDeviceMeteId());
                uPatrolTaskAttr.setDeviceId(uPatrolPlanAttr.getDeviceId());
                uPatrolTaskAttr.setCustomId(uPatrolPlanAttr.getCustomId());
                uPatrolTaskAttr.setPointTaskId(uPatrolPlanAttr.getPointTaskId());
                uPatrolTaskAttr.setPointType(uPatrolPlanAttr.getPointType());
                uPatrolTaskAttr.setDeviceType(uPatrolPlanAttr.getDeviceType());
                uPatrolTaskAttr.setMeteType(uPatrolPlanAttr.getMeteType());
                uPatrolTaskAttr.setRegionId(uPatrolPlanAttr.getUpRegionId());

                instanceList.add(uPatrolPlanAttr.getInstanceId());
                uPatrolTaskAttrs.add(uPatrolTaskAttr);
                if (uPatrolTaskAttrs.size() % 2000 == 0) {
                    this.uPatrolTaskAttrDao.batchAdd(uPatrolTaskAttrs);
                    uPatrolTaskAttrs = new ArrayList<>();
                }
            }
            TCruisePlanCount plan = tCruisePlanDao.selectByPrimaryId(tCruiseTaskAdd.getPlanId());
            tCruiseTaskAdd.setType(plan.getType());
            uPatrolTask.setTaskType(tCruiseTaskAdd.getType());
        } else {
            //处理报文中taskType字段为系统字典表中的字段
            Integer taskType;
            switch (tCruiseTaskAdd.getType()) {
                case 1:
                    //例行巡视
                    taskType = 214;
                    break;
                case 2:
                    //特殊巡视
                    taskType = 216;
                    break;
                case 3:
                    //专项巡视
                    taskType = 217;
                    break;
                default:
                    //自定义巡视
                    taskType = 218;
            }
            uPatrolTask.setTaskType(taskType);
            String[] deviceInstancesFromUpperSystem = tCruiseTaskAdd.getDeviceList().split(",");
            for (String item : deviceInstancesFromUpperSystem) {
                instanceList.add(Long.valueOf(item));
            }
            List<TCruisePointInstance> tCruisePointInstanceList = uPatrolTaskAttrDao.batchSelect(instanceList);
            log.info("tCruisePointInstanceList {}", tCruisePointInstanceList);
            for (TCruisePointInstance tCruisePointInstance : tCruisePointInstanceList) {
                UPatrolTaskAttr uPatrolTaskAttr = new UPatrolTaskAttr();
                uPatrolTaskAttr.setTaskId(uPatrolTask.getTaskId());
                uPatrolTaskAttr.setInstanceId(tCruisePointInstance.getInstanceId());
                uPatrolTaskAttr.setDeviceMeteId(tCruisePointInstance.getDeviceMeteId());
                uPatrolTaskAttr.setDeviceId(tCruisePointInstance.getDeviceId());
                uPatrolTaskAttr.setCustomId(tCruisePointInstance.getCustomId());
                uPatrolTaskAttr.setPointTaskId(uPatrolTaskAttr.getPointTaskId());
                uPatrolTaskAttrs.add(uPatrolTaskAttr);
                if (uPatrolTaskAttrs.size() % 2000 == 0) {
                    this.uPatrolTaskAttrDao.batchAdd(uPatrolTaskAttrs);
                    uPatrolTaskAttrs = new ArrayList<>();
                }
            }
        }

        if (uPatrolTaskAttrs.size() > 0) {
            uPatrolTaskAttrDao.batchAdd(uPatrolTaskAttrs);
        }
        uPatrolTask.setCreateTime(new Date());
        uPatrolTaskDao.add(uPatrolTask);
        log.info("instanceList {}", instanceList);
        if (!Constant.isHost()) {
            Constant.modelUpload("7");
        }
        return instanceList;
    }


    public List<TCruisePointInstanceNameDetail> initializeTaskInfo(List<Long> instanceList, UPatrolTask task)  {
        List<TCruisePointInstanceNameDetail> detailList = tCruisePointInstanceDao.selectForTask(instanceList);

        UPatrolResult uPatrolResult = new UPatrolResult();
        Date now = new Date();
        uPatrolResult.setTaskId(task.getTaskId())
                .setTaskName(task.getTaskName())
                .setTaskCode(task.getTaskCode())
                .setAreaId(task.getAreaId())
                .setTaskType(task.getTaskType())
                .setExecuteType(task.getExecuteType())
                .setTaskLevel(task.getTaskLevel())
                .setTaskState(238)
                .setCreateTime(task.getCreateTime())
                .setTaskCount(detailList.size())
                .setTaskWait(detailList.size())
                .setRemark("0");
        uPatrolResultDao.add(uPatrolResult);

        if (!Constant.fastTurbo()) {
            log.info("instancesList==={}", detailList);
        }
        Set<String> nodeSet = new HashSet<>(8);
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
                    .setCustomId(item.getCustomId())
                    .setCustomName(item.getCustomName())
                    .setDevicePointId(item.getDevicePointId())
                    .setIsWarn(0)
                    .setCruiseType(item.getCruiseType()).setCreatetime(now);
            Map<String, String> map = Object2Map.objectToMap(uPatrolDataResult, true);
            String edgeCode = Optional.ofNullable(item.getEdgeCode()).orElse("");
            map.put("edgeCode", edgeCode);
            map.put("devicePointId", item.getDevicePointId());
            map.put("deviceMeteId", String.valueOf(item.getDeviceMeteId()));
            map.put("taskName", task.getTaskName());
            map.put("startTime", DateTimeUtil.format3(task.getStartTime()));
            if(ArrayUtils.contains(new int[]{TypeEnum.UAV.getCode(), TypeEnum.ROBOT.getCode()}, item.getCruiseType())){
                map.put("cameraId","");
                map.put("robotId", String.valueOf(item.getRobotId()));
            }else {
                map.put("cameraId", String.valueOf(item.getCameraId()));
                map.put("robotId","");
            }

            if (StringUtils.isEmpty(edgeCode)) {
                String robotId = map.get("robotId");
                nodeSet.add(StringUtils.isEmpty(robotId) ? "camera" : robotId);
            } else {
                nodeSet.add(edgeCode);
            }

            map.put("isTemdif", String.valueOf(item.getIsTemdif()));
            // 初始化识别类型和采集文件类型,默认值为位置状态识别和识别图片
            map.put("recognitionType", StringUtils.isNotEmpty(item.getMeteType()) ?
                    RecognitionTypeEnum.getProRecognize(item.getMeteType()).getProtocolRecognize() : "2");
            map.put("fileType", "5");
            map.put("unit", Optional.ofNullable(item.getUnit()).orElse(""));
            switch (item.getMeteType()){
                case "222":
                    map.put("fileType", "1");
                    break;
                case "223":
                    map.put("fileType", "3");
                    break;
                case "220":
                case "433":
                    map.put("fileType", "2");
                    break;
                case "690":
                case "691":
                case "692":
                    map.put("fileType", "");
                    break;
                default:
                    break;
            }
            String str = PATROL_TASK_PREFIX + task.getTaskId() + ":" + item.getInstanceId();
            redisTemplate.opsForHash().putAll(str, map);
        }
        initializeThisTaskInfo(task, detailList.size(), nodeSet);
        if (!Constant.isHost()) {
            sendTaskStateToUp(task, 5);
        }
        return detailList;
    }

    /**
     * 周期任务初始化下一次的任务信息
     */
    public List<TCruisePointInstanceNameDetail> initializeNextTaskInfo(UPatrolTask task, List<Long> instanceList) {
        UPatrolTask ctask = task;
        if (task.getExecuteType() == CruiseConstant.TaskTypeEnum.CYCLE.getType()) {
            UPatrolTask nextTask = new UPatrolTask();
            String newTaskId = String.valueOf(UUID.randomUUID()).replace("-", "");
            // 周期任务的下一次任务时间和名称在初始化时就通过cron表达式进行计算，不再通过任务执行时再修改名称
            Date nextStartTime = DateTimeUtil.cornNextTime(task.getDateType(), task.getStartTime());
            String taskNameTime = DateTimeUtil.format3(nextStartTime);
            nextTask.setTaskId(newTaskId)
                    .setTaskCode(task.getTaskCode())
                    .setTaskName(task.getTaskName() + "_" + taskNameTime)
                    .setPlanId(task.getPlanId())
                    .setAreaId(task.getAreaId())
                    .setTaskType(task.getTaskType())
                    .setExecuteType(task.getExecuteType())
                    .setRobotId(task.getRobotId())
                    // .setDateType(task.getDateType())
                    .setTaskSource(task.getTaskSource())
                    .setTaskLevel(task.getTaskLevel())
                    .setStartTime(nextStartTime)
                    // 避免下一次的任务创建时间与当前任务创建时间重复，下一次任务创建时间 +30s
                    .setCreateTime(new Date(System.currentTimeMillis() + 30000))
                    .setEndTime(task.getEndTime())
                    .setCreateUserId(task.getCreateUserId());
            // 周期任务初始化下一个，非周期初始化当前
            ctask = nextTask;
            uPatrolTaskDao.add(nextTask);
        }
        return initializeTaskInfo(instanceList, ctask);
    }

    public void initializeThisTaskInfo(UPatrolTask task, int allSize, Set<String> nodeSet) {

        Map<String, String> mapForAbnormal = new HashMap<>();
        mapForAbnormal.put("all", String.valueOf(allSize));
        mapForAbnormal.put("abnormal", "0");
        mapForAbnormal.put("normal", "0");
        mapForAbnormal.put("taskStart", DateTimeUtil.format(task.getStartTime()));
        mapForAbnormal.put("taskState", String.valueOf(CruiseConstant.TASK_STATE_NOT_START));
        mapForAbnormal.put("nodes", JSON.toJSONString(nodeSet));

        String strForCountAbnormal = PATROL_SUMMARY_PREFIX + task.getTaskId();
        redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
    }
    @Transactional(rollbackFor = Exception.class)
    public Integer selectRobotType(String robotCode) {
        return tRobotInspectionDao.selectRobotType(robotCode);
    }
    public String selectRobotCodeByInstanceId(Long instanceId) {
        return tRobotInspectionDao.selectRobotCodeByInstanceId(instanceId);
    }

    public Integer getCruiseDeviceInfo(String robotCode) {
        return tRobotInspectionDao.selectRegion(robotCode);
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
            String[] patrolledIds = robotPatrolTaskStatus.getTaskPatrolledId().split("_");
            String taskCode = robotPatrolTaskStatus.getTaskCode();
            // 增加时间判断，避免预先初始化导致数据传入下一个任务
            String timeStr = patrolledIds.length > 2 ? patrolledIds[2] : patrolledIds[1];
            String patrolledId = patrolledIds.length > 2 ? patrolledIds[1] : patrolledIds[0];
            Date date = DateTimeUtil.parseFormat(timeStr, DateTimeUtil.getDateTimePattern3());
            String taskId = null;

            boolean robotEnd = false;
            int taskState = TASK_STATE_NOT_START;
            switch (robotPatrolTaskStatus.getTaskState()) {
                case "3":
                    taskState = TASK_STATE_PAUSE;
                    break;
                case "2":
                    taskState = TASK_STATE_EXECUTING;
                    break;
                case "1":
                    taskState = TASK_STATE_FINISHED;
                    robotEnd = true;
                    break;
                case "4":
                    taskState = TASK_STATE_INTERRUPT;
                    robotEnd = true;
                    break;
                case "6":
                    taskState = TASK_STATE_TIMEOUT;
                    robotEnd = true;
                    break;
                case "5":
                    // 任务状态为 5 未执行时，需要考虑这个任务是下级系统创建的还是上级系统创建的，如果是上级系统创建的，就不应该初始化，如果是下级系统创建的，则需要初始化
                    taskId = addToUpSystem(robotPatrolTaskStatus, patrolledId);
                    break;
                default:
                    break;
            }
            String robotCode = robotPatrolTaskStatus.getRobotCode();
            if (StringUtils.isEmpty(taskId)) {
                taskId = tRobotInspectionDao.selectRealTaskId(taskCode, date);
            }
            if (StringUtils.isEmpty(taskId)) {
                taskId = patrolledId;
            }
            //判断是不是机器人或者无人机
            Long robotId = tRobotInfoDao.selectRobotIdByCode(robotCode);

            // 如果不是上报的任务结束，则走更新任务状态逻辑
            updateTaskProgress(robotPatrolTaskStatus, taskId, taskState, robotId);

            // 如果下级上报任务结束，则走任务结束处理逻辑，避免提前更改任务状态
            if (robotEnd) {
                String taskIdFinal = taskId;
                int fanalTaskState = taskState;
                ScheduledMapConfig.schedule(15, Constant.endWaitTimes(), t-> dealRobotTaskShutDown(taskIdFinal, robotCode, robotId, fanalTaskState, t));
            }

            if (robotId != null){
                //放入redis
                redisTemplate.opsForHash().putAll(ROBOT_OR_DRONE_TASK+robotPatrolTaskStatus.getTaskCode()+":"+robotId,Object2Map.objectToMap(robotPatrolTaskStatus));
                redisTemplate.opsForHash().putAll("RobotTaskStatus:" + robotCode + ":" + taskCode, Object2Map.objectToMap(robotPatrolTaskStatus));
            }
        });
    }


    private String addToUpSystem(RobotPatrolTaskStatus robotPatrolTaskStatus, String taskId) {
        try {
            // long robotId = tRobotInspectionDao.selectRobotIdByRobotCode(robotPatrolTaskStatus.getRobotCode());
            Date createTime = new Date(System.currentTimeMillis() + 30000);
            Date startTime = format.parse(robotPatrolTaskStatus.getPlanStartTime());
            UPatrolTask uPatrolTask = new UPatrolTask()
                    .setTaskId(taskId)
                    .setTaskCode(robotPatrolTaskStatus.getTaskCode())
                    .setTaskName(robotPatrolTaskStatus.getTaskName())
                    .setAreaId(robotPatrolTaskStatus.getRobotCode())
                    // .setRobotId(robotId)
                    .setStartTime(startTime)
                    .setTaskSource(1)
                    .setTaskType(218)
                    .setExecuteType(173)
                    .setTaskLevel(1)
                    .setCreateTime(createTime);
            UPatrolResult uPatrolResult = new UPatrolResult()
                    .setTaskId(taskId)
                    .setTaskState(Integer.valueOf(robotPatrolTaskStatus.getTaskState()))
                    .setTaskCode(robotPatrolTaskStatus.getTaskCode())
                    .setTaskName(robotPatrolTaskStatus.getTaskName())
                    .setAreaId(robotPatrolTaskStatus.getRobotCode())
                    // .setRobotId(robotId)
                    .setTaskType(218)
                    .setExecuteType(173)
                    // taskSource 表示下级创建主动上报任务
                    .setTaskSource(1)
                    .setTaskLevel(1)
                    .setTaskState(TASK_STATE_NOT_START)
                    .setCreateTime(createTime)
                    .setExecuteTime(startTime);

            UPatrolTask taskExist = uPatrolTaskDao.selectThisTaskByTaskCode(robotPatrolTaskStatus.getTaskCode());
            if (taskExist == null || (
                    !StringUtils.equals(taskId, taskExist.getTaskId()) &&
                            Optional.ofNullable(taskExist.getTaskSource()).orElse(0) == 1)) {
                uPatrolTaskDao.add(uPatrolTask);
            } else {
                log.warn("Task already exist, not insert, task: {}", JSON.toJSONString(taskExist));
                return null;
            }

            UPatrolResult resultExsis = uPatrolResultDao.selectByPrimaryId(taskId);
            if (resultExsis == null) {
                uPatrolResultDao.add(uPatrolResult);
            } else {
                log.warn("Task result already exist: {}", JSON.toJSONString(resultExsis));
            }
            return taskId;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 更新任务进度，下级系统主动上报任务，非本级创建任务
     */
    private void updateTaskProgress(RobotPatrolTaskStatus robotPatrolTaskStatus, String taskId, int taskState, Long robotId){
        String key = PATROL_SUMMARY_PREFIX + taskId;
        Map<String, String> countMap = redisTemplate.opsForHash().entries(key);
        Map<String, String> countChangeMap = new HashMap<>(16);
        if (MapUtils.isEmpty(countMap)) {
            countChangeMap.put("taskStart", robotPatrolTaskStatus.getStartTime());
            countChangeMap.put("abnormal", "0");
            countChangeMap.put("normal", "0");
            countChangeMap.put("all", "0");
            // taskSource==1 下级创建任务主动上报
            countChangeMap.put("taskSource", "1");
            countChangeMap.put("taskPatrolledId", robotPatrolTaskStatus.getTaskPatrolledId());
            countMap.putAll(countChangeMap);
        }
        /*if (!"1".equals(map.get("taskSource"))) {
            log.info("Task create by self, don`t continue, taskId: {}", taskId);
            redisTemplate.opsForHash().put(key, "taskPatrolledId", robotPatrolTaskStatus.getTaskPatrolledId());
            return;
        }*/
        int oldState = MapUtils.getIntValue(countMap, "taskState", TASK_STATE_NOT_START);
        if (taskIsEnded(oldState)) {
            log.warn("任务已经结束，不可更改状态， taskId: {}, taskState: {}, newState: {}", taskId, oldState, taskState);
            return;
        }

        // 是否下级主动创建任务
        boolean subCreateTask = "1".equals(countMap.get("taskSource"));
        try {
            countChangeMap.put("taskPatrolledId", robotPatrolTaskStatus.getTaskPatrolledId());
            countChangeMap.put("lastCruiseTime", DateTimeUtil.getDateTimeString());
            if (subCreateTask) {
                // 下级主动创建任务第一次启动更新任务状态，更新任务进度
                if (TASK_STATE_NOT_START == MapUtils.getIntValue(countMap, "taskState", TASK_STATE_NOT_START) && TASK_STATE_EXECUTING == taskState) {
                    UPatrolResult result = new UPatrolResult().setTaskId(taskId).setTaskState(CruiseConstant.TASK_STATE_EXECUTING)
                        .setExecuteTime(DateTimeUtil.parse(robotPatrolTaskStatus.getStartTime(), new Date()));
                    log.info("TaskResult start, taskId: {}", taskId);
                    uPatrolResultDao.update(result);
                }

                String progress = robotPatrolTaskStatus.getTaskProgress();
                if (StringUtils.contains(progress, "%")) {
                    float pf = NumberUtils.toFloat(StringUtils.remove(progress, "%")) / 100F;
                    progress = CommonUtils.percentFormat(pf, "#.####");
                }
                if (NumberUtils.isCreatable(progress)) {
                    countChangeMap.put("taskProgress", progress);
                }
            }

            String nodesStr = countMap.get("nodes");
            List<String> nodes = Collections.emptyList();
            if (StringUtils.isNotEmpty(nodesStr)) {
                nodes = JSON.parseArray(nodesStr, String.class);
            }

            // 判断子节点状态，若所有子节点状态相同，则更新节点状态
            boolean stateNodeAll = true;
            String realCode = robotId == null ? robotPatrolTaskStatus.getRobotCode() : robotId.toString();
            for (String node : nodes) {
                if (!StringUtils.equals(node, realCode)) {
                    String tmpKey = PATROL_SUMMARY_PREFIX + "sub_state:" + taskId + ":" + node;
                    int nodeState = NumberUtils.toInt((String)redisTemplate.opsForValue().get(tmpKey));
                    if (nodeState != taskState) {
                        stateNodeAll = false;
                        break;
                    }
                }
            }

            boolean stateChange = nodes.size() <= 1;
            // 若节点是子节点创建，或仅包含一个节点，或所有节点状态相等，则更新当前节点状态
            if (robotId == null && (subCreateTask || stateChange || stateNodeAll)) {
                //任务暂停继续处理
                if (ArrayUtils.contains(new int[]{TASK_STATE_PAUSE, TASK_STATE_EXECUTING}, taskState)) {
                    UPatrolResult result = new UPatrolResult().setTaskId(taskId).setTaskState(taskState);
                    uPatrolResultDao.update(result);
                }
                countChangeMap.put("taskState", String.valueOf(taskState));
            }
            String subKey = PATROL_SUMMARY_PREFIX + "sub_state:" + taskId + ":" + realCode;
            redisTemplate.opsForValue().set(subKey, taskState);
            redisTemplate.expire(subKey, 3, TimeUnit.DAYS);

            log.info("update down task status: {}，taskState: {}, subCreateTask: {}, nodes: {}", JSON.toJSONString(countChangeMap), taskState, subCreateTask, JSON.toJSONString(nodes));

            redisTemplate.opsForHash().putAll(key, countChangeMap);
            redisTemplate.expire(key, 3, TimeUnit.DAYS);

            Map<String, String> jasonMap = new HashMap<>();
            jasonMap.put("type", "newTask");
            jasonMap.put("taskId", taskId);
            log.info("发送给前端的消息：   {}", JSON.toJSONString(jasonMap));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);
        } catch (Exception e) {
            log.error("更新任务状态失败， taskId: {}, state:{}, taskStatus: {}", taskId, taskState, JSON.toJSONString(robotPatrolTaskStatus));
        }
    }

    /**
     * 处理机器人上报任务结束
     *
     * @param taskId taskId
     * @param robotCode robotCode
     * @param robotId robotId
     */
    public boolean dealRobotTaskShutDown(String taskId, String robotCode, Long robotId, int taskState, int times) {
        log.info("处理机器人上报任务结束,参数：taskId:{}, robotCode:{}, robotId:{},", taskId, robotCode, robotId);

        String key = PATROL_SUMMARY_PREFIX + taskId;
        String taskSource = (String)redisTemplate.opsForHash().get(key, "taskSource");
        if ("1".equals(taskSource)) {
            forceCompletionTask(taskId);
            return true;
        }

        String cruiseResultKey = PATROL_TASK_PREFIX + taskId + ":";
        //处理结果 获取机器人的点
        Set<String> keys = redisScan(cruiseResultKey);
        List<Map<String, String>> resultMap = redisTemplate.executePipelined((RedisCallback<Map<String, String>>)connection -> {
            keys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
            return null;
        });

        int ret = 1;
        for (Map<String, String> result : resultMap) {
            int check = dealOneRobotTask(result, taskId, robotCode, robotId, cruiseResultKey, taskState, times);
            // check 0:正常 1:未执行 2:调用算法超时 -1:算法调用未结束 10:非当前节点数据
            // 如果check=10，表示有非当前节点数据，则机器人上报结束就不用重复调用
            if (check == -1 && ret != 0) {
                ret = -1;
            } else if (check == 10) {
                ret = 0;
            }
        }

        // ret != -1 表示等待结束，不需要再次等待，定时任务结束
        if (ret != -1) {
            checkCompletionTask(taskId);
        }

        return ret != -1;
    }

    /**
     * 判断任务如果是状态 完成、终止、超期、异常终止，那么强制结束任务
     */
    public void checkCompletionTask(String taskId) {
        String taskState = (String)redisTemplate.opsForHash().get(PATROL_SUMMARY_PREFIX + taskId, "taskState");
        if (!taskIsEnded(NumberUtils.toInt(taskState))) {
            forceCompletionTask(taskId);
        }
    }


    /**
     * 判断任务等待时间是否超时（调用算法），任务是否异常
     *
     * @param result result
     * @param robotCode robotCode
     * @param robotId robotId
     * @param times 当前处理已经执行次数
     * @return result 0:正常,已完成 1:未执行 2:调用算法超时 -1:算法调用未结束 10:非当前节点数据
     */
    private int checkProcessTime(Map<String, String> result, String robotCode, Long robotId, int times) {
        int cruiseState = MapUtils.getIntValue(result,"cruiseStatus");
        boolean inNode; // 判断当前节点是否在任务结束节点范围内，包括机器人/无人机/下级节点
        if (robotId == null) {
            // 是下级节点
            inNode = StringUtils.equals(robotCode, result.get("edgeCode"));
        } else {
            // 无人机或机器人节点
            inNode = robotId == MapUtils.getLongValue(result, "robotId");
        }

        if (!inNode) {
            return 10;
        }

        if ((CRUISE_STATE_UN == cruiseState || CRUISE_STATE_ANALYSE_DOING == cruiseState) && times < Constant.endWaitTimes()) {
            // 如果点位状态未执行，或算法分析中，多等待几轮
            return -1;
        }

        if (CRUISE_STATE_UN == cruiseState) {
            // 未执行
            return 1;
        }

        if (CRUISE_STATE_ANALYSE_DOING == cruiseState) {
            // 算法分析中
            return 2;
        }

        return 0;
    }

    /**
     * 处理单个需要结束的任务，吴国时未结束状态需要任务置为异常
     *
     * @param result result
     * @param taskId taskId
     * @param robotCode robotCode
     * @param robotId robotId
     * @param cruiseResultKey cruiseResultKey
     */
    private int dealOneRobotTask(Map<String, String> result, String taskId, String robotCode, Long robotId, String cruiseResultKey, int taskState, int times) {
        //判断是不是机器人的点以及还是否完成
        int cruiseType = MapUtils.getIntValue(result, "cruiseType");
        String instanceId = result.get("instanceId");
        int check = checkProcessTime(result, robotCode, robotId, times);
        if (1 == check) {
            //这个点 没有做
            CruiseConstant.TypeEnum cruiseTypeEnum = TypeEnum.getEnum(cruiseType);
            String rname = cruiseTypeEnum.getDesc();

            result.put("cruiseStatus", String.valueOf(CRUISE_STATE_OMIT));//执行遗漏
            result.put("resultNum", "-1");
            String desc = taskState == CruiseConstant.TASK_STATE_TIMEOUT ? AbnormalResDescEnum.TASK_TIMEOUT.getDesc(): rname + "任务异常";
            result.put("resultDesc", desc);
            result.put("cruiseAbnormal", String.valueOf(taskState == CruiseConstant.TASK_STATE_TIMEOUT ? CruiseConstant.CRUISE_ABNORMAL_TIMEOUT : CruiseConstant.CRUISE_ABNORMAL_INTERRUPT));//任务终止
            result.put("evaluationState", String.valueOf(CruiseConstant.EVALUATION_STATE_UN));//未审核
            result.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));//异常
            result.put("cruiseTime",DateTimeUtil.format(new Date()));

            String instanceKey = cruiseResultKey + instanceId;
            redisTemplate.opsForHash().putAll(instanceKey, result);

            patrolTaskResultHandler(taskId, Long.valueOf(instanceId));
        } else if (2 == check) {
            result.put("cruiseStatus", String.valueOf(CRUISE_STATE_DONE));//执行遗漏
            result.put("resultNum", "-1");
            result.put("resultDesc", AbnormalResDescEnum.ANALYSE_TIMEOUT.getDesc());
            result.put("cruiseAbnormal", String.valueOf(CruiseConstant.CRUISE_ABNORMAL_TIMEOUT));//超时
            result.put("evaluationState", String.valueOf(CruiseConstant.EVALUATION_STATE_UN));//未审核
            result.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));//异常
            result.put("cruiseTime",DateTimeUtil.format(new Date()));

            String instanceKey = cruiseResultKey + instanceId;
            redisTemplate.opsForHash().putAll(instanceKey, result);

            patrolTaskResultHandler(taskId, Long.valueOf(instanceId));
        }

        return check;
    }

    /**
     * 找出下级设备或节点的点让其做任务
     *
     * @param task           任务信息
     * @param tCruiseTaskAdd 任务关联信息
     * @param format         时间格式
     * @param detailList     区域巡视主机上的巡视点信息
     * @return String
     */
    private String taskToEdgeOrDevice(UPatrolTask task, TCruiseTaskAdd tCruiseTaskAdd, DateFormat format,
                                      List<TCruisePointInstanceNameDetail> detailList) {
        try {
            List<TCruisePointInstanceNameDetail> edgeDetailList = detailList.stream()
                    .filter(t -> StringUtils.isNotEmpty(t.getEdgeCode()) && StringUtils.isNotEmpty(t.getOriginId()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(edgeDetailList)) {
                Map<String, LinkedHashSet<String>> listMap = Maps.newHashMap();
                edgeDetailList.forEach(t -> {
                    LinkedHashSet<String> list = listMap.computeIfAbsent(t.getEdgeCode(), v -> new LinkedHashSet<>(512));
                    list.add(Constant.standardPoints() ? StringUtils.defaultIfEmpty(t.getDevicePointId(), t.getOriginId()) : t.getOriginId());
                });

                String[] cycleExecuteTimeArray = tCruiseTaskAdd.getCycleExecuteTime().split(",");
                if (cycleExecuteTimeArray.length > 1) {
                    log.info("这种格式的周期任务走上层任务调度");
                } else {
                    List<RobotTaskInstanceInfo> edgeTaskInfoList = new ArrayList<>();
                    listMap.forEach((edgeCode, instanceList) -> {
                        RobotTaskInstanceInfo taskInfo = new RobotTaskInstanceInfo();
                        taskInfo.setCruiseType(task.getTaskType());
                        taskInfo.setTaskId(task.getTaskId());
                        // 从巡视主机下发至边缘节点的任务等级为3级
                        taskInfo.setPriority(String.valueOf(task.getTaskLevel()));
                        taskInfo.setTaskName(task.getTaskName());
                        taskInfo.setInstanceList(new ArrayList<>(instanceList));
                        String ifFun = String.valueOf(tCruiseTaskAdd.getIfRun());
                        taskInfo.setIfRun(ifFun);
                        taskInfo.setUnionTaskStatus(tCruiseTaskAdd.getUnionTaskStatus());
                        taskInfo.setEdgeCode(edgeCode);
                        packageTaskProtocolInfo(tCruiseTaskAdd, format, taskInfo, ifFun);
                        edgeTaskInfoList.add(taskInfo);
                    });

                    Map<String, List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>(4);
                    robotTaskInfoMap.put("robotTaskInfoList", edgeTaskInfoList);
                    log.info("edgeTaskInfoMap = {}", robotTaskInfoMap);

                    // 调用robot服务下发任务
                    Result result = robotTask(robotTaskInfoMap);
                    detailList.removeAll(edgeDetailList);
                }
            }

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
            String[] cycleExecuteTimeArray = tCruiseTaskAdd.getCycleExecuteTime().split(",");
            if (cycleExecuteTimeArray.length > 1){
                log.info("这种格式的周期任务走上层任务调度");
            }else {
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
                    robotTaskInfo.setPriority(String.valueOf(task.getTaskLevel()));
                    robotTaskInfo.setTaskName(task.getTaskName());
                    List<String> robotTaskInstanceList = tRobotInspectionDao.selectRobotTaskInstanceId(robotInstanceList, item);
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

                Map<String, List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>(4);
                robotTaskInfoMap.put("robotTaskInfoList", robotTaskInfoList);
                log.info("robotTaskInfoMap = {}", robotTaskInfoMap);

                // 调用robot服务下发任务
                Result result = robotTask(robotTaskInfoMap);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return "";
    }

    public void taskToRobotOrDroneStart(UPatrolTask task, String dateType, List<Long> allInstanceList){
        List<TCruisePointInstanceNameDetail> detailList = tCruisePointInstanceDao.selectForTask(allInstanceList);
        log.info("instancesList==={}", detailList);
        //找出下级节点做任务的巡视点
        List<TCruisePointInstanceNameDetail> edgeDetailList = detailList.stream()
                .filter(t -> StringUtils.isNotEmpty(t.getEdgeCode()) && StringUtils.isNotEmpty(t.getOriginId()))
                .collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(edgeDetailList)) {
            Map<String, LinkedHashSet<String>> listMap = Maps.newHashMap();
            edgeDetailList.forEach(t -> {
                LinkedHashSet<String> list = listMap.computeIfAbsent(t.getEdgeCode(), v -> new LinkedHashSet<>(512));
                list.add(Constant.standardPoints() ? StringUtils.defaultIfEmpty(t.getDevicePointId(), t.getOriginId()) : t.getOriginId());
            });

            if (StringUtils.isNotEmpty(dateType)) {
                boolean moreTime = dateType.split(" ")[2].contains(",");
                if (moreTime && task.getExecuteType() == 172) {
                    List<RobotTaskInstanceInfo> edgeTaskInfoList = new ArrayList<>();
                    listMap.forEach((edgeCode, instanceList) -> {
                        RobotTaskInstanceInfo taskInfo = new RobotTaskInstanceInfo();
                        taskInfo.setCruiseType(task.getTaskType());
                        taskInfo.setTaskId(task.getTaskId());
                        taskInfo.setPriority(String.valueOf(task.getTaskLevel()));
                        taskInfo.setTaskName(task.getTaskName());
                        taskInfo.setInstanceList(new ArrayList<>(instanceList));
                        taskInfo.setIfRun("173");
                        taskInfo.setEdgeCode(edgeCode);
                        taskInfo.setFixedStartTime(format.format(new Date()));
                        edgeTaskInfoList.add(taskInfo);
                    });

                    Map<String, List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>(4);
                    robotTaskInfoMap.put("robotTaskInfoList", edgeTaskInfoList);
                    log.info("edgeTaskInfoMap = {}", robotTaskInfoMap);

                    robotTask(robotTaskInfoMap);
                    log.info("===============The task was successfully sent to the edge===============");
                }
            }
        }

        //找出机器人和无人机做任务的巡检点
        List<Long> robotCruiseList = uPatrolTaskDao.selectRobotOrDroneInsByTaskId(task.getTaskCode());
        log.info("robotCruiseList : {}", robotCruiseList);
        List<Long> robotInstanceList = uPatrolTaskDao.selectInstanceIdByTaskId(task.getTaskCode());
        log.info("robotInstanceList : {}", robotInstanceList);

        if (robotCruiseList.isEmpty()) {
            log.info("There are no instanceId for robot or drone to do！！！");
            return;
        }
        List<String> robotCode = tRobotInspectionDao.selectForRobotTask(robotCruiseList);
        log.info("robotCode : {}", robotCode);
        List<RobotTaskInstanceInfo> robotTaskInfoList = new ArrayList<>();
        for (String item: robotCode){
            RobotTaskInstanceInfo robotTaskInfo = new RobotTaskInstanceInfo();
            robotTaskInfo.setCruiseType(task.getTaskType());
            robotTaskInfo.setTaskId(task.getTaskId());
            robotTaskInfo.setPriority(String.valueOf(task.getTaskLevel()));
            robotTaskInfo.setTaskName(task.getTaskName());
            List<String> robotTaskInstanceList = tRobotInspectionDao.selectRobotTaskInstanceId(robotInstanceList, item);
            robotTaskInfo.setInstanceList(robotTaskInstanceList);
            robotTaskInfo.setIfRun("173");
            robotTaskInfo.setRobotCode(item);
            robotTaskInfo.setFixedStartTime(format.format(new Date()));
            robotTaskInfoList.add(robotTaskInfo);
        }
        Map<String,List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>(4);
        robotTaskInfoMap.put("robotTaskInfoList",robotTaskInfoList);
        log.info("robotTaskInfoMap = {}", robotTaskInfoMap);
        //让机器人和无人机做任务
        log.info("task=={}, dateType: {}", task, dateType);
        if (StringUtils.isNotEmpty(dateType)) {
            boolean moreTime = dateType.split(" ")[2].contains(",");
            if (moreTime && task.getExecuteType() == 172) {
                robotTask(robotTaskInfoMap);
                log.info("===============The task was successfully sent to the robot===============");
            }
        }
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
            CruiseConstant.TaskTypeEnum taskType = CruiseConstant.TaskTypeEnum.getEnm(NumberUtils.toInt(ifFun));

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
                    taskInfo.setCycleMonth(Optional.ofNullable(tCruiseTaskAdd.getCycleMonth()).orElse(""));
                    String cycleWeek = tCruiseTaskAdd.getCycleWeek();
                    if (StringUtils.isNotEmpty(cycleWeek)){
                        String[] array = cycleWeek.split(",");
                        StringJoiner cycleWeeks = new StringJoiner(",");
                        for (int i = 0; i < array.length; i++) {
                            int cycleWeekTemp = Integer.parseInt(array[i]);
                            cycleWeek = String.valueOf(cycleWeekTemp == 1 ? 7 : cycleWeekTemp - 1);
                            cycleWeeks.add(cycleWeek);
                        }
                        taskInfo.setCycleWeek(String.valueOf(cycleWeeks));
                    }else {
                        taskInfo.setCycleWeek("");
                    }

                    String cycleExecuteTime = tCruiseTaskAdd.getCycleExecuteTime();

                    if (StringUtils.isNotEmpty(cycleExecuteTime)){
                        if (Integer.parseInt(tCruiseTaskAdd.getCycleExecuteTime()) < 10) {
                            cycleExecuteTime = "0" + tCruiseTaskAdd.getCycleExecuteTime() + ":00:00";
                        } else {
                            cycleExecuteTime = tCruiseTaskAdd.getCycleExecuteTime() + ":00:00";
                        }
                    }
                    taskInfo.setCycleExecuteTime(cycleExecuteTime);
                    taskInfo.setCycleStartTime(Optional.ofNullable(tCruiseTaskAdd.getCycleStartTime()).orElse(""));
                    taskInfo.setCycleEndTime(Optional.ofNullable(tCruiseTaskAdd.getCycleEndTime()).orElse(""));

                    taskInfo.setIntervalType(Optional.ofNullable(tCruiseTaskAdd.getIntervalType()).orElse(""));
                    taskInfo.setIntervalNumber(Optional.ofNullable(tCruiseTaskAdd.getIntervalNumber()).orElse(""));
                    taskInfo.setIntervalExecuteTime(Optional.ofNullable(tCruiseTaskAdd.getIntervalExecuteTime()).orElse(""));

                    boolean isInterval = StringUtils.isEmpty(tCruiseTaskAdd.getIntervalType());
                    taskInfo.setCycleStartTime(isInterval ? tCruiseTaskAdd.getCycleStartTime() : "");
                    taskInfo.setCycleEndTime(isInterval ? tCruiseTaskAdd.getCycleEndTime() : "");
                    taskInfo.setIntervalStartTime(isInterval ? "" : tCruiseTaskAdd.getIntervalStartTime());
                    taskInfo.setIntervalEndTime(isInterval ? "" : tCruiseTaskAdd.getIntervalEndTime());
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
                Result result = serviceRestTemplate.postForObject(ROBOT_TASK_URL, robotTaskInfoMap, Result.class);
                // 处理普宙无人机
                sendDroneTaskStart(robotTaskInfoMap);
                return result;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
     * 针对无人机任务目前还没有定时任务触发功能，需要额外发送启动命令执行任务启动
     *
     * @param robotTaskInfoMap robotTaskInfoMap
     */
    private void sendDroneTaskStart(Map<String, List<RobotTaskInstanceInfo>> robotTaskInfoMap) {
        List<RobotTaskInstanceInfo> taskList = robotTaskInfoMap.get("robotTaskInfoList");
        if (CollectionUtils.isEmpty(taskList)) {
            return;
        }

        taskList.forEach(task -> {
            if (task != null && StringUtils.isNoneBlank(task.getTaskId())) {
                if (task.getIfRun().equals(TaskTypeEnum.NOW.getType())) {
                    // 只有立即执行任务才需要额外发送启动命令
                    taskStart(task.getTaskId(), null, true);
                }
            }
        });
    }

    public void setQuartzTask(UPatrolTask task) {
        //开启定时任务
        QuartzTask quartzTask = new QuartzTask();
        quartzTask.setJobName(task.getTaskId());
        quartzTask.setJobGroup(jobName);
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

    private void sendTaskStateToUp(String taskId, Integer state) {
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(task, state);
    }

    /**
     * 任务状态上报上一级系统
     *
     * @param task 任务信息
     * @param state 状态
     */
    private void sendTaskStateToUp(UPatrolTask task, Integer state) {
        XMLBaseModel xmlBaseModel = new XMLBaseModel();
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        xmlBaseModel.setType("41");
        try {
            String stationCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
            String taskPatrolledIdTemp = task.getTaskCode();
/*
            UPatrolTask uPatrolTask = selectTaskByTaskCode(task.getTaskCode());
            if (StringUtils.isNotEmpty(uPatrolTask.getDateType())){
                taskPatrolledIdTemp = task.getTaskCode();
            }*/

            item.put("task_patrolled_id", stationCode + "_" +taskPatrolledIdTemp + "_" + DateTimeUtil.format3(task.getStartTime()));
            item.put("task_name", task.getTaskName());
            item.put("task_code", task.getTaskCode());
            item.put("task_state", String.valueOf(state));
            String planTime = DateTimeUtil.format(task.getStartTime());
            item.put("plan_start_time", planTime);

            Map<String, String> mapForGet = redisTemplate.opsForHash().entries(PATROL_SUMMARY_PREFIX + task.getTaskId());

            item.put("start_time", mapForGet.getOrDefault("taskStart", planTime));

            int all = NumberUtils.toInt(mapForGet.get("all"));
            all = Math.max(all, 1);
            Integer normal = NumberUtils.toInt(mapForGet.get("normal"));
            Integer abnormal = NumberUtils.toInt(mapForGet.get("abnormal"));
            int i = all - normal - abnormal;
            i = Math.max(i, 0);
            String progress = String.format("%.2f", 100F * (normal + abnormal) / all);
            item.put("task_progress", progress + "%");

            item.put("task_estimated_time", i * 60 * 5);
            item.put("description", "");
            items.add(item);
            xmlBaseModel.setItems(items);

            List<XMLBaseModel> list = new ArrayList<>();
            list.add(xmlBaseModel);
            Map<String, List<XMLBaseModel>> map = new HashMap<>();
            map.put("list", list);
            log.info("信息上报：-" + map);
            Constant.otherServer(map, Constant.TCP_URL);//江苏要求
        } catch (Exception e) {
            log.info("任务状态上报上一级系统出错：", e);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskConfirmation(String userId, TCruiseTaskAdd tCruiseTaskAdd, HttpServletRequest request) throws Exception {
        String iP = request.getHeader("HTTP_X_FORWARDED_FOR");
        SysUser sysUser = sysUserDao.selectByPrimaryId(Long.valueOf(userId));
        String password = tCruiseTaskAdd.getpCode();
        String identifier = tCruiseTaskAdd.getIdentifier();
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
            params.set("title", "任务" + tCruiseTaskAdd.getTaskName() + "下发");
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
    public int deleteByPrimaryId(String taskId, String startTime,String source,HttpServletRequest request) {
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);
        Long planId = task.getPlanId();
        if (Optional.ofNullable(request).isPresent() && Objects.nonNull(request.getHeader("userId"))) {
            logsRecord.LogsSend(request, "4", "删除任务", "删除任务-" + task.getTaskName());
        }
        if (Objects.nonNull(task.getExecuteType()) && task.getExecuteType() == TaskTypeEnum.CYCLE.getType()) {
            if (!"-1".equals(startTime)) {
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
                JobManager.removeJob(taskId, jobName, taskId + "_trg", jobName);

                log.info("taskMap del..." + Constant.taskMap);
                log.info("del task totally...");
                tCruiseTaskDelDao.deleteByPrimaryId(taskId);
                //删除初始化的一条
                uPatrolTaskDao.deleteInitByPrimaryId(taskId);
                int result = uPatrolTaskDao.deleteByPrimaryId(taskId);
                deleteTransfer(source, planId, taskId, startTime);
                return result;
            }
        } else if (Objects.nonNull(task.getExecuteType()) && task.getExecuteType() == TaskTypeEnum.TIME.getType()) {
            JobManager.removeJob(taskId, jobName, taskId + "_trg", jobName);
        }
        tCruiseTaskDelDao.deleteByPrimaryId(taskId);
        //删除初始化的一条
        uPatrolTaskDao.deleteInitByPrimaryId(taskId);
        deleteTransfer(source, planId, taskId, startTime);
        int result = uPatrolTaskDao.deleteByPrimaryId(taskId);
        deleteTransfer(source, planId, taskId, startTime);
        return result;
    }

    /**
     * 任务删除同步
     * @param source
     * @param planId
     * @param taskId
     * @param startTime
     */
    public void deleteTransfer(String source, Long planId, String taskId, String startTime){
        String edgeLevel = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeLevel", "content"));
        //边缘节点无需上报或下发
        if (!"1".equals(edgeLevel)) {
            //巡视系统需要下发，来源为上级系统的删除需要在上级系统下发
            if ("2".equals(edgeLevel) ||("3".equals(source) && "3".equals(edgeLevel))) {
                List<UPatrolPlanAttr> uPatrolPlanAttrList = uPatrolPlanAttrDao.selectByPlanId(planId);
                List<Long> instanceIdList = uPatrolPlanAttrList.stream().map(UPatrolPlanAttr::getInstanceId).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(instanceIdList)) {
                    List<TCruisePointInstanceNameDetail> tCruisePointInstanceNameDetails =
                            tCruisePointInstanceDao.selectForTask(instanceIdList);
                    Set<String> edgeCode = tCruisePointInstanceNameDetails.stream().map(TCruisePointInstanceNameDetail::getEdgeCode)
                            .filter(StringUtils::isNotBlank).collect(Collectors.toSet());
                    if (CollectionUtils.isNotEmpty(edgeCode)) {
                        //删除下级系统任务信息
                        robotProxy.deleteTransfer(new ArrayList<>(edgeCode), taskId, startTime,source);
                    }
                }
            }
            //只有来源为巡视系统的删除命令需要上报上级系统
            if ("2".equals(edgeLevel) && "2".equals(source)) {
                XMLBaseModel xmlBaseModel = new XMLBaseModel();
                List<Map<String, Object>> xmlItems = new ArrayList<>();
                Map<String, Object> xmlItem = new HashMap<>(16);

                xmlBaseModel.setType("41");
                xmlBaseModel.setCommand("102");
                xmlItem.put("taskId", taskId);
                xmlItem.put("startTime", startTime);
                xmlItems.add(xmlItem);
                xmlBaseModel.setItems(xmlItems);
                List<XMLBaseModel> list = new ArrayList<>();
                list.add(xmlBaseModel);
                Map<String, List<XMLBaseModel>> deleteMap = new HashMap<>(3);
                deleteMap.put("list", list);
                log.info("删除任务上报: {}", JSON.toJSONString(deleteMap));
                try {
                    Constant.otherServer(deleteMap, Constant.TCP_URL);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public int taskPauseWithoutRobot(String taskId) {
        //任务暂停 不用给机器人发
        UPatrolResult taskResult = uPatrolResultDao.selectByPrimaryId(taskId);
        if (taskIsEnded(taskResult.getTaskState())) {
            log.info("当前任务已经结束，不可暂停:{}, state: {}", taskId, taskResult.getTaskState());
            return 1;
        }
        taskResult.setTaskState(TASK_STATE_PAUSE);

        // 更新Redis任务状态
        updateTaskStateForRedis(taskId, String.valueOf(TASK_STATE_PAUSE));
        // 更新数据库任务状态
        int ret = uPatrolResultDao.update(taskResult);

        // 任务状态向下级和上级同步
        taskPauseStateAnsy(taskId);

        return ret;
    }

    public void taskPauseStateAnsy(String taskId) {
        // 消息不发给前端，newTask 会刷新消息界面
/*
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
            log.error("任务暂停异常: ", e);
        }
*/

        Map<String, Object> robotTaskStatesMap = new HashMap<>(8);
        robotTaskStatesMap.put("taskId", taskId);
        robotTaskStatesMap.put("commandValue", 2);
        // 给下级系统任务暂停，不对机器人任务暂停
        List<String> edgeCodeList = uPatrolTaskDao.selectEdgeIsRunning(taskId);
        log.info("===Edge task pause,edgeCodeList:{}", edgeCodeList);
        if (CollectionUtils.isNotEmpty(edgeCodeList)) {
            robotTaskStatesMap.put("robotCodeList", edgeCodeList);
            robotTaskStates(robotTaskStatesMap);
        }

        //任务状态上报站端
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(task, 3);
    }

//    @Transactional(rollbackFor = Exception.class)
    public String taskStart(String taskId, HttpServletRequest request, boolean isDrone) {
        String taskPatrolledId = "";
        try {
            String newTaskId = String.valueOf(UUID.randomUUID()).replace("-", "");
            UPatrolTask uPatrolTask = uPatrolTaskDao.selectByPrimaryId(taskId);
            if (Optional.ofNullable(request).isPresent()) {
                logsRecord.LogsSend(request, "29", "任务启动", "任务启动-" + uPatrolTask.getTaskName());
            }
            TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
            if (Objects.nonNull(uPatrolTask.getPlanId())){
                tCruiseTaskAdd.setPlanId(uPatrolTask.getPlanId());
            }else {
                List<String> deviceList = uPatrolTaskAttrDao.selectDeviceList(taskId);
                tCruiseTaskAdd.setDeviceList(StringUtils.join(deviceList, ","));
            }
            //创建立即任务
            tCruiseTaskAdd.setTaskId(newTaskId);
            tCruiseTaskAdd.setTaskCode(uPatrolTask.getTaskCode());
            tCruiseTaskAdd.setIfRun(173);
            tCruiseTaskAdd.setTaskName(uPatrolTask.getTaskName() + "任务启动" + DateTimeUtil.format3(new Date()));
            tCruiseTaskAdd.setType(uPatrolTask.getTaskType());
            tCruiseTaskAdd.setTaskLevel(uPatrolTask.getTaskLevel());
            tCruiseTaskAdd.setCreateUserId(uPatrolTask.getCreateUserId());
            tCruiseTaskAdd.setAreaId(uPatrolTask.getAreaId());
            Map<String, Object> taskMap = this.addTask(tCruiseTaskAdd, false);
            taskPatrolledId = String.valueOf(taskMap.get("taskPatrolledId"));
            List<String> robotCodeList = uPatrolTaskDao.selectRobotIsRunning(taskId);
            log.info("机器人任务启动,robotCodeList:{}", robotCodeList);
            robotCodeList = robotCodeList.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(robotCodeList)) {
                Map<String, Object> robotTaskStatesMap = new HashMap<>();
                robotTaskStatesMap.put("taskId", taskId);
                robotTaskStatesMap.put("commandValue", 1);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                if (isDrone) {
                    // 如果是无人机任务，需要额外发送启动命名
                    robotTaskStatesMap.put("isDrone", isDrone);
                }

                robotTaskStates(robotTaskStatesMap);
            }
        } catch (Exception e) {
            log.error("任务启动异常: ", e);
        }
        return taskPatrolledId;
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
    public int taskPause(String taskId, HttpServletRequest request) {
        UPatrolResult uPatrolResult = uPatrolTaskDao.selectForTaskId(taskId);
        if (Optional.ofNullable(request).isPresent()) {
            logsRecord.LogsSend(request, "11", "任务暂停", "任务暂停-" + uPatrolResult.getTaskName());
        }
        if (taskIsEnded(uPatrolResult.getTaskState())) {
            log.info("当前任务已经结束，不可暂停:{}, state: {}", taskId, uPatrolResult.getTaskState());
            return 1;
        }
        uPatrolResult.setTaskState(TASK_STATE_PAUSE);
        try {
            //TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);

            //Thread.sleep(10000);
            updateTaskStateForRedis(taskId, String.valueOf(TASK_STATE_PAUSE));

            // 机器人任务暂停，包括机器人和下级系统
            List<String> robotCodeList = uPatrolTaskDao.selectRobotIsRunning(taskId);
            robotCodeList = robotCodeList.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            log.info("===Robot task pause,robotCodeList:{}", robotCodeList);
            if (CollectionUtils.isNotEmpty(robotCodeList)) {
                Map<String, Object> robotTaskStatesMap = new HashMap<>(6);
                robotTaskStatesMap.put("taskId", taskId);
                robotTaskStatesMap.put("commandValue", 2);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                robotTaskStates(robotTaskStatesMap);
            }

            Map<String, String> jasonMapOnFinished = new HashMap<>();
            jasonMapOnFinished.put("type", "taskChange");
            jasonMapOnFinished.put("taskId", taskId);
            String jsonMessage = JSON.toJSONString(jasonMapOnFinished);
            log.info("发送给前端的消息：{}", jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMapOnFinished);
        } catch (Exception e) {
            log.error("任务暂停异常: ", e);
        }

        //任务状态上报站端
        UPatrolTask uPatrolTask = uPatrolTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(uPatrolTask, 3);

        return uPatrolResultDao.update(uPatrolResult);
    }

    public boolean taskIsEnded(int state) {
        return ArrayUtils.contains(new int[] {TASK_STATE_FINISHED, TASK_STATE_INTERRUPT, TASK_STATE_ABNORMAL, TASK_STATE_TIMEOUT}, state);
    }

    public void updateTaskStateForRedis(String taskId, String state) {
        String strForCountAbnormal = PATROL_SUMMARY_PREFIX + taskId;
        log.info("任务转态变更, strForCountAbnormal:{}, {}", strForCountAbnormal, state);
        redisTemplate.opsForHash().put(strForCountAbnormal, "taskState", state);
    }

    public String taskStatus(String taskId) {
        String strForCountAbnormal = PATROL_SUMMARY_PREFIX + taskId;
        HashOperations<String, String, String> hashOperations = redisTemplate.opsForHash();
        return hashOperations.get(strForCountAbnormal, "taskState");
    }

    @Transactional(rollbackFor = Exception.class)
    public int taskGoOn(String taskId, boolean force, HttpServletRequest request) {
        UPatrolResult uPatrolResult = uPatrolTaskDao.selectForTaskId(taskId);
        if (Optional.ofNullable(request).isPresent()) {
            logsRecord.LogsSend(request, "12", "任务恢复", "任务恢复-" + uPatrolResult.getTaskName());
        }
        if (taskIsEnded(uPatrolResult.getTaskState())) {
            log.info("当前任务已经结束:{}, state: {}", taskId, uPatrolResult.getTaskState());
            return 1;
        }
        log.info("当前低优先级任务，force： {}, uPatrolResult: {}", force, JSON.toJSONString(uPatrolResult));
        //暂停的任务也考虑进去
        List<String> highTaskList = uPatrolTaskDao.selectPlanRunningOrPauseTask(null, uPatrolResult.getTaskLevel());
        if (!force && CollectionUtils.isNotEmpty(highTaskList)) {
            log.info("存在高优先级任务，当前任务暂停，taskId: {}, taskLevel: {}, List：{}", taskId, uPatrolResult.getTaskLevel(), JSON.toJSONString(highTaskList));
            for (String htId : highTaskList) {
                String highKey = TASK_LOWER_REDIS_KEY + htId;
                redisTemplate.opsForSet().add(highKey, taskId);
                redisTemplate.expire(highKey, 3, TimeUnit.DAYS);
            }
            taskPauseStateAnsy(taskId);

            return -1;
        }

        try {
            //机器人任务继续
            List<String> robotCodeList = uPatrolTaskDao.selectRobotIsRunning(taskId);
            robotCodeList = robotCodeList.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
            log.info("机器人任务继续,robotCodeList:{}", robotCodeList);
            if (CollectionUtils.isNotEmpty(robotCodeList)) {
                Map<String, Object> robotTaskStatesMap = new HashMap<>();
                robotTaskStatesMap.put("taskId", taskId);
                robotTaskStatesMap.put("commandValue", 3);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                robotTaskStates(robotTaskStatesMap);
            }
            if (uPatrolResult.getTaskState() == TASK_STATE_EXECUTING) {
                return 1;
            }

            updateTaskStateForRedis(taskId, String.valueOf(TASK_STATE_EXECUTING));

            uPatrolResult.setTaskState(TASK_STATE_EXECUTING);
            uPatrolResultDao.update(uPatrolResult);

            localTaskStart(taskId);

            UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);

            Map<String, String> jasonMapOnFinished = new HashMap<>();
            jasonMapOnFinished.put("type", "taskChange");
            jasonMapOnFinished.put("taskId", taskId);
            String jsonMessage = JSON.toJSONString(jasonMapOnFinished);
            log.info("发送给前端的消息：{}" + jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMapOnFinished);
            //任务状态上报站端
            sendTaskStateToUp(task, 2);
        } catch (Exception e) {
            log.error("任务继续异常: ", e);
        }
        return 1;
    }

    /**
     * 任务终止，异步执行
     */
    @Async
    public void taskShutDown(String taskId, String content) {
        UPatrolResult uPatrolResult = uPatrolResultDao.selectByPrimaryId(taskId);
        if (taskIsEnded(uPatrolResult.getTaskState())) {
            log.warn("任务已经结束，不可重复终止！taskState: {}", uPatrolResult.getTaskState());
            return;
        }
        uPatrolResult.setTaskState(TASK_STATE_INTERRUPT);

        // 下级任务终止
        List<String> robotCodeList = uPatrolTaskDao.selectRobotIsRunning(taskId);
        robotCodeList = robotCodeList.stream().filter(StringUtils::isNotEmpty).collect(Collectors.toList());
        log.info("下级任务终止,robotCodeList:{}", robotCodeList);
        if (CollectionUtils.isNotEmpty(robotCodeList)) {
            Map<String, Object> robotTaskStatesMap = new HashMap<>();
            robotTaskStatesMap.put("taskId", taskId);
            robotTaskStatesMap.put("commandValue", 4);
            robotTaskStatesMap.put("robotCodeList", robotCodeList);
            robotTaskStates(robotTaskStatesMap);
        }

        updateTaskStateForRedis(taskId, String.valueOf(TASK_STATE_INTERRUPT));
        try {

            Set<String> tasKeys = redisTemplate.keys(PATROL_TASK_PREFIX + taskId + ":*");
            if (CollectionUtils.isEmpty(tasKeys)) {
                log.error("patrol_task_result:{}:* 未查到任务，任务未正确初始化", taskId);
                log.error("直接更新任务状态，不执行入库操作， {}", taskId);
                uPatrolResultDao.update(uPatrolResult);
                throw new BusinessException("任务未正确初始化");
            }

            log.info("tasKeys size: {}", tasKeys.size());

            // 暂停15秒等待未接收数据完成接收
            ScheduledMapConfig.schedule(15, taskId, tid -> {
                List<Map<String, String>> taskInfoList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>)connection -> {
                    tasKeys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
                    return null;
                });

                log.info("taskInfoList size: {}", taskInfoList.size());

                if (!taskInfoList.isEmpty()) {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    List<Map<String, String>> skipPointList = new ArrayList<>();
                    for (Map<String, String> taskInfo : taskInfoList) {
                        if (MapUtils.isNotEmpty(taskInfo)) {
                            //count = count+1;
                            if (CommonUtils.isEmptyOrNullstr(taskInfo.get("cruiseResult"))) {
                                //任务终止
                                taskInfo.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                                taskInfo.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_INTERRUPT));
                                taskInfo.put("cruiseStatus", String.valueOf(CRUISE_STATE_IGNORE));
                                taskInfo.put("cruiseTime", simpleDateFormat.format(new Date()));
                                taskInfo.put("resultNum", "-1");
                                taskInfo.put("resultDesc", StringUtils.isNotEmpty(content) ? content : AbnormalResDescEnum.TERMINATION_OF_TASK.getDesc());
                                skipPointList.add(taskInfo);
                            }
                        }
                    }
                    log.info("task [{}] shut down, skipPointList: {}", tid, skipPointList.size());
                    ThreadPoolUtil.PATROL_POOL.addThread(new LocalCruiseExecutThread<>(this, skipPointList, true, true, tid));
                }

                log.info("任务终止成功=={}", tid);

                //任务状态上报站端
                // sendTaskStateToUp(task, 4);
                uPatrolResultDao.update(uPatrolResult);

                lowTaskGoOn(tid);
            });
        } catch (Exception e) {
            log.info("任务终止失败", e);
        }

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
        //只取本层级的测点任务
        taskInfoList = taskInfoList.stream().filter(t -> StringUtils.isEmpty(t.get("edgeCode"))).collect(Collectors.toList());
        log.info("taskInfoList size: {}", taskInfoList.size());

        // 查询检修区域
        final String[] overhaulString = {""};
        List<String> overhaulList = tCruisePointInstanceDao.selectTimeIsIn(new Date());
        overhaulList.forEach(s -> overhaulString[0] = StringUtils.isEmpty(overhaulString[0]) ? s : StringUtils.join(overhaulString[0], ",", s));
        List<String> overhaul = new ArrayList<>();
        if (StringUtils.isNotEmpty(overhaulString[0])) {
            overhaul = Arrays.asList(overhaulString[0].split(","));
        }
        List<String> finalOverhaul = overhaul;
        Collections.sort(finalOverhaul);
        log.info("finalOverhaul: {}", finalOverhaul);
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
            if (CollectionUtils.isNotEmpty(finalOverhaul) && Collections.binarySearch(finalOverhaul, MapUtils.getString(m, "instanceId")) >= 0) {
                m.put("resultNum", "-1");
                m.put("resultDesc", AbnormalResDescEnum.EQUIPMENT_MAINTENANCE.getDesc());
                // 异常原因，设备检修
                m.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_OVERHAUL));
                // 巡检数据状态，忽略
                m.put("cruiseStatus", String.valueOf(CRUISE_STATE_IGNORE));
                skipFlag = true;
            }
            // 机器人离线判断
            if (!skipFlag && (TypeEnum.ROBOT.getCode() == cruiseType || TypeEnum.UAV.getCode() == cruiseType)) {
                long robotId = MapUtils.getLongValue(m, "robotId");
                if (robotOffline(robotOfflineMap, robotId)) {
                    m.put("resultNum", "-1");
                    m.put("resultDesc", AbnormalResDescEnum.ROBOT_OFFLINE.getDesc());
                    // 巡检数据状态，执行失败
                    m.put("cruiseStatus", String.valueOf(CRUISE_STATE_FAILED));
                    // 异常原因，设备离线
                    m.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_OFFLINE));
                    if (robotOfflineMap.get(robotId) == 4) {
                        m.put("resultDesc", AbnormalResDescEnum.ROBOT_OVERHAUL.getDesc());
                        m.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_OVERHAUL));
                        // 巡检数据状态，忽略
                        m.put("cruiseStatus", String.valueOf(CRUISE_STATE_IGNORE));
                    }
                    skipFlag = true;
                }
            }
            if (skipFlag) {
                // 巡视结果，异常
                m.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                // 未审核
                m.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
                m.put("picpath", "--");
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

        log.info("task [{}] ready, skipPointList: {}, cruiseGroupMap: {}", taskId, skipPointList.size(), cruiseGroupMap);
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
            } else {
                // 机器人信息不存在则置为离线
                robotOfflineMap.put(robotId, 1);
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

    /**
     * 通过  cruiseResult 判断正常还是异常
     */
    public void patrolTaskResultHandler(Map<String, String> cruiseResultMap) {
        patrolTaskResultHandler(Collections.singletonList(cruiseResultMap), MapUtils.getIntValue(cruiseResultMap, "cruiseResult", CRUISE_RESULT_NORMAL));
    }

    /**
     * 批量传入，则表示一定异常
     */
    public void patrolTaskResultHandler(List<Map<String, String>> cruiseResultList) {
        if (CollectionUtils.isEmpty(cruiseResultList)) {
            log.error("cruiseResultList is empty.");
            return;
        }
        log.info("taskResultHandler task: {}, cruiseResultList: {}", cruiseResultList.get(0).get("taskId"), cruiseResultList.size());
        patrolTaskResultHandler(cruiseResultList, CRUISE_RESULT_ABNORMAL);
    }
    private void patrolTaskResultHandler(List<Map<String, String>> cruiseResultList, int cruiseResult) {
        if(CollectionUtils.isEmpty(cruiseResultList)){
            log.error("cruiseResultList is empty.");
            return;
        }
        int size = cruiseResultList.size();
        String taskId = cruiseResultList.get(0).get("taskId");
        int abnormalCounts = patrolTaskResult(taskId, cruiseResult, size);

        try {
            // 压测模式减少非必要消息传输
            if (!Constant.fastTurbo()) {
                // webSocket通知前端调用巡视监控的接口
                Map<String, String> jasonMap = new HashMap<>(3);
                jasonMap.put("type", "finishedOneInstance");
                jasonMap.put("taskId", taskId);
                log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMap));
                Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);

                // 巡视结果上报上一级系统
                processResultToUpSystem.alarmAndResultToUpSystem(cruiseResultList, null, null);
                //任务状态上报站端
                String strForCountAbnormal = PATROL_SUMMARY_PREFIX + taskId;
                Map<String, String> resultCountsMap = redisTemplate.opsForHash().entries(strForCountAbnormal);
                int taskStatus = NumberUtils.toInt(resultCountsMap.get("taskState"), TASK_STATE_FINISHED);
                int state = 2;
                switch (taskStatus) {
                    case TASK_STATE_PAUSE:
                        state = 3;
                        break;
                    case TASK_STATE_INTERRUPT:
                    case TASK_STATE_ABNORMAL:
                        state = 4;
                        break;
                    case TASK_STATE_TIMEOUT:
                        state = 6;
                        break;
                    default:
                        break;
                }
                sendTaskStateToUp(taskId, state);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (abnormalCounts >= 0) {
            log.info("该点任务执行完成{}", taskId);
            completionOfTask(taskId);
        }
    }

    private int patrolTaskResult(String taskId, int cruiseResult, int size) {
        // 获取当前redis正常异常点位个数并更新
        int abnormalCounts;
        int normalCounts;
        int allCounts;
        boolean endOnece = false;
        synchronized (LOCK_FLAG) {
            String strForCountAbnormal = PATROL_SUMMARY_PREFIX + taskId;
            Map<String, String> resultCountsMap = redisTemplate.opsForHash().entries(strForCountAbnormal);
            abnormalCounts = NumberUtils.toInt(resultCountsMap.get("abnormal"));
            normalCounts = NumberUtils.toInt(resultCountsMap.get("normal"));
            allCounts = NumberUtils.toInt(resultCountsMap.get("all"));
            boolean ended = Boolean.parseBoolean(resultCountsMap.getOrDefault("ended", "false"));
            log.info("From redis---task:{}, all:{}, abnormalCounts:{}, normalCounts:{}", taskId, allCounts, abnormalCounts, normalCounts);

            if (CRUISE_RESULT_NORMAL == cruiseResult) {
                normalCounts += size;
            } else {
                abnormalCounts += size;
            }
            String progress;
            if(allCounts != 0){
                float progressF = (float)(normalCounts + abnormalCounts)/allCounts;
                progress = CommonUtils.percentFormat(Math.min(progressF, 1.0F), "#.####");
                // 如果 all==0，则表示这不是本机创建的任务，任务进度不由本级计算，不更新进度值
                resultCountsMap.put("taskProgress", progress);
            }
            log.info("task:{}, normalCounts:{}, abnormalCounts:{}", taskId, normalCounts, abnormalCounts);
            resultCountsMap.put("abnormal", String.valueOf(abnormalCounts));
            resultCountsMap.put("normal", String.valueOf(normalCounts));
            resultCountsMap.put("lastCruiseTime", DateTimeUtil.getDateTimeString());


            if (allCounts != 0 && normalCounts + abnormalCounts >= allCounts && !ended) {
                endOnece = true;
            }

            redisTemplate.opsForHash().putAll(strForCountAbnormal, resultCountsMap);
            redisTemplate.expire(strForCountAbnormal, 3, TimeUnit.DAYS);
        }

        // 判断任务是否结束
        if (endOnece) {
            return abnormalCounts;
        }
        return -1;
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
     * 强制结束任务，无论任务是否做完
     */
    public void forceCompletionTask(String taskId) {
        try {
            log.info("forceCompletionTask---task:{}", taskId);

            completionOfTask(taskId);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    /**
     * 任务所有点做完,完成,并且进度为100%的处理
     *
     * @param taskId 任务id
     */
    private void completionOfTask(String taskId) {
        log.info("\n————————————————————\n等待任务执行完毕————————————————————\n");
        // 延迟15秒执行
        ScheduledMapConfig.schedule(15, taskId, this::completionOfTaskDone);
    }

    private void completionOfTaskDone(String taskId) {
        try {
            int taskStatus;
            int all;
            boolean ended;
            synchronized (LOCK_FLAG) {
                String strForCountAbnormal = PATROL_SUMMARY_PREFIX + taskId;
                Map<String, String> resultCountsMap = redisTemplate.opsForHash().entries(strForCountAbnormal);

                ended = Boolean.parseBoolean(resultCountsMap.get("ended"));

                taskStatus = NumberUtils.toInt(resultCountsMap.get("taskState"), TASK_STATE_FINISHED);
                taskStatus = canFinish(taskStatus) ? TASK_STATE_FINISHED : taskStatus;

                all = MapUtils.getIntValue(resultCountsMap, "all", 0);

                resultCountsMap.put("taskState", String.valueOf(taskStatus));
                resultCountsMap.put("ended", "true");

                redisTemplate.opsForHash().putAll(strForCountAbnormal, resultCountsMap);
            }
            if (ended) {
                log.warn("taskId is:{} , already ended； {}", taskId, ended);
                // 表明已经入库过一次，不再重复入库，但需要更改任务状态，避免一直显示正在执行
                UPatrolResult uPatrolResult = new UPatrolResult().setTaskId(taskId).setTaskState(taskStatus);
                uPatrolResultDao.update(uPatrolResult);
                return;
            }

            Map<String, String> jasonMap = new HashMap<>(2);
            jasonMap.put("type", "lastOneInstance");
            jasonMap.put("taskId", taskId);
            log.info("最后一个点-前端推送：{}", JSON.toJSONString(jasonMap));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);

            int abnormalCounts = 0;
            List<UPatrolDataResult> uPatrolDataResultList = new ArrayList<>();
            Set<String> robotInfoKeys = redisScan(PATROL_TASK_PREFIX + taskId + ":");
            List<Map<String, String>> taskInfoList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>) connection -> {
                robotInfoKeys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
                return null;
            });

            List<Long> missInstanceMapList = new ArrayList<>();

            for (Map<String, String> redisInfoMap : taskInfoList) {
                UPatrolDataResult uPatrolDataResult = new UPatrolDataResult();
                uPatrolDataResult.setTaskId(taskId);
                uPatrolDataResult.setDeviceId(NumberUtils.toLong(redisInfoMap.get("deviceId")));
                uPatrolDataResult.setDeviceName(redisInfoMap.get("deviceName"));
                uPatrolDataResult.setDeviceMeteId(NumberUtils.toLong(redisInfoMap.get("deviceMeteId")));
                uPatrolDataResult.setDeviceMeteName(redisInfoMap.get("deviceMeteName"));
                uPatrolDataResult.setCustomId(redisInfoMap.get("customId"));
                uPatrolDataResult.setCustomName(redisInfoMap.get("customName"));
                uPatrolDataResult.setDevicePointId(redisInfoMap.get("devicePointId"));
                uPatrolDataResult.setInstanceId(NumberUtils.toLong(redisInfoMap.get("instanceId")));
                uPatrolDataResult.setInstanceName(redisInfoMap.get("instanceName"));
                uPatrolDataResult.setCruiseId(NumberUtils.toLong(redisInfoMap.get("cruiseId")));
                uPatrolDataResult.setCruiseName(redisInfoMap.get("cruiseName"));
                uPatrolDataResult.setCruiseTime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")));
                uPatrolDataResult.setCruiseStatus(NumberUtils.toInt(redisInfoMap.get("cruiseStatus")));
                uPatrolDataResult.setResultNum(redisInfoMap.get("resultNum"));
                uPatrolDataResult.setResultDesc(redisInfoMap.get("resultDesc"));
                uPatrolDataResult.setUnit(redisInfoMap.get("unit"));
                uPatrolDataResult.setPicpath(redisInfoMap.get("picpath"));
                uPatrolDataResult.setCruiseType(NumberUtils.toInt(redisInfoMap.get("cruiseType")));
                uPatrolDataResult.setOrigpic(redisInfoMap.get("origpic"));
                uPatrolDataResult.setCruiseAbnormal(NumberUtils.toInt(redisInfoMap.get("cruiseAbnormal")));
                uPatrolDataResult.setEvaluationState(MapUtils.getIntValue(redisInfoMap, "evaluationState", EVALUATION_STATE_UN));
                uPatrolDataResult.setCreatetime(new Date());
                uPatrolDataResult.setIsWarn(NumberUtils.toInt(redisInfoMap.get("isWarn")));
                uPatrolDataResult.setCruiseResult(NumberUtils.toInt(redisInfoMap.get("cruiseResult")));

                uPatrolDataResultList.add(uPatrolDataResult);
                if (CRUISE_RESULT_NORMAL != uPatrolDataResult.getCruiseResult()) {
                    abnormalCounts++;
                }
                if (String.valueOf(CRUISE_STATE_OMIT).equals(redisInfoMap.get("cruiseStatus"))) {
                    missInstanceMapList.add(NumberUtils.toLong(redisInfoMap.get("instanceId")));
                }
            }

            if (CollectionUtils.isNotEmpty(missInstanceMapList)) {
                log.info("任务：{}开始重试！",taskId);
                ThreadPoolUtil.PATROL_POOL.addThread(new CruiseRetryThread(uPatrolTaskDao,this,missInstanceMapList,taskId));
            }

            // 更新upr
            int allCounts = robotInfoKeys.size();
            UPatrolResult uPatrolResult = new UPatrolResult().setTaskId(taskId);
            uPatrolResult.setTaskState(taskStatus).setTaskWait(0).setEndTime(new Date()).setTaskAbnormal(abnormalCounts);
            // 如果 Redis 状态中总点数为 0，则表示非上级系统下发任务，需更新总点数值
            if ( all == 0) {
                uPatrolResult.setTaskCount(allCounts);
            }
            // 获取当前站内的环境数据并添加
            String stationWeather = getStationWeather();
            uPatrolResult.setWeather(stationWeather);

            uPatrolResultDao.update(uPatrolResult);
            log.info("taskId is:{} , uPatrolDataResultList size is:{}, ended； {}", taskId, uPatrolDataResultList.size(), ended);

            // 如果 ended=true,表示巡视结果已经入库过一次，不再重复入库，但需要修改
            if (CollectionUtils.isNotEmpty(uPatrolDataResultList)) {
                batchInsertUPatrolDataResult(uPatrolDataResultList);
                if (!Constant.fastTurbo()) {
                    log.info("准备传其他服务的taskId==={}", taskId);
                    uPatrolDataResultService.updateCruiseAnalyze(taskId);

                    // 理论上不需要这一步处理，影响效率，先去掉 Chenfei 20230515
                    // for (UPatrolDataResult up : uPatrolDataResultList) {
                    //     updateIsWarn(taskId, up.getInstanceId(), up.getCruiseDataId());
                    // }
                }
            }
            if (!Constant.fastTurbo()) {
                //低优先任务继续
                lowTaskGoOn(taskId);
            }
            int state = 1;
            switch (taskStatus) {
                case TASK_STATE_INTERRUPT:
                case TASK_STATE_ABNORMAL:
                    state = 4;
                    break;
                case TASK_STATE_TIMEOUT:
                    state = 6;
                    break;
                default:
                    break;
            }

            //任务状态上报站端
            sendTaskStateToUp(taskId, state);
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
    }

    private boolean canFinish(Integer taskState) {
        return ArrayUtils.contains(new int[]{TASK_STATE_EXECUTING,TASK_STATE_PAUSE,TASK_STATE_NOT_START},taskState);
    }

    public String getStationWeather(){
        String temperature = String.valueOf(redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("temperature", ""))
                 + redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("temperatureUnit", "");
        String humidity = String.valueOf(redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("humidity", ""))
                + redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("humidityUnit", "");
        String windSpeed = String.valueOf(redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("windSpeed", ""))
                + redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("windSpeedUnit", "");
        String precipitation = String.valueOf(redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("precipitation", ""))
                + redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("precipitationUnit", "");
        String windDirection = String.valueOf(redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("windDirection", ""));
        String airPressure = String.valueOf(redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("airPressure", ""))
                + redisTemplate.opsForHash().entries("weatherInfoForLastValue").getOrDefault("airPressureUnit", "");
        temperature = StringUtils.isEmpty(temperature) ? "暂无" : temperature;
        humidity = StringUtils.isEmpty(humidity) ? "暂无" : humidity;
        windSpeed = StringUtils.isEmpty(windSpeed) ? "暂无" : windSpeed;
        precipitation = StringUtils.isEmpty(precipitation) ? "暂无" : precipitation;
        windDirection = StringUtils.isEmpty(windDirection) ? "暂无" : windDirection;
        airPressure = StringUtils.isEmpty(airPressure) ? "暂无" : airPressure;
        String weather = "气温:" + temperature + ",湿度:" + humidity + ",风速:" + windSpeed + ",雨量:" + precipitation + ",风向:" + windDirection + ",气压:" + airPressure;
        log.info("====Now the environmental data is {}", weather);
       return weather;
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
//        log.info("dayBefore：{}", dayBefore);
//        log.info("dayAfter：{}", dayAfter);

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

//                log.info("dayBefore={}，dayAfter={}", dayBeforeTime, dayAfterTime);
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
            } else if (!(tCruiseTaskCount.getIfRun() == TaskTypeEnum.CYCLE.getType() && "238".equals(tCruiseTaskCount.getTaskState()))){
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
        try {
            if (Objects.equals(null, taskStartDate)) {
                taskStartDate = new Date();
            }
            DateTime start = DateUtil.beginOfMonth(taskStartDate);
            DateTime end = DateUtil.endOfMonth(taskStartDate);
            map.put("firstDay", DateTimeUtil.format(start));
            map.put("lastDay", DateTimeUtil.format(end));
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return map;
    }

    private Map<String, String> yearHandle(Date taskStartDate) {
        Map<String, String> map = new HashMap<>();
        try {
            if (Objects.equals(null, taskStartDate)) {
                taskStartDate = new Date();
            }
            DateTime start = DateUtil.beginOfYear(taskStartDate);
            DateTime end = DateUtil.endOfYear(taskStartDate);
            map.put("firstDay", DateTimeUtil.format(start));
            map.put("lastDay", DateTimeUtil.format(end));
        }catch (Exception e){
            log.error(e.getMessage(), e);
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
        DateTime firstDay = DateUtil.beginOfMonth(taskStartDate);
        DateTime lastDay = DateUtil.endOfMonth(taskStartDate);

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
//        log.info("list: " + list);
        List<TCruiseTaskDel> listDel = this.tCruiseTaskDelDao.slectByTimeZone(dayBefore, dayAfter);
        List<Map<String, Object>> listTask = new ArrayList<>();
        for (TCruiseTaskCount tCruiseTaskCount : list) {
            Date dayBeforeTime = dayBefore;
            Date dayAfterTime = dayAfter;
            if (tCruiseTaskCount.getIfRun() == TaskTypeEnum.CYCLE.getType() && !StringUtils.isEmpty(tCruiseTaskCount.getDateType())) {
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
            } else if (!(tCruiseTaskCount.getIfRun() == TaskTypeEnum.CYCLE.getType() && "238".equals(tCruiseTaskCount.getTaskState()))){
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
//        log.info("listTask: " + listTask);
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
    public UPatrolTask selectTaskByTaskCode(String taskCode){
        return uPatrolTaskDao.selectTaskByTaskCode(taskCode);
    }

    @Transactional(rollbackFor = Exception.class)
    public String selectTaskCodeByTaskId(String taskId){
        return uPatrolTaskDao.selectTaskCodeByTaskId(taskId);
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
    public void batchInsertUPatrolDataResult(List<UPatrolDataResult> uPatrolDataResultList) {
        if (uPatrolDataResultList.size() > 1000) {
            List<List<UPatrolDataResult>> lists = Lists.partition(uPatrolDataResultList, 1000);
            ExecutorService executorService = Executors.newFixedThreadPool(lists.size());
            lists.forEach(subList ->
                    executorService.submit(() -> {
                        uPatrolDataResultDao.batchInsertUPatrolDataResult(subList);
                    }));
            executorService.shutdown();
        } else {
            uPatrolDataResultDao.batchInsertUPatrolDataResult(uPatrolDataResultList);
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
            scanParams.match(key + "*");
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

    private void lowTaskGoOn(String taskId){
        String lowTaskKey = TASK_LOWER_REDIS_KEY + taskId;
        Set<String> lowTaskList = redisTemplate.opsForSet().members(lowTaskKey);
        log.info("低优先级任务继续，lowTaskList: {}", JSON.toJSONString(lowTaskList));
        if (lowTaskList != null && lowTaskList.size() > 0) {
            lowTaskList.forEach(lowTask -> {
                try {
                    taskGoOn(lowTask, false, null);
                }catch (Exception e){
                    log.info("低优先级任务继续出错：",e);
                }
            });
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> selectForSequenceInfoByMeteId(String meteId) {
        return uPatrolTaskDao.selectForSequenceInfoByMeteId(meteId);
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> selectRobotTaskProgress(Long robotId) throws Exception {
        String sysLevel = Constant.getLevelEdge();
        Map<String, Object> reMap = new HashMap<>();
        Map<String,String> taskMap = uPatrolTaskDao.selectRobotTaskOnStartV2(robotId);
        if (Constant.LEVEL_UP_SYSTEM.equals(sysLevel)) {
            if (MapUtils.isEmpty(taskMap)) {
                taskMap = Maps.newHashMap();
            }
            TRobotInfo tRobotInfo = tRobotInfoDao.selectByPrimaryId(robotId);
            if (Objects.nonNull(tRobotInfo)) {
                String taskCode = String.valueOf(redisTemplate.opsForValue().get("robotTaskUpInfo:" + tRobotInfo.getRobotNum()));
                if (StringUtils.isNotBlank(taskCode)) {
                    String taskId = uPatrolTaskDao.selectCurrentTaskId(taskCode);
                    if (StringUtils.isNotBlank(taskId)) {
                        taskMap.put("taskId",taskId);
                        taskMap.put("taskCode",taskCode);
                    }
                }
            }
        }

        if (MapUtils.isNotEmpty(taskMap)) {
            String taskId = taskMap.get("taskId");
            String taskCode = taskMap.get("taskCode");
            reMap.put("taskId", taskId);
            Map<String, String> robotOrDroneTaskInfo = redisTemplate.opsForHash().entries(ROBOT_OR_DRONE_TASK+taskCode+":"+robotId);
            if (robotOrDroneTaskInfo.size() == 0) {
                reMap.put("taskProgress", 0);
                reMap.put("taskName", "");
                reMap.put("startTime", "");
                reMap.put("taskState", "");
                return reMap;
            }
            String re = Optional.ofNullable(robotOrDroneTaskInfo.get("taskProgress")).orElse("0");
            UPatrolResult result = uPatrolResultDao.selectByPrimaryId(taskId);
            if (result == null) {
                reMap.put("taskProgress", 0);
                reMap.put("taskName", "");
                reMap.put("startTime", "");
                reMap.put("taskState", "");
            } else {
                if (result.getTaskState() == 239 || result.getTaskState() == 241) {
                    reMap.put("taskProgress", re);
                    reMap.put("taskName", result.getTaskName());
                    reMap.put("startTime", robotOrDroneTaskInfo.get("startTime"));
                    String state = robotOrDroneTaskInfo.get("taskState");
                    reMap.put("taskState", taskStatusToString(state));
                    List<RobotTaskMessage> list = selectRobotTaskMessage(sysLevel,taskId,taskCode, robotId ,robotOrDroneTaskInfo);
                    reMap.put("list", list);
                } else {
                    reMap.put("taskProgress", 0);
                    reMap.put("taskName", "");
                    reMap.put("startTime", "");
                    reMap.put("taskState", "");
                }
            }

        }else {
            reMap.put("taskProgress", 0);
            reMap.put("taskName", "");
            reMap.put("startTime", "");
            reMap.put("taskState", "");
            reMap.put("taskId", "");
        }
        return reMap;
    }

    private String taskStatusToString(String state) {
        if (!StringUtils.isEmpty(state)) {
            //1=已执行 2=正在执行 3=暂停 4=终止 5=未执行 6=超期
            if ("1".equals(state)) {
                state = "已执行";
            }
            if ("2".equals(state)) {
                state = "正在执行";
            }
            if ("3".equals(state)) {
                state = "暂停";
            }
            if ("4".equals(state)) {
                state = "终止";
            }
            if ("5".equals(state)) {
                state = "未执行";
            }
            if ("6".equals(state)) {
                state = "超期";
            }
        } else {
            state = "";
        }
        return state;
    }

    public List<RobotTaskMessage> selectRobotTaskMessage(String sysLevel,String taskId,String taskCode, Long robotId,Map<String, String> mapForRobotState) throws Exception {
        List<RobotTaskMessage> re = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if ("3".equals(sysLevel)) {
            // String taskId = uPatrolTaskDao.selectTaskByRobotTaskCode(robotPatrolTaskStatus.getTaskCode());
            String cruiseResultKey = PATROL_TASK_PREFIX + taskId + ":";
            //处理结果 获取机器人的点
            Set<String> keys = redisScan(cruiseResultKey);
            if (!keys.isEmpty()) {
                for (String item : keys) {
                    //获取任务数据
                    Map<String, String> mapForRobotTaskMessage = redisTemplate.opsForHash().entries(item);
                    RobotTaskMessage robotTaskMessage = new RobotTaskMessage();
                    robotTaskMessage.setDeviceName(mapForRobotTaskMessage.get("deviceName"));
                    robotTaskMessage.setInstanceName(mapForRobotTaskMessage.get("instanceName"));
                    if (mapForRobotTaskMessage.get("cruiseTime") != null && !"null".equals(mapForRobotTaskMessage.get("cruiseTime"))) {
                        robotTaskMessage.setCruiseTime(mapForRobotTaskMessage.get("cruiseTime"));
                        robotTaskMessage.setResult(mapForRobotTaskMessage.get("resultDesc"));
                    } else {
                        robotTaskMessage.setCruiseTime("");
                        robotTaskMessage.setResult("");
                    }
                    re.add(robotTaskMessage);
                }
            }
            return re;
        }

        //获取此机器人的巡视点
        String robotCode = tRobotInspectionDao.selectRobotCode(robotId);
        String robotState = mapForRobotState.get("taskState");
        if ("4".equals(robotState)) {
            //机器人未在做任务
//            Map<String,String> jasonMap=new HashMap<>();
//            jasonMap.put("type","noTask");
//            //jasonMap.put("taskId",tCruiseTask.getTaskId());
//            String json= JSON.toJSONString(jasonMap);
//            Constant.websocketSendMsg(Constant.WEBSOCKET_URL,jasonMap);
//            log.info("发送给前端的消息-停止调接口：   "+json);
            return null;
        }

        List<String> instanceIdList = uPatrolTaskDao.selectRobotTaskInstanceList(taskCode);
        //List<TCruisePointAttr> nameList =  tRobotInspectionDao.selectRobotTaskMessage(instanceIdList);
        for (String item : instanceIdList) {
            //获取任务数据
            Map<String, String> mapForRobotTaskMessage = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + item);
            RobotTaskMessage robotTaskMessage = new RobotTaskMessage();
            robotTaskMessage.setDeviceName(mapForRobotTaskMessage.get("deviceName"));
            robotTaskMessage.setInstanceName(mapForRobotTaskMessage.get("instanceName"));
            if (mapForRobotTaskMessage.get("cruiseTime") != null && !"null".equals(mapForRobotTaskMessage.get("cruiseTime"))) {
                robotTaskMessage.setCruiseTime(mapForRobotTaskMessage.get("cruiseTime"));
                robotTaskMessage.setResult(mapForRobotTaskMessage.get("resultDesc"));
            } else {
                robotTaskMessage.setCruiseTime("");
                robotTaskMessage.setResult("");
            }
            re.add(robotTaskMessage);
        }
        return re;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Map<String,Object>> selectTaskPriorityConfigList(){
        return uPatrolTaskDao.selectTaskPriorityConfigList();
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateTaskPriorityConfig(Integer type,Integer level){
        uPatrolTaskDao.updateTaskPriorityConfig(type, level);
        taskPriorityConfigToRedis();
    }

    @Transactional(rollbackFor = Exception.class)
    public void taskPriorityConfigToRedis(){
        List<Map<String,Object>> priorityConfigs = uPatrolTaskDao.selectTaskPriorityConfigList();
        for(Map<String,Object> item:priorityConfigs){
            String str = TASK_PRIORITY_REDIS_KEY+item.get("type");
            log.info("str:{}",str);
            Map<String,String> map = redisTemplate.opsForHash().entries(str);
            if(map != null && map.size()>0){
                redisTemplate.delete(str);
            }else {
                map = new HashMap<>();
            }
            map.put("type",item.get("type").toString());
            map.put("level",item.get("level").toString());
            map.put("name",item.get("name").toString());
            redisTemplate.opsForHash().putAll(str, map);
        }
    }

    public String downloadPicture(String source,String target) {
        try {
            FtpsUtil.downloadFile(source, target, applicationProperties.getIntelAnalysisFtps().getIp(), applicationProperties.getIntelAnalysisFtps().getPort(), applicationProperties.getIntelAnalysisFtps().getUserName(), applicationProperties.getIntelAnalysisFtps().getPassword());
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return "";
    }

    @Transactional(rollbackFor = Exception.class)
    public String upSystemCtrl(XMLBaseModel xmlBaseModel) throws Exception {
        String com = xmlBaseModel.getCommand();
        String code = xmlBaseModel.getCode();
        String taskId = code.contains("_") ? code.split("_").length > 2 ? StringUtils.substringBetween(code, "_") : StringUtils.substringBefore(code, "_") : code;
        log.info("taskId : {} control", taskId);
        switch (com) {
            case "1":
                return this.taskStart(taskId, null, false);
            case "2":
                return String.valueOf(this.taskPause(taskId, null));
            case "3":
                return String.valueOf(this.taskGoOn(taskId, true, null));
            case "4":
                this.taskShutDown(taskId, "");
                return "1";
            default:
                return "-1";
        }
    }

    /**
     * 根据任务id查询名称
     * @param taskId
     * @return
     */
    public String selectTaskName(String taskId) {
        return uPatrolTaskDao.selectTaskName(taskId);
    }

    /**
     * 遗漏点位重试任务
     * @param instanceIdList
     * @param uPatrolTaskParam
     * @return
     */
    public UPatrolTask omitInstanceRetry(List<Long> instanceIdList,UPatrolTask uPatrolTaskParam) {
        log.info("任务：{}正在发起重试",uPatrolTaskParam);
        if (!Constant.isHost()) {
            log.info("当前任务 \"{}\" 非巡视主机，不执行重试！",uPatrolTaskParam.getTaskName());
            return null;
        }
        Object retry = redisTemplate.opsForValue().get(TASK_RETRY_PREFIX + uPatrolTaskParam.getTaskId());
        log.info("{}",retry);
        if (Objects.nonNull(retry)) {
            log.info("当前任务 \"{}\"为重试任务不再重试！",uPatrolTaskParam.getTaskName());
            return null;
        }


        TCruiseTaskAdd tCruiseTaskAdd = new TCruiseTaskAdd();
        tCruiseTaskAdd.setIfRun(173);
        tCruiseTaskAdd.setTaskName(uPatrolTaskParam.getTaskName() + TASK_RETRY_SUFFIX);
        tCruiseTaskAdd.setPlanId(uPatrolTaskParam.getPlanId());
        tCruiseTaskAdd.setCycleExecuteTime("");
        tCruiseTaskAdd.setCycleMonth("");
        tCruiseTaskAdd.setCycleWeek("");
        tCruiseTaskAdd.setIntervalEndTime("");
        tCruiseTaskAdd.setIntervalExecuteTime("");
        tCruiseTaskAdd.setIntervalNumber("");
        tCruiseTaskAdd.setIntervalStartTime("");
        tCruiseTaskAdd.setIntervalType("");
        tCruiseTaskAdd.setMin("");
        tCruiseTaskAdd.setMonth("");
        tCruiseTaskAdd.setYear("");
        UPatrolTask uPatrolTask = dealTaskInfo(tCruiseTaskAdd);

        // 设置任务优先级
        setLevel(uPatrolTask, tCruiseTaskAdd);
        uPatrolTask.setCreateTime(new Date());
        if (Objects.isNull(uPatrolTask.getTaskId())) {
            uPatrolTask.setTaskId(String.valueOf(UUID.randomUUID()).replace("-", ""));
        }
        if (Objects.isNull(uPatrolTask.getTaskCode())) {
            uPatrolTask.setTaskCode(uPatrolTask.getTaskId());
        }

        redisTemplate.opsForValue().set(TASK_RETRY_PREFIX + uPatrolTask.getTaskId(),"done",3,TimeUnit.DAYS);

        List<Long> instanceList = insertTaskAttrForRetry(uPatrolTask, tCruiseTaskAdd,instanceIdList);
        if (CollectionUtils.isEmpty(instanceList)) {
            return null;
        }
        List<TCruisePointInstanceNameDetail> detailList = initializeNextTaskInfo(uPatrolTask, instanceList);
            // 找出下级设备或下级节点的点让其做任务
            String res = taskToEdgeOrDevice(uPatrolTask, tCruiseTaskAdd, format, detailList);
            if (StringUtils.isNotEmpty(res)) {
                throw new BusinessException(ResultCodeEnum.CODE10001.getCode(), res);
            }

        return uPatrolTask;
    }

    private List<Long> insertTaskAttrForRetry(UPatrolTask uPatrolTask, TCruiseTaskAdd tCruiseTaskAdd,List<Long> instanceIdList) {
        List<Long> instanceList = new ArrayList<>();
        List<UPatrolTaskAttr> uPatrolTaskAttrs = new ArrayList<>();
        List<TCruisePointInstanceNameDetail> tCruisePointInstanceNameDetails = tCruisePointInstanceDao.selectForTask(instanceIdList);
        if (CollectionUtils.isEmpty(tCruisePointInstanceNameDetails)) {
            log.info("遗漏点位重试任务执行失败，点位已全部取消！");
            return new ArrayList<>();
        }
        for (TCruisePointInstanceNameDetail tCruisePointInstanceNameDetail : tCruisePointInstanceNameDetails) {
            if (!instanceIdList.contains(tCruisePointInstanceNameDetail.getInstanceId())) {
                continue;
            }

            //通过巡视点为关联关系判断遗漏点位是否重做
            UPatrolTaskAttr uPatrolTaskAttr = new UPatrolTaskAttr();
            uPatrolTaskAttr.setTaskId(uPatrolTask.getTaskId());
            uPatrolTaskAttr.setInstanceId(tCruisePointInstanceNameDetail.getInstanceId());
            uPatrolTaskAttr.setDeviceMeteId(tCruisePointInstanceNameDetail.getDeviceMeteId());
            uPatrolTaskAttr.setDeviceId(tCruisePointInstanceNameDetail.getDeviceId());
            uPatrolTaskAttr.setCustomId(tCruisePointInstanceNameDetail.getCustomId());
            uPatrolTaskAttr.setPointType(tCruisePointInstanceNameDetail.getCruiseType());
            uPatrolTaskAttr.setDeviceType(tCruisePointInstanceNameDetail.getDeviceType());
            uPatrolTaskAttr.setMeteType(NumberUtils.toInt(tCruisePointInstanceNameDetail.getMeteType()));
            uPatrolTaskAttr.setRegionId(tCruisePointInstanceNameDetail.getUpRegionId());

            instanceList.add(tCruisePointInstanceNameDetail.getInstanceId());
            uPatrolTaskAttrs.add(uPatrolTaskAttr);
            if (uPatrolTaskAttrs.size() % 2000 == 0) {
                this.uPatrolTaskAttrDao.batchAdd(uPatrolTaskAttrs);
                uPatrolTaskAttrs = new ArrayList<>();
            }
        }

        TCruisePlanCount plan = tCruisePlanDao.selectByPrimaryId(tCruiseTaskAdd.getPlanId());
        tCruiseTaskAdd.setType(plan.getType());
        uPatrolTask.setTaskType(tCruiseTaskAdd.getType());

        if (uPatrolTaskAttrs.size() > 0) {
            uPatrolTaskAttrDao.batchAdd(uPatrolTaskAttrs);
        }
        uPatrolTask.setCreateTime(new Date());
        uPatrolTaskDao.add(uPatrolTask);
        log.info("instanceList {}", instanceList);
        if (!Constant.isHost()) {
            Constant.modelUpload("7");
        }
        return instanceList;

    }
}
