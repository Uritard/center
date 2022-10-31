/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.thread;

import com.yjh.platform.module.patrol.CruiseConstant;
import com.yjh.platform.module.patrol.service.CruiseExecuteFactory;
import com.yjh.platform.module.patrol.service.CruiseInspectionExecute;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.yjh.platform.module.patrol.CruiseConstant.CRUISE_RESULT_ABNORMAL;
import static com.yjh.platform.module.patrol.CruiseConstant.TypeEnum;

/**
 * <功能描述>
 *
 * @author Chenfei
 * @date 2022/10/18
 * @since [产品/模块版本] （可选）
 */
public class LocalCruiseExecutThread<T> implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(LocalCruiseExecutThread.class);

    private final UPatrolTaskService uPatrolTaskService;
    private final List<Map<String, String>> cruisePointList;
    private final boolean skip;

    public LocalCruiseExecutThread(UPatrolTaskService uPatrolTaskService, List<Map<String, String>> cruisePoints, boolean skip) {
        this.uPatrolTaskService = uPatrolTaskService;
        this.cruisePointList = cruisePoints;
        this.skip = skip;
    }

    @Override
    public void run() {
        if (CollectionUtils.isEmpty(cruisePointList)) {
            log.warn("cruisePoints is empty !!!");
            return;
        }

        if (skip) {
            skipPointList(cruisePointList);
        } else {
            cruisePointList.forEach(m -> {
                int cruiseResult = MapUtils.getIntValue(m, "cruiseResult");
                if (CRUISE_RESULT_ABNORMAL == cruiseResult) {
                    skipPoint(m);
                } else {
                    pointExecut(m);
                }
            });
        }

    }

    private void skipPointList(List<Map<String, String>> inspectionMapList) {
        CruiseRedisStorage.piplinePutPatrolDetail(inspectionMapList);
    }

    private void skipPoint(Map<String, String> inspectionMap) {
        CruiseRedisStorage.offer(inspectionMap);
    }

    private void pointExecut(Map<String, String> inspectionMap) {
        // 巡检点类型
        int cruiseType = MapUtils.getIntValue(inspectionMap, "cruiseType");
        CruiseConstant.TypeEnum cruiseTypeEnum = TypeEnum.getEnum(cruiseType);
        CruiseInspectionExecute execute = CruiseExecuteFactory.CREATE.createExecute(cruiseTypeEnum);
        boolean isEnded = execute.execute(inspectionMap);

        String taskId = inspectionMap.get("taskId");
        long insId = MapUtils.getIntValue(inspectionMap, "instanceId");
        // 上传上级系统
        execute.sendTaskUpSystem(inspectionMap);
        // 发送页面
        execute.sendWebsocket(taskId);
        // 任务结束，调用结束方法
        if (isEnded) {
            uPatrolTaskService.patrolTaskResultHandler(taskId, insId);
        }
    }
}

