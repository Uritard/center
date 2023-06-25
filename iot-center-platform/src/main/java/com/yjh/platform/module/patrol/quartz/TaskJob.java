package com.yjh.platform.module.patrol.quartz;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.common.utils.JSONUtil;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskDao;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.dao.TCruiseTaskDelDao;
import com.yjh.platform.module.task.entity.RobotTaskInstanceInfo;
import com.yjh.platform.module.task.entity.TCruiseTaskDel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_SUMMARY_PREFIX;

/**
 * @Author: lqh
 * @Date: 2022/10/12
 */
@PersistJobDataAfterExecution
@DisallowConcurrentExecution
@Configuration
@Slf4j
public class TaskJob extends QuartzJobBean {

    @Autowired
    private UPatrolTaskDao uPatrolTaskDao;
    @Resource
    private TCruiseTaskDelDao tCruiseTaskDelDao;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private UPatrolTaskService uPatrolTaskService;
    @Resource
    private UPatrolResultDao uPatrolResultDao;
    @Resource
    private TRobotInspectionDao tRobotInspectionDao;


    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


    /**
     * 巡视任务类
     *
     * @param context
     */
    @Override
    public void executeInternal(JobExecutionContext context) {

        String taskId = context.getMergedJobDataMap().getString("taskId");
        UPatrolTask ancestralTask = uPatrolTaskDao.selectByPrimaryId(taskId);
        UPatrolTask task = uPatrolTaskDao.selectThisTaskByTaskCode(Optional.ofNullable(ancestralTask).map(UPatrolTask::getTaskCode).orElse("notfound"));
        //任务的计划执行时间
        Date date = context.getScheduledFireTime();
        String taskDate = DateTimeUtil.format(date);
        Date nextTime = new Date();
        Date fireTime = context.getFireTime();
        try {
            Date startTime = context.getTrigger().getStartTime();
            nextTime = context.getNextFireTime();
            List<JobExecutionContext> jobs = context.getScheduler().getCurrentlyExecutingJobs();
            log.info("任务开始执行, taskId: {}, startTime: {}, nextTime: {}, scheduledFireTime: {}, fireTime: {}, jobs: {}", taskId,
                simpleDateFormat.format(startTime), nextTime, taskDate,simpleDateFormat.format(fireTime), jobs.toArray());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        if (task == null || ancestralTask == null){
            log.error("{} 任务初始化记录不存在，任务已经过期或删除", taskId);
            return;
        }

        List<TCruiseTaskDel> tCruiseTaskDelList = tCruiseTaskDelDao.select(taskId, date, null);
        if (task.getExecuteType() == CruiseConstant.TaskTypeEnum.CYCLE.getType()) {
            if (tCruiseTaskDelList != null && tCruiseTaskDelList.size() > 0) {
                //不需要执行任务
                log.info(task.getTaskName() + "在 " + taskDate + " 时间不需要执行");
                return;
            } else if (Objects.nonNull(ancestralTask.getEndTime()) && date.after(ancestralTask.getEndTime())) {
                log.info("此时间大于结束时间,以后都不会再做了,删除当前任务:{}", taskId);
                String edgeLevel = String.valueOf(redisTemplate.opsForHash().get("t_sys_param:edgeLevel", "content"));
                uPatrolTaskService.deleteByPrimaryId(taskId, DateTimeUtil.format(date),edgeLevel, null);
                return;
            } else if (Objects.nonNull(ancestralTask.getStartTime()) && date.before(ancestralTask.getStartTime())) {
                log.info(taskDate + "此时间小于开始时间,任务不需要执行:{}", taskId);
                return;
            }
        }


        List<Long> allInstanceList = uPatrolTaskDao.selectInsByTask(taskId);
        // 如果是一天多个时间点 调度走本级 给下级设备或节点发立即任务
        uPatrolTaskService.taskToRobotOrDroneStart(task, ancestralTask.getDateType(), allInstanceList);

        // 增加下发给下级系统的
        droneTaskStart(task);

        boolean canRunning = true;
        try {
            log.info("当前任务优先级：{}", task.getTaskLevel());
            canRunning =  pauseFormerLowerTask(task);
        } catch (Exception e) {
            log.info("将低优先任务暂停失败:", e);
        }
        log.info("任务 {} 开始执行", task.getTaskName());
        log.info("任务Id {} ", task.getTaskId());
        log.info("任务执行时间 {}, fireTime: {} ", DateTimeUtil.format(new Date()), taskDate);
        log.info("task.getStartTime {} ", task.getStartTime());
        try {
            //更改任务状态
            int taskState = canRunning ? CruiseConstant.TASK_STATE_EXECUTING : CruiseConstant.TASK_STATE_PAUSE;
            setTaskResult(task, fireTime, taskState);
            if (canRunning) {
                //调用摄像机任务
                uPatrolTaskService.localTaskStart(task.getTaskId());
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        //初始化下一次任务信息
        if (task.getExecuteType() == CruiseConstant.TaskTypeEnum.CYCLE.getType() && nextTime.compareTo(ancestralTask.getEndTime()) <= 0) {
            log.info("周期任务初始化下一次任务");
            uPatrolTaskService.initializeNextTaskInfo(ancestralTask, allInstanceList);
        }

    }

    /**
     * 针对普宙无人机无法自启动任务，需要额外发送一条任务启动报文
     *
     * @param task 任务信息
     */
    private void droneTaskStart(UPatrolTask task) {
        log.info("普宙无人机额外发送启动报文：task: {}", JSONUtil.toJSONString(task));

        boolean droneOpen = Boolean.valueOf(redisTemplate.opsForHash().get("t_sys_param:droneOpen", "content").toString());
        if (!droneOpen) {
            // 无人机开关，打开时才需要补充发送任务启动报文
            return;
        }

        List<String> robotCodes = uPatrolTaskDao.selectDroneCodeByTaskCode(task.getTaskCode());
        if (CollectionUtils.isEmpty(robotCodes)) {
            return;
        }

        robotCodes.forEach(robotCode -> {
            try {
                List<String> robotCodeList = new ArrayList<>();
                robotCodeList.add(robotCode);
                Map<String, Object> robotTaskStatesMap = new HashMap<>();
                robotTaskStatesMap.put("taskId", task.getTaskId());
                robotTaskStatesMap.put("commandValue", 1);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                uPatrolTaskService.robotTaskStates(robotTaskStatesMap);
            } catch (Exception e) {
                log.info("额外发送无人机启动任务报错，task: {}", JSONUtil.toJSONString(task));
                log.info("额外发送无人机启动任务报错，err:", e);
            }
        });

    }

    private void setTaskResult(UPatrolTask task, Date date, int taskState) {
        String taskId = task.getTaskId();
        // String realTaskId = uPatrolTaskDao.selectTaskByRobotTaskCode(taskId);
        UPatrolResult result = uPatrolTaskDao.selectForTaskId(taskId);
        log.info("TaskResult update, taskId: {}, result: {}", taskId, JSON.toJSONString(result));
        if (uPatrolTaskService.taskIsEnded(result.getTaskState())) {
            log.error("任务已经结束，不可再次执行，task: {}，taskState: {}", taskId, result.getTaskState());
            return;
        }
        // 周期任务的下一次任务时间和名称在初始化时就通过cron表达式进行计算，不再通过任务执行时再修改名称
        // if (task.getExecuteType() == CruiseConstant.TaskTypeEnum.CYCLE.getType()) {
        //     // 周期任务修改任务名称
        //     result.setTaskName(task.getTaskName());
        // }
        result.setTaskState(taskState);
        //任务开始时间
        result.setExecuteTime(date);
        uPatrolResultDao.update(result);

        uPatrolTaskService.updateTaskStateForRedis(taskId, String.valueOf(taskState));
        // 周期任务的下一次任务时间和名称在初始化时就通过cron表达式进行计算，不再通过任务执行时再修改名称
        // if (task.getExecuteType() == CruiseConstant.TaskTypeEnum.CYCLE.getType()) {
        //     // 周期任务修改任务名称
        //     UPatrolTask utask = new UPatrolTask().setTaskId(taskId).setTaskName(task.getTaskName()).setStartTime(task.getStartTime());
        //     uPatrolTaskDao.update(utask);
        // }
        // 更新 Redis 任务开始时间
        redisTemplate.opsForHash().put(PATROL_SUMMARY_PREFIX + taskId, "taskStart", DateTimeUtil.format(date));
        try {
            Map<String, String> jasonMap = new HashMap<>();
            jasonMap.put("type", "newTask");
            jasonMap.put("taskId", task.getTaskId());
            String json = JSON.toJSONString(jasonMap);
            log.info("发送给前端的消息：   " + json);
            Constant.websocketSendMsg(Constant.WEBSOCKET_URL, jasonMap);
        }catch (Exception e){
            log.info("发送给前端的 newTask 出错：", e);
        }

    }

    /**
     * 判断当前任务之前是否有正在执行的低优先级任务
     * 若存在 则暂停彼任务
     * 判断当前任务之前是否存在高优先级任务，若存在，则暂停当前任务
     *
     * @param task
     */
    private boolean pauseFormerLowerTask(UPatrolTask task) {
        if (task.getTaskLevel() != null) {
            //任务开始前 判断任务优先级 找到优先级比当前任务小的任务
            List<String> lowTaskList = uPatrolTaskDao.selectPlanRunningTask(task.getTaskLevel(), null);

            List<String> highTaskList = uPatrolTaskDao.selectPlanRunningTask(null, task.getTaskLevel());
            if (CollectionUtils.isNotEmpty(highTaskList)) {
                String taskId = task.getTaskId();
                log.info("存在高优先级任务，当前任务暂停，taskId: {}, List：{}", taskId, JSON.toJSONString(highTaskList));
                for (String htId : highTaskList ) {
                    String highKey = UPatrolTaskService.TASK_LOWER_REDIS_KEY + htId;
                    redisTemplate.opsForSet().add(highKey, taskId);
                    redisTemplate.expire(highKey, 3, TimeUnit.DAYS);
                }
                uPatrolTaskService.taskPauseStateAnsy(taskId);
                return false;
            }

            //将此任务暂停的任务放入 redis
            log.info("低优先级任务id：{}", JSON.toJSONString(lowTaskList));
            if (lowTaskList != null && lowTaskList.size() > 0) {
                String lowTaskKey = UPatrolTaskService.TASK_LOWER_REDIS_KEY + task.getTaskId();
                redisTemplate.opsForSet().add(lowTaskKey, lowTaskList.toArray(new String[0]));
                redisTemplate.expire(lowTaskKey, 3, TimeUnit.DAYS);
                //将低优先任务暂停
                lowTaskList.forEach(lowTask -> {
                    uPatrolTaskService.taskPauseWithoutRobot(lowTask);
                });
            }
        }

        return true;
    }

}
