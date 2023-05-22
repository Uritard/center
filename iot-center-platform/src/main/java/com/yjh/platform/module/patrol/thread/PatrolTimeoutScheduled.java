/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.thread;

import com.yjh.platform.common.Constant;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.utils.CommonUtils;
import com.yjh.platform.common.utils.DateTimeUtil;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.patrol.dao.UPatrolResultDao;
import com.yjh.platform.module.patrol.dao.UPatrolTaskDao;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import com.yjh.platform.module.task.entity.TaskSimpleInfo;
import com.yjh.platform.scheduled.ScheduledMapConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.yjh.platform.module.patrol.CruiseConstant.*;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_SUMMARY_PREFIX;
import static com.yjh.platform.module.patrol.service.UPatrolTaskService.PATROL_TASK_PREFIX;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/11/1
 * @since [产品/模块版本] （可选）
 */
@Component
public class PatrolTimeoutScheduled {
    private static final Logger log = LoggerFactory.getLogger(PatrolTimeoutScheduled.class);

    private final RedisTemplate redisTemplate;
    private final UPatrolResultDao uPatrolResultDao;
    private final UPatrolTaskDao uPatrolTaskDao;
    private final UPatrolTaskService uPatrolTaskService;
    HashOperations<String, String, String> hashOperations;

    public PatrolTimeoutScheduled(RedisTemplate redisTemplate, UPatrolResultDao uPatrolResultDao, UPatrolTaskDao uPatrolTaskDao,
        UPatrolTaskService uPatrolTaskService) {
        this.redisTemplate = redisTemplate;
        this.uPatrolResultDao = uPatrolResultDao;
        this.uPatrolTaskDao = uPatrolTaskDao;
        this.uPatrolTaskService = uPatrolTaskService;
        this.hashOperations = redisTemplate.opsForHash();
    }

    @Scheduled(cron = "0 */5 * * * ?")
    public void patrolTimeoutScheduled() {
        log.info("超时判断定时任务");

        String tasksAreTime = hashOperations.get("t_sys_param:tasksAreTime", "content");
        int timeOut = NumberUtils.toInt(tasksAreTime);

        if (!Constant.isHost()) {
            // 边缘节点和上级系统超时时间加长30分钟，避免和巡视主机一致
            timeOut += 30;
        }

        List<TaskSimpleInfo> runningList = uPatrolResultDao.selectTaskIsRunning();

        Date currentDate = new Date();

        for (TaskSimpleInfo task : runningList) {
            String taskId = task.getTaskId();
            String key = PATROL_SUMMARY_PREFIX + taskId;
            Map<String, String> taskMap = hashOperations.entries(key);
            String lastDate = taskMap.get("lastCruiseTime");
            String taskStart = taskMap.get("taskStart");
            int taskState = NumberUtils.toInt(taskMap.get("taskState"), TASK_STATE_EXECUTING);
            Date lastTime = DateTimeUtil.parse(lastDate, taskStart);
            if (currentDate.getTime() - lastTime.getTime() >= timeOut * 60 * 1000L && !ArrayUtils.contains(
                new int[] {TASK_STATE_FINISHED, TASK_STATE_PAUSE, TASK_STATE_INTERRUPT, TASK_STATE_ABNORMAL, TASK_STATE_TIMEOUT}, taskState)) {
                // 设置任务状态超时
                uPatrolTaskService.updateTaskStateForRedis(taskId, String.valueOf(TASK_STATE_TIMEOUT));

                taskTimeout(taskId);
            }
        }

    }

    private void taskTimeout(String taskId) {

        Set<String> tasKeys = redisTemplate.keys(PATROL_TASK_PREFIX + taskId + ":*");
        if (CollectionUtils.isEmpty(tasKeys)) {
            log.error("patrol_task_result:{}:* 未查到任务，任务未正确初始化", taskId);
            throw new BusinessException("任务未正确初始化");
        }

        List<Map<String, String>> taskInfoList = redisTemplate.executePipelined((RedisCallback<Map<String, String>>)connection -> {
            tasKeys.forEach(s -> connection.hGetAll(s.getBytes(StandardCharsets.UTF_8)));
            return null;
        });

        // 超时的点
        List<Map<String, String>> outPointList = new ArrayList<>();
        taskInfoList.forEach(m -> {
            int cruiseStatus = MapUtils.getIntValue(m, "cruiseStatus", CRUISE_STATE_UN);
            String cruiseResult = MapUtils.getString(m, "cruiseResult");
            // 已经执行点位
            if (cruiseStatus != CRUISE_STATE_UN && !CommonUtils.isEmptyOrNullstr(cruiseResult)) {
                return;
            }

            m.put("resultNum", "-1");
            m.put("resultDesc", "超时");
            // 异常原因，超时
            m.put("cruiseAbnormal", String.valueOf(CRUISE_ABNORMAL_TIMEOUT));
            // 巡视结果，异常
            m.put("cruiseResult", String.valueOf(CRUISE_RESULT_ABNORMAL));
            // 未审核
            m.put("evaluationState", String.valueOf(EVALUATION_STATE_UN));
            m.put("picpath", "--");
            // 巡检数据状态，执行遗漏
            m.put("cruiseStatus", String.valueOf(CRUISE_STATE_OMIT));
            String dateTime = DateTimeUtil.getDateTimeString();
            // m.put("createtime", dateTime);
            m.put("endTime", dateTime);
            m.put("cruiseTime", dateTime);

            outPointList.add(m);
        });
        log.info("任务超时处理, task:{}, outPointSize: {}", taskId, outPointList.size());
        // 存入 redis
        CruiseRedisStorage.piplinePutPatrolDetail(outPointList);
        // 更新 PATROL_SUMMARY_PREFIX 并存储
        uPatrolTaskService.patrolTaskResultHandler(outPointList);
        uPatrolTaskService.forceCompletionTask(taskId);

        // 16秒后终止下级任务，延后终止避免与本级超时状态冲突，终止后下级上报任务状态是终止，本级是超时
        ScheduledMapConfig.schedule(16, t -> {
            List<String> robotCodeList = uPatrolTaskDao.selectRobotIsRunning(taskId);
            log.info("下级任务超时终止,robotCodeList:{}", robotCodeList);
            if (robotCodeList != null && !robotCodeList.isEmpty()) {
                Map<String, Object> robotTaskStatesMap = new HashMap<>();
                robotTaskStatesMap.put("taskId", taskId);
                robotTaskStatesMap.put("commandValue", 4);
                robotTaskStatesMap.put("robotCodeList", robotCodeList);
                uPatrolTaskService.robotTaskStates(robotTaskStatesMap);
            }
        });
    }

}
