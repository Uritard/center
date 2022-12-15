package com.yjh.platform.module.patrol.quartz;

import com.alibaba.fastjson.JSON;
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
        UPatrolTask task = uPatrolTaskDao.selectThisTaskByTaskCode(taskId);
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

        List<TCruiseTaskDel> tCruiseTaskDelList = tCruiseTaskDelDao.select(taskId, date, null);
        if (task.getExecuteType() == CruiseConstant.TaskTypeEnum.CYCLE.getType()) {
            if (tCruiseTaskDelList != null && tCruiseTaskDelList.size() > 0) {
                //不需要执行任务
                log.info(task.getTaskName() + "在 " + taskDate + " 时间不需要执行");
                return;
            } else if (Objects.nonNull(ancestralTask.getEndTime()) && date.after(ancestralTask.getEndTime())) {
                log.info("此时间大于结束时间,以后都不会再做了,删除当前任务:{}", taskId);
                uPatrolTaskService.deleteByPrimaryId(taskId, DateTimeUtil.format(date));
                return;
            } else if (Objects.nonNull(ancestralTask.getStartTime()) && date.before(ancestralTask.getStartTime())) {
                log.info(taskDate + "此时间小于开始时间,任务不需要执行:{}", taskId);
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
        log.info("任务执行时间 {}, fireTime: {} ", DateTimeUtil.format(new Date()), taskDate);
        log.info("task.getStartTime {} ", task.getStartTime());
        List<Long> allInstanceList = null;
        try {
            allInstanceList = uPatrolTaskDao.selectInsByTask(taskId);
            //更改任务状态
            setTaskResult(task, fireTime);
            //给机器人发任务启动  没考虑下级系统 暂时是考虑了机器人和无人机
            uPatrolTaskService.taskToRobotOrDroneStart(task, ancestralTask.getDateType());
            //调用摄像机任务
            uPatrolTaskService.localTaskStart(task.getTaskId());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        //初始化下一次任务信息
        if (task.getExecuteType() == CruiseConstant.TaskTypeEnum.CYCLE.getType() && nextTime.compareTo(ancestralTask.getEndTime()) <= 0) {
            log.info("周期任务初始化下一次任务");
            uPatrolTaskService.initializeNextTaskInfo(ancestralTask, allInstanceList);
        }

    }

    private void setTaskResult(UPatrolTask task,Date date) {
        String taskId = task.getTaskId();
        // String realTaskId = uPatrolTaskDao.selectTaskByRobotTaskCode(taskId);
        UPatrolResult result = uPatrolTaskDao.selectForTaskId(taskId);
        log.info("TaskResult update, taskId: {}, result: {}", taskId, JSON.toJSONString(result));
        if (ArrayUtils.contains(new int[]{CruiseConstant.TASK_STATE_FINISHED, CruiseConstant.TASK_STATE_INTERRUPT, CruiseConstant.TASK_STATE_ABNORMAL, CruiseConstant.TASK_STATE_TIMEOUT}, result.getTaskState())) {
            log.error("任务已经结束，不可再次执行，task: {}，taskState: {}", taskId, result.getTaskState());
            return;
        }
        // 周期任务的下一次任务时间和名称在初始化时就通过cron表达式进行计算，不再通过任务执行时再修改名称
        // if (task.getExecuteType() == CruiseConstant.TaskTypeEnum.CYCLE.getType()) {
        //     // 周期任务修改任务名称
        //     result.setTaskName(task.getTaskName());
        // }
        result.setTaskState(CruiseConstant.TASK_STATE_EXECUTING);
        //任务开始时间
        result.setExecuteTime(date);
        uPatrolResultDao.update(result);

        uPatrolTaskService.updateTaskStateForRedis(taskId, String.valueOf(CruiseConstant.TASK_STATE_EXECUTING));
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
