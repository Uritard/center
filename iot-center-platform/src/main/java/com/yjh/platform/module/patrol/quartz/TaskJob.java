package com.yjh.platform.module.patrol.quartz;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.logs.SpringBeanUtils;
import com.yjh.platform.common.restTemplate.ServiceRestTemplate;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskDao;
import com.yjh.platform.module.patrol.entity.UPatrolResult;
import com.yjh.platform.module.patrol.entity.UPatrolTask;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.dao.TCruiseTaskDelDao;
import com.yjh.platform.module.task.entity.TCruiseTaskDel;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.PersistJobDataAfterExecution;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

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
        UPatrolTask task = uPatrolTaskDao.selectByPrimaryId(taskId);
        //任务的执行时间
        Date date = context.getFireTime();
        String taskDate = simpleDateFormat.format(context.getFireTime());

        try {
            Date startTime = context.getTrigger().getStartTime();
            Date fireTime = context.getScheduledFireTime();
            List<JobExecutionContext> jobs = context.getScheduler().getCurrentlyExecutingJobs();
            log.info("任务开始执行, taskId: {}, startTime: {}, scheduledFireTime: {}, fireTime: {}, jobs: {}", taskId,
                simpleDateFormat.format(startTime), simpleDateFormat.format(fireTime), taskDate, jobs.toArray());
        } catch (SchedulerException e) {
            log.error(e.getMessage(), e);
        }

        List<TCruiseTaskDel> tCruiseTaskDelList = tCruiseTaskDelDao.select(taskId, date, null);
        if (task.getExecuteType() == CruiseConstant.TaskTypeEnum.CYCLE.getType()) {
            if (tCruiseTaskDelList != null && tCruiseTaskDelList.size() > 0) {
                //不需要执行任务
                log.info(task.getTaskName() + "在 " + taskDate + " 时间不需要执行");
                return;
            } else if (Objects.nonNull(task.getEndTime()) && date.after(task.getEndTime())) {
                log.info("此时间大于结束时间,以后都不会再做了,删除当前任务");
                uPatrolTaskService.deleteByPrimaryId(taskId, DateTimeUtil.format(date));
            } else if (Objects.nonNull(task.getStartTime()) && date.before(task.getStartTime())) {
                log.info(taskDate + "此时间小于开始时间,任务不需要执行");
                return;
            }
        }

        try {
            log.info("当前任务优先级：{}", task.getTaskLevel());
            pauseFormerLowerTask(taskId, task);
        } catch (Exception e) {
            log.info("将低优先任务暂停失败:", e);
        }
        log.info("任务 {} 开始执行", task.getTaskName());
        log.info("任务Id {} ", task.getTaskId());
        log.info("任务执行时间 {}, fireTime: {} ", new Date(), taskDate);
        log.info("task.getStartTime {} ", task.getStartTime());
        List<Long> allInstanceList = uPatrolTaskDao.selectInsByTask(taskId);
        initializeThisTaskInfo(task, allInstanceList);
        //初始化下一次任务信息
        initializeNextTaskInfo(task, allInstanceList);
        //更改任务状态
        setTaskResult(taskId);
        //给机器人发任务启动
        robotTaskStart(task);
        //调用摄像机任务
        uPatrolTaskService.localTaskStart(taskId);


    }

    private void setTaskResult(String taskId) {
        String realTaskId = uPatrolTaskDao.selectTaskByRobotTaskCode(taskId);
        UPatrolResult result = uPatrolTaskDao.selectForTaskId(realTaskId);
        result.setTaskState(CruiseConstant.TASK_STATE_EXECUTING);
        //任务开始时间
        Date date = result.getCreateTime();
        result.setExecuteTime(date);
        uPatrolResultDao.update(result);
    }

    /**
     * 判断当前任务之前是否有正在执行的低优先级任务
     * 若存在 则暂停彼任务
     *
     * @param taskId
     */
    private void pauseFormerLowerTask(String taskId, UPatrolTask task) {
        if (task.getPlanId() != null && task.getTaskLevel() != null) {
            //任务开始前 判断任务优先级 找到优先级比当前任务小的任务
            List<String> lowTaskList = uPatrolTaskDao.selectPlanRunningTask(task.getTaskLevel());
            //将此任务暂停的任务放入 redis
            log.info("低优先级任务id：{}", lowTaskList);
            if (lowTaskList != null && lowTaskList.size() > 0) {
                String lowTaskKey = "lowTask:" + taskId;
                redisTemplate.opsForList().leftPushAll(lowTaskKey, lowTaskList);
                //将低优先任务暂停
                lowTaskList.forEach(lowTask -> {
                    uPatrolTaskService.taskPauseWithoutRobot(lowTask);
                });
            }
        }
    }

    private void initializeThisTaskInfo(UPatrolTask task, List<Long> instanceList) {

        Map<String, String> mapForAbnormal = new HashMap<>();
        mapForAbnormal.put("all", String.valueOf(instanceList.size()));
        mapForAbnormal.put("abnormal", "0");
        mapForAbnormal.put("normal", "0");
        Date taskStart = new Date();
        //任务超期时间
        Map<String, String> mapForTaskAreTime = redisTemplate.opsForHash().entries("t_sys_param:tasksAreTime");
        Integer tasksAreTime = Integer.valueOf(mapForTaskAreTime.get("content"));

        Calendar cal = Calendar.getInstance();
        cal.setTime(taskStart);
        cal.add(Calendar.MINUTE, tasksAreTime);
        Date taskAre = cal.getTime();

        mapForAbnormal.put("taskStart", simpleDateFormat.format(taskStart));
        mapForAbnormal.put("overDay", simpleDateFormat.format(taskAre));
        mapForAbnormal.put("taskState", String.valueOf(CruiseConstant.TASK_STATE_EXECUTING));

        String strForCountAbnormal = "countForAbnormal:" + task.getTaskId();
        redisTemplate.opsForHash().putAll(strForCountAbnormal, mapForAbnormal);
    }

    /**
     * 周期任务初始化下一次的任务信息
     */
    private void initializeNextTaskInfo(UPatrolTask task, List<Long> instanceList) {
        if (task.getExecuteType() == CruiseConstant.TaskTypeEnum.CYCLE.getType()) {
            UPatrolTask nextTask = new UPatrolTask();
            String newTaskId = String.valueOf(UUID.randomUUID()).replace("-", "");
            nextTask.setTaskId(newTaskId)
                    .setTaskCode(task.getTaskCode())
                    .setTaskName(task.getTaskName())
                    .setPlanId(task.getPlanId())
                    .setAreaId(task.getAreaId())
                    .setTaskType(task.getTaskType())
                    .setExecuteType(task.getExecuteType())
                    .setRobotId(task.getRobotId())
                    .setDateType(task.getDateType())
                    .setTaskSource(task.getTaskSource())
                    .setTaskLevel(task.getTaskLevel())
                    .setStartTime(task.getStartTime())
                    .setEndTime(task.getEndTime())
                    .setCreateUserId(task.getCreateUserId());
            uPatrolTaskService.initializeTaskInfo(instanceList, nextTask);
        }
    }

    /**
     * 机器人任务启动
     */
    private void robotTaskStart(UPatrolTask task) {
        try {
            List<String> robotCodeList = tRobotInspectionDao.selectRobotIsRunning(task.getTaskId());
            log.info("机器人任务启动,robotCodeList:{}", robotCodeList);
            if (robotCodeList != null && robotCodeList.size() > 0) {
                Map<String, Object> robotTaskStatesMap = new HashMap<>();
                robotTaskStatesMap.put("taskId", task.getTaskId());
                robotTaskStatesMap.put("commandValue", 1);
                robotTaskStatesMap.put("isEdge", 0);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                robotTaskStates(robotTaskStatesMap);
            }
        } catch (Exception e) {
            log.error("发送机器人启动错误：", e);
        }

    }

    private void robotTaskStates(Map<String, Object> robotTaskStatesMap) {
        try {
            ServiceRestTemplate serviceRestTemplate = SpringBeanUtils.getBean("serviceRestTemplate", ServiceRestTemplate.class);
            if (null != serviceRestTemplate) {
                serviceRestTemplate.postForObject(Constant.ROBOT_TASK_STATUS_URL, robotTaskStatesMap, String.class);
            }
        } catch (Exception e) {
            log.error("调用机器人服务出错：", e);
        }
    }

}
