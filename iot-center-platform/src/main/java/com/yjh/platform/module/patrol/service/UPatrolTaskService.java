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
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.quartz.JobManager;
import com.yjh.platform.common.quartz.QuartzTask;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.common.utils.*;
import com.yjh.platform.common.utils.smUtil.Demo;
import com.yjh.platform.configuration.IntelAnalysisFtpsConfig;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.Analysis;
import com.yjh.platform.module.device.entity.RobotTaskMessage;
import com.yjh.platform.module.device.entity.TCruisePointInstanceNameDetail;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.*;
import com.yjh.platform.module.patrol.entity.XMLBaseModel;
import com.yjh.platform.module.patrol.entity.*;
import com.yjh.platform.module.patrol.thread.LocalCruiseExecutThread;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.dao.TCruiseTaskDelDao;
import com.yjh.platform.module.task.dao.TPeriodModelDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.service.RunAtNowTask;
import com.yjh.platform.module.user.dao.SysUserDao;
import com.yjh.platform.module.user.dao.TRobotInfoDao;
import com.yjh.platform.module.user.entity.SysUser;
import com.yjh.platform.module.user.entity.TRobotInfo;
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
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
    public static final String TASK_PRIORITY_REDIS_KEY="task_priority_config:";
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
    private AnalyseDataOperateService analyseDataOperateService;
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
    private IntelAnalysisFtpsConfig intelAnalysisFtpsConfig;

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

    /**
     * 任务下发，在外层处理设置定时器逻辑，不走事物，否则会导致定时器延时
     */
    public String addTask(TCruiseTaskAdd tCruiseTaskAdd){
        // insert 需要走事物，使用 AopContext.currentProxy 获取当前代理，走事物处理
        UPatrolTaskService proxy = SpringBeanUtils.getBean(UPatrolTaskService.class);
        assert proxy != null;
        UPatrolTask uPatrolTask = proxy.insert(tCruiseTaskAdd);

        // 设置定时器，不走事物逻辑，否则会延时
        setQuartzTask(uPatrolTask);

        String stationCode = (String) redisTemplate.opsForHash().get("t_sys_param:edgeId", "content");
        return stationCode + "_" + uPatrolTask.getTaskId() + "_" + DateTimeUtil.format3(uPatrolTask.getStartTime());
    }

    @Transactional(rollbackFor = Exception.class)
    public UPatrolTask insert(TCruiseTaskAdd tCruiseTaskAdd) {
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
        // 找出下级设备或下级节点的点让其做任务
        String res = taskToEdgeOrDevice(uPatrolTask, tCruiseTaskAdd, format, detailList);
        if (StringUtils.isNotEmpty(res)) {
            throw new BusinessException(ResultCodeEnum.CODE10001.getCode(), res);
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
                    String hour = intervalExecuteTime.startsWith("0") ? intervalExecuteTime.substring(1, 2) : intervalExecuteTime.substring(0,1);
                    String min = intervalExecuteTime.substring(3, 5).startsWith("0") ? intervalExecuteTime.substring(7, 8) : intervalExecuteTime.substring(3, 5);
                    String second = intervalExecuteTime.substring(6, 8).startsWith("0") ? intervalExecuteTime.substring(7, 8) : intervalExecuteTime.substring(6, 8);

                    // 天: 秒 分 时 */日 * ?
                    if (StringUtils.equals("2", intervalType)) {
                        cronExpressionDate = String.format("%s %s %s */%s * ?", second, min, hour, intervalNumber);
                    }
                    // 时: 秒 分 */时 * * ？
                    else {
                        cronExpressionDate = String.format("%s %s */%s * * ?", second, min, intervalNumber);
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
            if (Objects.nonNull(tCruiseTaskAdd.getTaskCode())) {
                uPatrolTask.setTaskId(tCruiseTaskAdd.getTaskCode());
                uPatrolTask.setTaskCode(tCruiseTaskAdd.getTaskCode());
            }
            if (Objects.nonNull(tCruiseTaskAdd.getStartTime()) && !Objects.equals("", tCruiseTaskAdd.getStartTime())) {
                uPatrolTask.setStartTime(tCruiseTaskAdd.getStartTime());
            } else {

                uPatrolTask.setStartTime(new Date());
                uPatrolTask.setEndTime(new Date());
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
        Object level1 = redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY + "901", "level");
        Object level3 = redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY + "903", "level");
        Object level4 = redisTemplate.opsForHash().get(TASK_PRIORITY_REDIS_KEY + "904", "level");
        Integer ifRun = uPatrolTask.getExecuteType();
        if (tCruiseTaskAdd.getTaskLevel() == null) {
            String sysLevel = String.valueOf(redisTemplate.opsForHash().entries("t_sys_param:edgeLevel").get("content"));
            if("3".equals(sysLevel)) {
                // 上级系统
                uPatrolTask.setTaskLevel(2);
                return;
            }
            if (Objects.equals("0", tCruiseTaskAdd.getUnionTaskStatus()) || tCruiseTaskAdd.getUnionTaskStatus() == null) {
                if (Objects.equals(TaskTypeEnum.NOW.getType(), ifRun)) {
                    uPatrolTask.setTaskLevel(Objects.isNull(level3) ? 3 : Integer.parseInt(String.valueOf(level3)));
                } else {
                    uPatrolTask.setTaskLevel(Objects.isNull(level1) ? 1 : Integer.parseInt(String.valueOf(level1)));
                }
            } else {
                uPatrolTask.setTaskLevel(Objects.isNull(level4) ? 4 : Integer.parseInt(String.valueOf(level4)));
            }
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
                uPatrolTaskAttr.setPointTaskId(uPatrolTaskAttr.getPointTaskId());
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
        return instanceList;
    }


    public List<TCruisePointInstanceNameDetail> initializeTaskInfo(List<Long> instanceList, UPatrolTask task)  {
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
                .setCreateTime(new Date())
                .setTaskCount(instanceList.size())
                .setTaskWait(instanceList.size())
                .setRemark("0")
                .setCreateTime(now);
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
                    .setCruiseType(item.getCruiseType()).setCreatetime(now);
            Map map = Object2Map.objectToMap(uPatrolDataResult, true);
            map.put("edgeCode", Optional.ofNullable(item.getEdgeCode()).orElse(""));
            map.put("deviceMeteId", String.valueOf(item.getDeviceMeteId()));
            map.put("taskName", task.getTaskName());
            map.put("startTime", DateTimeUtil.format3(task.getStartTime()));
            if(item.getCruiseType() != 228){
                map.put("cameraId", String.valueOf(item.getCameraId()));
                map.put("robotId","");
            }else {
                map.put("cameraId","");
                map.put("robotId", String.valueOf(item.getRobotId()));
            }
            String str = PATROL_TASK_PREFIX + task.getTaskId() + ":" + item.getInstanceId();
            log.info("str {}", str);
            log.info("map {}", map);
            log.info("task {}", task);
            redisTemplate.opsForHash().putAll(str, map);
        }
        initializeThisTaskInfo(task,instanceList);
        sendTaskStateToUp(task, 5);
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

    public void initializeThisTaskInfo(UPatrolTask task, List<Long> instanceList) {

        Map<String, String> mapForAbnormal = new HashMap<>();
        mapForAbnormal.put("all", String.valueOf(instanceList.size()));
        mapForAbnormal.put("abnormal", "0");
        mapForAbnormal.put("normal", "0");
        mapForAbnormal.put("taskStart", DateTimeUtil.format(task.getStartTime()));
        mapForAbnormal.put("taskState", String.valueOf(CruiseConstant.TASK_STATE_NOT_START));

        String strForCountAbnormal = PATROL_SUMMARY_PREFIX + task.getTaskId();
        redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
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
            if (StringUtils.isEmpty(taskId)) {
                taskId = tRobotInspectionDao.selectRealTaskId(taskCode, date);
            }
            if (StringUtils.isEmpty(taskId)) {
                taskId = patrolledId;
            }
            updateTaskProgress(robotPatrolTaskStatus, taskId, taskState);
            if (robotEnd) {
                String taskIdFinal = taskId;
                // 机器人/下级系统任务终止
                ThreadPoolUtil.PATROL_POOL.addThread(() -> dealRobotTaskShutDown(taskIdFinal));
            }

            //判断是不是机器人或者无人机
            Long robotId = tRobotInfoDao.selectRobotIdByCode(robotPatrolTaskStatus.getRobotCode());
            if (robotId != null){
                //放入redis
                redisTemplate.opsForHash().putAll(ROBOT_OR_DRONE_TASK+robotPatrolTaskStatus.getTaskCode()+":"+robotId,Object2Map.objectToMap(robotPatrolTaskStatus));
                redisTemplate.opsForHash().putAll("RobotTaskStatus:" + robotPatrolTaskStatus.getRobotCode() + ":" + taskCode, Object2Map.objectToMap(robotPatrolTaskStatus));
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

            UPatrolTask taskExsis = uPatrolTaskDao.selectThisTaskByTaskCode(robotPatrolTaskStatus.getTaskCode());
            if (taskExsis == null || Optional.ofNullable(taskExsis.getTaskSource()).orElse(0) == 1) {
                uPatrolTaskDao.add(uPatrolTask);
            } else {
                log.warn("Task already exsis, not insert, task: {}", JSON.toJSONString(taskExsis));
                return null;
            }
            UPatrolResult resultExsis = uPatrolResultDao.selectByPrimaryId(taskId);
            if (resultExsis == null) {
                uPatrolResultDao.add(uPatrolResult);
            } else {
                log.warn("Task result already exsis: {}", JSON.toJSONString(resultExsis));
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
    private void updateTaskProgress(RobotPatrolTaskStatus robotPatrolTaskStatus, String taskId, int taskState){
        String key = PATROL_SUMMARY_PREFIX + taskId;
        Map<String, String> map = redisTemplate.opsForHash().entries(key);
        if (MapUtils.isEmpty(map)) {
            map = new HashMap<>(16);
            map.put("taskStart", robotPatrolTaskStatus.getStartTime());
            map.put("abnormal", "0");
            map.put("normal", "0");
            map.put("all", "0");
            // taskSource==1 下级创建任务主动上报
            map.put("taskSource", "1");
            map.put("taskPatrolledId", robotPatrolTaskStatus.getTaskPatrolledId());
        }
        if (!"1".equals(map.get("taskSource"))) {
            log.info("Task create by self, don`t continue, taskId: {}", taskId);
            redisTemplate.opsForHash().put(key, "taskPatrolledId", robotPatrolTaskStatus.getTaskPatrolledId());
            return;
        }

        try {
            if (TASK_STATE_NOT_START == MapUtils.getIntValue(map, "taskState", TASK_STATE_NOT_START) && TASK_STATE_EXECUTING == taskState) {
                UPatrolResult result =
                        new UPatrolResult().setTaskId(taskId).setTaskState(CruiseConstant.TASK_STATE_EXECUTING)
                                .setExecuteTime(DateTimeUtil.parse(robotPatrolTaskStatus.getStartTime(), new Date()));
                log.info("TaskResult start, taskId: {}", taskId);
                uPatrolResultDao.update(result);
            }

            map.put("taskState", String.valueOf(taskState));
            String progress = robotPatrolTaskStatus.getTaskProgress();
            if (StringUtils.contains(progress, "%")) {
                float pf = NumberUtils.toFloat(StringUtils.remove(progress, "%")) / 100F;
                progress = CommonUtils.percentFormat(pf, "#.####");
            }
            if (NumberUtils.isCreatable(progress)) {
                map.put("taskProgress", progress);
            }
            map.put("lastCruiseTime", DateTimeUtil.getDateTimeString());
            log.info("update down task status: {}", JSON.toJSONString(map));

            redisTemplate.opsForHash().putAll(key, map);
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

    private void dealRobotTaskShutDown(String taskId) {
        try {
            //等待30秒
            Thread.sleep(15 * 1000);
        } catch (Exception e) {
            log.error("等待出错：", e);
        }

        String key = PATROL_SUMMARY_PREFIX + taskId;
        String taskSource = (String)redisTemplate.opsForHash().get(key, "taskSource");
        if ("1".equals(taskSource)) {
            forceCompletionTask(taskId);
            return;
        }

        // String taskId = uPatrolTaskDao.selectTaskByRobotTaskCode(robotPatrolTaskStatus.getTaskCode());
        String cruiseResultKey = PATROL_TASK_PREFIX + taskId + ":";
        //处理结果 获取机器人的点
        Set<String> keys = redisScan(cruiseResultKey);

        List<Map<String, String>> resultMap = redisTemplate.executePipelined((RedisCallback<Map<String, String>>)connection -> {
            keys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
            return null;
        });

        resultMap.forEach(result -> {
            //判断是不是机器人的点以及还是否完成
            int cruiseType = MapUtils.getIntValue(result, "cruiseType");
            int cruiseState = MapUtils.getIntValue(result,"cruiseStatus");
            String instanceId = result.get("instanceId");
            if (TypeEnum.ROBOT.getCode() == cruiseType && CRUISE_STATE_UN == cruiseState) {
                //这个点 没有做
                result.put("cruiseStatus", String.valueOf(CRUISE_STATE_FAILED));//执行失败
                result.put("resultNum", "机器人任务异常");
                result.put("cruiseAbnormal", String.valueOf(CruiseConstant.CRUISE_ABNORMAL_DATAABNORMAL));//数据异常
                result.put("evaluationState", String.valueOf(CruiseConstant.EVALUATION_STATE_UN));//未审核
                result.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));//异常

                String instanceKey = cruiseResultKey + instanceId;
                redisTemplate.opsForHash().putAll(instanceKey, result);

                patrolTaskResultHandler(taskId, Long.valueOf(instanceId));
            }
        });
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
                Map<String, List<Long>> listMap = Maps.newHashMap();
                edgeDetailList.forEach(t -> {
                    List<Long> list = new ArrayList<>();
                    if (listMap.containsKey(t.getEdgeCode())) {
                        list = listMap.get(t.getEdgeCode());
                    }
                    list.add(Long.valueOf(t.getOriginId()));
                    listMap.put(t.getEdgeCode(), list);
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
                        taskInfo.setPriority(3);
                        taskInfo.setTaskName(task.getTaskName());
                        taskInfo.setInstanceList(instanceList);
                        String ifFun = String.valueOf(tCruiseTaskAdd.getIfRun());
                        taskInfo.setIfRun(ifFun);
                        taskInfo.setUnionTaskStatus(tCruiseTaskAdd.getUnionTaskStatus());
                        taskInfo.setEdgeCode(edgeCode);
                        packageTaskProtocolInfo(tCruiseTaskAdd, format, taskInfo, ifFun);
                        edgeTaskInfoList.add(taskInfo);
                    });

                    Map<String, List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>(3);
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
            Map<String, List<Long>> listMap = Maps.newHashMap();
            edgeDetailList.forEach(t -> {
                List<Long> list = new ArrayList<>();
                if (listMap.containsKey(t.getEdgeCode())) {
                    list = listMap.get(t.getEdgeCode());
                }
                list.add(Long.valueOf(t.getOriginId()));
                listMap.put(t.getEdgeCode(), list);
            });

            if (StringUtils.isNotEmpty(dateType)) {
                boolean moreTime = dateType.split(" ")[2].contains(",");
                if (moreTime && task.getExecuteType() == 172) {
                    List<RobotTaskInstanceInfo> edgeTaskInfoList = new ArrayList<>();
                    listMap.forEach((edgeCode, instanceList) -> {
                        RobotTaskInstanceInfo taskInfo = new RobotTaskInstanceInfo();
                        taskInfo.setCruiseType(task.getTaskType());
                        taskInfo.setTaskId(task.getTaskId());
                        taskInfo.setPriority(task.getTaskLevel());
                        taskInfo.setTaskName(task.getTaskName());
                        taskInfo.setInstanceList(instanceList);
                        taskInfo.setIfRun("173");
                        taskInfo.setEdgeCode(edgeCode);
                        taskInfo.setFixedStartTime(format.format(new Date()));
                        edgeTaskInfoList.add(taskInfo);
                    });

                    Map<String, List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>(3);
                    robotTaskInfoMap.put("robotTaskInfoList", edgeTaskInfoList);
                    log.info("edgeTaskInfoMap = {}", robotTaskInfoMap);

                    robotTask(robotTaskInfoMap);
                    log.info("下发成功！！！");
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
//                robotTaskInfo.setPlanCode(task.getPlanCode());
            robotTaskInfo.setPriority(task.getTaskLevel());
            robotTaskInfo.setTaskName(task.getTaskName());
            List<Long> robotTaskInstanceList = tRobotInspectionDao.selectRobotTaskInstanceId(robotInstanceList, item);
            robotTaskInfo.setInstanceList(robotTaskInstanceList);
            robotTaskInfo.setIfRun("173");
            robotTaskInfo.setRobotCode(item);
            robotTaskInfo.setFixedStartTime(format.format(new Date()));
            robotTaskInfoList.add(robotTaskInfo);
        }
        Map<String,List<RobotTaskInstanceInfo>> robotTaskInfoMap = new HashMap<>();
        robotTaskInfoMap.put("robotTaskInfoList",robotTaskInfoList);
        log.info("robotTaskInfoMap = {}", robotTaskInfoMap);
        //让机器人和无人机做任务
        log.info("task=={}, dateType: {}", task, dateType);
        if (StringUtils.isNotEmpty(dateType)) {
            boolean moreTime = dateType.split(" ")[2].contains(",");
            if (moreTime && task.getExecuteType() == 172) {
                robotTask(robotTaskInfoMap);
                log.info("下发成功！！！");
            }
        }

        // 暂时只给机器人和无人机发送了任务启动的命令 下级节点未考虑
        try {
            String startCommand = (String)redisTemplate.opsForHash().get("t_sys_param:robotStartCommand", "content");

            if (Boolean.parseBoolean(startCommand)) {
                List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(task.getTaskId());
                log.info("机器人任务启动,robotCodeList:{}", robotCodeList);
                if (robotCodeList != null && robotCodeList.size() > 0) {
                    Map<String, Object> robotTaskStatesMap = new HashMap<>();
                    robotTaskStatesMap.put("taskId", task.getTaskId());
                    robotTaskStatesMap.put("commandValue", 1);
                    robotTaskStatesMap.put("robotCodeList", robotCodeList);
                    robotTaskStates(robotTaskStatesMap);
                }
            }
        } catch (Exception e) {
            log.error("发送机器人/无人机启动错误：", e);
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

                    String intervalExecuteTime = tCruiseTaskAdd.getIntervalExecuteTime();
                    intervalExecuteTime = StringUtils.isNotEmpty(intervalExecuteTime) ? intervalExecuteTime.substring(11) : intervalExecuteTime;
                    taskInfo.setIntervalExecuteTime(intervalExecuteTime);

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
            item.put("task_patrolled_id", stationCode + "_" +task.getTaskId() + "_" + DateTimeUtil.format3(task.getStartTime()));
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
                //删除初始化的一条
                uPatrolTaskDao.deleteInitByPrimaryId(taskId);
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
        //删除初始化的一条
        uPatrolTaskDao.deleteInitByPrimaryId(taskId);
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
            List<String> robotCodeList = uPatrolTaskDao.selectRobotIsRunning(taskId);
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
            List<String> robotCodeList = uPatrolTaskDao.selectRobotIsRunning(taskId);
            log.info("机器人任务暂停,robotCodeList:{}", robotCodeList);
            if (robotCodeList != null && robotCodeList.size() > 0) {
                Map<String, Object> robotTaskStatesMap = new HashMap<>();
                robotTaskStatesMap.put("taskId", taskId);
                robotTaskStatesMap.put("commandValue", 2);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                robotTaskStates(robotTaskStatesMap);
            }
            updateTaskStateForRedis(taskId, String.valueOf(TASK_STATE_PAUSE));
            Map<String, String> jasonMapOnFinished = new HashMap<>();
            jasonMapOnFinished.put("type", "taskChange");
            jasonMapOnFinished.put("taskId", taskId);
            String jsonMessage = JSON.toJSONString(jasonMapOnFinished);
            log.info("发送给前端的消息：" + jsonMessage);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMapOnFinished);
        } catch (Exception e) {
            log.error("任务暂停异常: ", e);
        }

        //任务状态上报站端
        UPatrolTask uPatrolTask = uPatrolTaskDao.selectByPrimaryId(taskId);
        sendTaskStateToUp(uPatrolTask, 3);

        return uPatrolResultDao.update(uPatrolResult);
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
    public int taskGoOn(String taskId) throws Exception {
        UPatrolResult uPatrolResult = uPatrolTaskDao.selectForTaskId(taskId);

        //机器人任务继续
        List<String> robotCodeList = uPatrolTaskDao.selectRobotIsRunning(taskId);
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

        updateTaskStateForRedis(taskId, String.valueOf(TASK_STATE_EXECUTING));

        uPatrolResult.setTaskState(TASK_STATE_EXECUTING);
        uPatrolResultDao.update(uPatrolResult);

        localTaskStart(taskId);

        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);

        Map<String, String> jasonMapOnFinished = new HashMap<>();
        jasonMapOnFinished.put("type", "taskChange");
        jasonMapOnFinished.put("taskId", taskId);
        String jsonMessage = JSON.toJSONString(jasonMapOnFinished);
        log.info("发送给前端的消息：" + jsonMessage);
        Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMapOnFinished);

        //任务状态上报站端
        sendTaskStateToUp(task, 2);
        return 1;
    }

    /**
     * 任务终止，异步执行
     */
    @Async
    public void taskShutDown(String taskId) {
        UPatrolResult uPatrolResult = uPatrolResultDao.selectByPrimaryId(taskId);
        if (uPatrolResult.getTaskState() == TASK_STATE_FINISHED) {
            return;
        }
        uPatrolResult.setTaskState(TASK_STATE_INTERRUPT);
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);

        List<String> robotCodeList = uPatrolTaskDao.selectRobotIsRunning(taskId);
        Map<String, Object> robotTaskStatesMap = new HashMap<>();
        robotTaskStatesMap.put("taskId", taskId);
        robotTaskStatesMap.put("commandValue", 4);
        robotTaskStatesMap.put("robotCodeList", robotCodeList);
        robotTaskStates(robotTaskStatesMap);

        updateTaskStateForRedis(taskId, String.valueOf(TASK_STATE_INTERRUPT));
        try {

            Set<String> tasKeys = redisTemplate.keys(PATROL_TASK_PREFIX + taskId + ":*");
            if (CollectionUtils.isEmpty(tasKeys)) {
                log.error("patrol_task_result:{}:* 未查到任务，任务未正确初始化", taskId);
                throw new BusinessException("任务未正确初始化");
            }

            log.info("tasKeys size: {}", tasKeys.size());

            // 暂停15秒等待未接收数据完成接收
            Thread.sleep(15000);

            List<Map<String, String>> taskInfoList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>) connection -> {
                tasKeys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
                return null;
            });

            log.info("taskInfoList size: {}", taskInfoList.size());

            if(taskInfoList.size() != 0) {
                SimpleDateFormat  simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                List<Map<String, String>> skipPointList = new ArrayList<>();
                for (Map<String, String> taskInfo : taskInfoList) {
                    if (MapUtils.isNotEmpty(taskInfo)) {
                        //count = count+1;
                        if (CommonUtils.isEmptyOrNullstr(taskInfo.get("cruiseResult"))) {
                            //任务终止
                            taskInfo.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
                            taskInfo.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_INTERRUPT));
                            taskInfo.put("cruiseStatus", String.valueOf(CRUISE_STATE_UN));
                            taskInfo.put("cruiseTime",simpleDateFormat.format(new Date()));
                            taskInfo.put("resultNum", "任务终止");
                            skipPointList.add(taskInfo);
                        }
                        // todo 任务终止 上报站端
                    }
                }
                log.info("task [{}] shut down, skipPointList: {}", taskId, skipPointList.size());
                ThreadPoolUtil.PATROL_POOL.addThread(new LocalCruiseExecutThread<>(this, skipPointList, true, true, taskId));
            }

            log.info("任务终止成功=={}", taskId);
        } catch (Exception e) {
            log.info("任务终止失败", e);
        }

        lowTaskGoOn(taskId);
        //任务状态上报站端
        // sendTaskStateToUp(task, 4);
        uPatrolResultDao.update(uPatrolResult);
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
                    skipFlag = true;
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
            // webSocket通知前端调用巡视监控的接口
            Map<String, String> jasonMap = new HashMap<>(3);
            jasonMap.put("type", "finishedOneInstance");
            jasonMap.put("taskId", taskId);
            log.info("发送给前端的消息：{}", JSON.toJSONString(jasonMap));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);

            // 巡视结果上报上一级系统
            processResultToUpSystem.alarmAndResultToUpSystem(cruiseResultList, null, null);
            //任务状态上报站端
            sendTaskStateToUp(taskId, 2);
        } catch (IOException e) {
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
                progress = CommonUtils.percentFormat((float)(normalCounts + abnormalCounts)/allCounts, "#.####");
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
        try {
            Thread.sleep(15000);

            int taskStatus;
            int all;
            boolean ended;
            synchronized (LOCK_FLAG) {
                String strForCountAbnormal = PATROL_SUMMARY_PREFIX + taskId;
                Map<String, String> resultCountsMap = redisTemplate.opsForHash().entries(strForCountAbnormal);

                ended = Boolean.parseBoolean(resultCountsMap.get("ended"));

                taskStatus = NumberUtils.toInt(resultCountsMap.get("taskState"), TASK_STATE_FINISHED);
                taskStatus = taskStatus == TASK_STATE_EXECUTING ? TASK_STATE_FINISHED : taskStatus;

                all = MapUtils.getIntValue(resultCountsMap, "all", 0);

                redisTemplate.opsForHash().put(strForCountAbnormal, "ended", "true");
            }
            if (ended) {
                log.warn("taskId is:{} , already ended； {}", taskId, ended);
                return;
            }

            Map<String, String> jasonMap = new HashMap<>(2);
            jasonMap.put("type", "lastOneInstance");
            jasonMap.put("taskId", taskId);
            log.info("最后一个点-前端推送：{}", JSON.toJSONString(jasonMap));
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);

            int abnormalCounts = 0;
            List<UPatrolDataResult> uPatrolDataResultList = new ArrayList<>();
            Set<String> robotInfoKeys = redisScan(PATROL_TASK_PREFIX + taskId);
            List<Map<String, String>> cruiseResultMapList = new ArrayList<>();
            for (String key : robotInfoKeys) {
                Map<String, String> redisInfoMap = redisTemplate.opsForHash().entries(key);
                cruiseResultMapList.add(redisInfoMap);
                boolean conditionRes = ArrayUtils.contains(new String[]{String.valueOf(CRUISE_RESULT_NORMAL), String.valueOf(CRUISE_RESULT_ABNORMAL)}, redisInfoMap.get("cruiseResult"));
                if (conditionRes) {
                    UPatrolDataResult uPatrolDataResult = new UPatrolDataResult();
                    uPatrolDataResult.setTaskId(taskId);
                    uPatrolDataResult.setDeviceId(NumberUtils.toLong(redisInfoMap.get("deviceId")));
                    uPatrolDataResult.setDeviceName(redisInfoMap.get("deviceName"));
                    uPatrolDataResult.setInstanceId(NumberUtils.toLong(redisInfoMap.get("instanceId")));
                    uPatrolDataResult.setInstanceName(redisInfoMap.get("instanceName"));
                    uPatrolDataResult.setCruiseId(NumberUtils.toLong(redisInfoMap.get("cruiseId")));
                    uPatrolDataResult.setCruiseName(redisInfoMap.get("cruiseName"));
                    uPatrolDataResult.setCruiseTime(DateTimeUtil.parse(redisInfoMap.get("cruiseTime")));
                    uPatrolDataResult.setCruiseStatus(NumberUtils.toInt(redisInfoMap.get("cruiseStatus")));
                    uPatrolDataResult.setResultNum(redisInfoMap.get("resultNum"));
                    uPatrolDataResult.setPicpath(redisInfoMap.get("picpath"));
                    uPatrolDataResult.setCruiseType(NumberUtils.toInt(redisInfoMap.get("cruiseType")));
                    uPatrolDataResult.setOrigpic(redisInfoMap.get("origpic"));
                    uPatrolDataResult.setCruiseAbnormal(NumberUtils.toInt(redisInfoMap.get("cruiseAbnormal")));
                    uPatrolDataResult.setEvaluationState(MapUtils.getIntValue(redisInfoMap, "evaluationState", EVALUATION_STATE_UN));
                    uPatrolDataResult.setCreatetime(new Date());
                    uPatrolDataResult.setIsWarn(NumberUtils.toInt(redisInfoMap.get("isWarn")));
                    uPatrolDataResult.setCruiseResult(NumberUtils.toInt(redisInfoMap.get("cruiseResult")));

                    uPatrolDataResultList.add(uPatrolDataResult);
                    if (CRUISE_RESULT_NORMAL != uPatrolDataResult.getCruiseResult()){
                        abnormalCounts++;
                    }
                }
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
            getStationWeather(uPatrolResult);

            uPatrolResultDao.update(uPatrolResult);
            log.info("taskId is:{} , uPatrolDataResultList size is:{}, ended； {}", taskId, uPatrolDataResultList.size(), ended);

            // 如果 ended=true,表示巡视结果已经入库过一次，不再重复入库，但需要修改
            if (CollectionUtils.isNotEmpty(uPatrolDataResultList)){
                batchInsertUPatrolDataResult(uPatrolDataResultList);
                log.info("准备传其他服务的taskId==={}", taskId);
                uPatrolDataResultService.updateCruiseAnalyze(taskId);

                for (UPatrolDataResult up : uPatrolDataResultList){
                    updateIsWarn(taskId, up.getInstanceId(), up.getCruiseDataId());
                }
            }

            //低优先任务继续
            lowTaskGoOn(taskId);

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

    private void getStationWeather(UPatrolResult uPatrolResult){
        String temperature = String.valueOf(redisTemplate.opsForHash().entries("stationWeather:" + "1").getOrDefault("valueUnit", ""));
        String humidity = String.valueOf(redisTemplate.opsForHash().entries("stationWeather:" + "2").getOrDefault("valueUnit", ""));
        String windSpeed = String.valueOf(redisTemplate.opsForHash().entries("stationWeather:" + "3").getOrDefault("valueUnit", ""));
        String precipitation = String.valueOf(redisTemplate.opsForHash().entries("stationWeather:" + "4").getOrDefault("valueUnit", ""));
        String windDirection = String.valueOf(redisTemplate.opsForHash().entries("stationWeather:" + "5").getOrDefault("valueUnit", ""));
        String airPressure = String.valueOf(redisTemplate.opsForHash().entries("stationWeather:" + "6").getOrDefault("valueUnit", ""));
        temperature = StringUtils.isEmpty(temperature) ? "暂无" : temperature;
        humidity = StringUtils.isEmpty(humidity) ? "暂无" : humidity;
        windSpeed = StringUtils.isEmpty(windSpeed) ? "暂无" : windSpeed;
        precipitation = StringUtils.isEmpty(precipitation) ? "暂无" : precipitation;
        windDirection = StringUtils.isEmpty(windDirection) ? "暂无" : windDirection;
        airPressure = StringUtils.isEmpty(airPressure) ? "暂无" : airPressure;
        String weather = "气温:" + temperature + ",湿度:" + humidity + ",风速:" + windSpeed + ",雨量:" + precipitation + ",风向:" + windDirection + ",气压:" + airPressure;
        log.info("====Now the environmental data is {}", weather);
        uPatrolResult.setWeather(weather);
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
        String lowTaskKey = "lowTask:" + taskId;
        List<String> lowTaskList = redisTemplate.opsForList().range(lowTaskKey, 0, -1);
        if (lowTaskList != null && lowTaskList.size() > 0) {
            lowTaskList.forEach(lowTask -> {
                try {
                    taskGoOn(lowTask);
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
        Map<String, Object> reMap = new HashMap<>();
        String taskId = uPatrolTaskDao.selectRobotTaskOnStart(robotId);
        if (!org.springframework.util.StringUtils.isEmpty(taskId)) {
            reMap.put("taskId", taskId);
            Map<String, String> robotOrDroneTaskInfo = redisTemplate.opsForHash().entries(ROBOT_OR_DRONE_TASK+taskId+":"+robotId);
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
                    List<RobotTaskMessage> list = selectRobotTaskMessage(taskId, robotId ,robotOrDroneTaskInfo);
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
        if (!org.springframework.util.StringUtils.isEmpty(state)) {
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

    public List<RobotTaskMessage> selectRobotTaskMessage(String taskId, Long robotId,Map<String, String> mapForRobotState) throws Exception {
        List<RobotTaskMessage> re = new ArrayList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
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

        List<String> instanceIdList = uPatrolTaskDao.selectRobotTaskInstanceList(taskId);
        //List<TCruisePointAttr> nameList =  tRobotInspectionDao.selectRobotTaskMessage(instanceIdList);
        for (String item : instanceIdList) {
            //获取任务数据
            Map<String, String> mapForRobotTaskMessage = redisTemplate.opsForHash().entries(PATROL_TASK_PREFIX + taskId + ":" + item);
            RobotTaskMessage robotTaskMessage = new RobotTaskMessage();
            robotTaskMessage.setDeviceName(mapForRobotTaskMessage.get("deviceName"));
            robotTaskMessage.setInstanceName(mapForRobotTaskMessage.get("instanceName"));
            if (mapForRobotTaskMessage.get("cruiseTime") != null && !"null".equals(mapForRobotTaskMessage.get("cruiseTime"))) {
                robotTaskMessage.setCruiseTime(mapForRobotTaskMessage.get("cruiseTime"));
                robotTaskMessage.setResult(mapForRobotTaskMessage.get("resultNum"));
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
            FtpsUtil.downloadFile(source, target, intelAnalysisFtpsConfig.getIp(), intelAnalysisFtpsConfig.getPort(),
                    intelAnalysisFtpsConfig.getKeypw(), intelAnalysisFtpsConfig.getUsername(), intelAnalysisFtpsConfig.getPassword());
        }catch (Exception e){
            log.error(e.getMessage(), e);
        }
        return "";
    }

    @Transactional(rollbackFor = Exception.class)
    public int upSystemCtrl(XMLBaseModel xmlBaseModel) throws Exception {
        String com = xmlBaseModel.getCommand();
        String code = xmlBaseModel.getCode();
        String taskId = code.contains("_") ? code.split("_").length > 2 ? StringUtils.substringBetween(code, "_") : StringUtils.substringBefore(code, "_") : code;
        log.info("taskId : {} control", taskId);
        switch (com) {
            case "1":
                return this.taskStart(taskId);
            case "2":
                return this.taskPause(taskId);
            case "3":
                return this.taskGoOn(taskId);
            case "4":
                this.taskShutDown(taskId);
                return 1;
            default:
                return -1;
        }
    }
}
