/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.service;

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
import com.yjh.platform.module.task.dao.*;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.*;
import com.yjh.platform.module.user.entity.SysUser;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskResult;
import com.yjh.platform.module.patrol.entity.RobotPatrolTaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/11
 * @since [产品/模块版本] （可选）
 */
@Service
public class UPatrolTaskService {

    @Autowired
    private RedisTemplate redisTemplate;

    private Logger log = LoggerFactory.getLogger(UPatrolTaskService.class);

    @Autowired
    private TCruiseTaskDao tCruiseTaskDao;
    @Autowired
    private TCruiseTaskDelDao tCruiseTaskDelDao;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private UPatrolTaskDao uPatrolTaskDao;
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


    //模板图片路径
    private String picModelPath;
    //等待相机转到预置位时间
    private Long waitTime;
    ;
    //jobName
    @Value("${spring.QingHua.jobName}")
    private String jobName;
    /**
     * 变电站编码
     */
    @Value("${station.code}")
    private String stationCode;
    @Value("${taskToRobot}")
    private boolean taskToRobot;
    //任务超期时间
    private Float tasksAreTime;
    //算法接口
    private static final String ALGORITHM_URL = "http://iot-center-accessvideo/analysis/v1/algorithm";
    private static final String DEFECT_URL = "http://iot-center-accessvideo/analysis/v1/defect";
    private static final String ROBOT_TASK_URL = "http://iot-center-accessrobot/robot/v1/taskIssued";


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

        List<TCruisePointInstanceNameDetail> detailList = initializeTaskInfo(instanceList,uPatrolTask);
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
        this.uPatrolTaskDao.add(uPatrolTask);
        this.uPatrolTaskAttrDao.batchAdd(uPatrolTaskAttrs);
        return instanceList;
    }


    private List<TCruisePointInstanceNameDetail> initializeTaskInfo(List<Long> instanceList, UPatrolTask task) {
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
    public void robotPatrolTaskResult(List<RobotPatrolTaskResult> resultList){
        for (RobotPatrolTaskResult robotPatrolTaskResult : resultList){
            // 通过上报的任务id查询巡视主机上的任务id
            String taskCode = robotPatrolTaskResult.getTaskCode();
            String taskId = "selectRealTaskByTaskCode(taskCode)";
            if(StringUtils.isEmpty(taskId)){
                taskId = taskCode;
                log.info("taskId is empty, use taskCode as taskId");
            }
            log.info("taskCode==={},taskId===={}", taskCode, taskId);

            // 结果文件处理及判断结果是否告警
            String ftpImageRelative = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageRelative", "content"));
            String ftpImageAbsolute = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpImageAbsolute", "content"));
            String ftpsFilePath = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:ftpsFilePath", "content"));

            String filePathTemp = new SimpleDateFormat("yyyy/MM/dd").format(new Date()) + "/" + taskId;
            String[] nameArray = robotPatrolTaskResult.getFilePath().split("/");
            String fileName = nameArray[nameArray.length - 1];

            String developAbsoluteUrl = ftpImageAbsolute + "/" + filePathTemp;
            String developRelativeUrl = ftpImageRelative + "/" + filePathTemp;

            String temporaryFilePath = ftpsFilePath + "/" + robotPatrolTaskResult.getFilePath();
            log.info("temporaryFilePath==={}",temporaryFilePath);

            String fileType = robotPatrolTaskResult.getFileType();
            String originFilePath = robotPatrolTaskResult.getOriginFilePath();
            if (StringUtils.isNotEmpty(originFilePath) && StringUtils.equals("1", fileType)){
                // 红外原图
                String[] originNameArray = originFilePath.split("/");
                String infraredOriginName = originNameArray[originNameArray.length - 1];
                String temporaryInfraredOriginPath = ftpsFilePath + "/" + originFilePath;
            }

        }
        return;
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
    private String taskToRobotOrDrone(UPatrolTask task, TCruiseTaskAdd tCruiseTaskAdd, DateFormat format, List<TCruisePointInstanceNameDetail> detailList) {
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
                    intervalExecuteTime = StringUtils.isNotEmpty(intervalExecuteTime) ? intervalExecuteTime.substring(11) : intervalExecuteTime;
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
            jobManager.addCruiseTaskJobAtTime(quartzTask, task.getTaskId());
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
                i = tCruiseTaskDao.countInstance(task.getTaskId());
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
        TCruiseTask tCruiseTask = tCruiseTaskDao.selectByPrimaryId(taskId);
        if (Objects.nonNull(tCruiseTask.getIfRun()) && tCruiseTask.getIfRun() == 172) {
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
                if (Objects.isNull(tCruiseTask.getDateType())) {
                    taskId = tCruiseTask.getTaskCode();
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
//                tCruiseTaskAttrDao.deleteByPrimaryId(taskId);
                tCruiseTaskDelDao.deleteByPrimaryId(taskId);
                return this.tCruiseTaskDao.deleteByPrimaryId(taskId);
            }
        } else if (Objects.nonNull(tCruiseTask.getIfRun()) && tCruiseTask.getIfRun() == 174) {
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
//        tCruiseTaskAttrDao.deleteByPrimaryId(taskId);
        tCruiseTaskDelDao.deleteByPrimaryId(taskId);
        return this.tCruiseTaskDao.deleteByPrimaryId(taskId);
    }
}
