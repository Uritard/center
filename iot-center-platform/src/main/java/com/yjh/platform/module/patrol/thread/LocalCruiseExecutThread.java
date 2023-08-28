/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.thread;

import com.yjh.platform.common.utils.NumberUtil;
import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.service.CruiseExecuteFactory;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.yjh.platform.module.patrol.CruiseConstant.CRUISE_RESULT_ABNORMAL;
import static com.yjh.platform.module.patrol.CruiseConstant.TypeEnum;

/**
 * 任务执行线程
 *
 * @author Chenfei
 * @date 2022/10/18
 * @since [产品/模块版本] （可选）
 */
public class LocalCruiseExecutThread<T> implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(LocalCruiseExecutThread.class);

    private final UPatrolTaskService uPatrolTaskService;
    private final List<Map<String, String>> cruisePointList;
    /**
     * 跳过具体执行逻辑，直接存入缓存
     */
    private final boolean skip;
    /**
     * 强制结束任务
     */
    private final boolean forceStop;
    private String taskId;

    public LocalCruiseExecutThread(UPatrolTaskService uPatrolTaskService, List<Map<String, String>> cruisePoints) {
        this.uPatrolTaskService = uPatrolTaskService;
        this.cruisePointList = cruisePoints;
        this.skip = false;
        this.forceStop = false;
    }

    public LocalCruiseExecutThread(UPatrolTaskService uPatrolTaskService, List<Map<String, String>> cruisePoints, boolean skip) {
        this.uPatrolTaskService = uPatrolTaskService;
        this.cruisePointList = cruisePoints;
        this.skip = skip;
        this.forceStop = false;
    }

    public LocalCruiseExecutThread(UPatrolTaskService uPatrolTaskService, List<Map<String, String>> cruisePoints, boolean skip,
        boolean forceStop, String taskId) {

        this.uPatrolTaskService = uPatrolTaskService;
        this.cruisePointList = cruisePoints;
        this.skip = skip;
        this.forceStop = forceStop;
        this.taskId = taskId;
    }

    @Override
    public void run() {
        //随机暂停 控制调用设备频率
        int waitTime = NumberUtil.getIntRandomNum(0, 10000);
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            log.error("随机暂停失败", e);
        }
        log.warn("cruiseExecutThread start, cruisePoints size: {}, skip: {}, forceStop: {}", CollectionUtils.size(cruisePointList), skip,
            forceStop);

        if (CollectionUtils.isNotEmpty(cruisePointList)) {
            if (skip) {
                skipPointList(cruisePointList);
            } else {
                for (Map<String, String> m : cruisePointList) {
                    int cruiseResult = MapUtils.getIntValue(m, "cruiseResult");
                    if (CRUISE_RESULT_ABNORMAL == cruiseResult) {
                        skipPoint(m);
                    } else {
                        // 执行点位，并判断是否暂停
                        if (!pointExecut(m)) {
                            break;
                        }
                    }
                }
            }
        }
        if (forceStop) {
            if (StringUtils.isEmpty(taskId)) {
                throw new RuntimeException("taskId can not be empty !!!");
            }
            uPatrolTaskService.forceCompletionTask(taskId);
        }
        log.info("cruiseExecutThread start, size: {}", cruisePointList.size());

    }

    private void skipPointList(List<Map<String, String>> inspectionMapList) {
        CruiseRedisStorage.piplinePutPatrolDetail(inspectionMapList);
        uPatrolTaskService.patrolTaskResultHandler(inspectionMapList);
    }

    private void skipPoint(Map<String, String> inspectionMap) {
        CruiseRedisStorage.offer(inspectionMap);
        uPatrolTaskService.patrolTaskResultHandler(inspectionMap);
    }

    private boolean pointExecut(Map<String, String> inspectionMap) {
        String taskId = inspectionMap.get("taskId");
        long insId = MapUtils.getLongValue(inspectionMap, "instanceId");

        String taskStatus = uPatrolTaskService.taskStatus(taskId);
        if (NumberUtils.toInt(taskStatus, CruiseConstant.TASK_STATE_EXECUTING) != CruiseConstant.TASK_STATE_EXECUTING) {
            log.warn("任务非进行时，taskId: {}, instanceId: {}， taskStatus: {}", taskId, insId, taskStatus);
            // 任务非进行时，停止执行
            return false;
        }

        // 巡检点类型
        int cruiseType = MapUtils.getIntValue(inspectionMap, "cruiseType");
        CruiseConstant.TypeEnum cruiseTypeEnum = TypeEnum.getEnum(cruiseType);
        log.info("巡检执行，cruiseType: {}, cruiseTypeEnum: {}", cruiseType, cruiseTypeEnum);
        CruiseInspectionExecute execute = CruiseExecuteFactory.CREATE.createExecute(cruiseTypeEnum);
        boolean isEnded = execute.execute(inspectionMap);

        // 任务结束，调用结束方法
        if (isEnded) {
            log.info("当前点位结束，isEnded: {}", isEnded);
            uPatrolTaskService.patrolTaskResultHandler(inspectionMap);
        }
        return true;
    }
}

