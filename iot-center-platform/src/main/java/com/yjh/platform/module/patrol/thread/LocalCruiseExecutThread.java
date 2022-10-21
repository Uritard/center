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

    public LocalCruiseExecutThread(UPatrolTaskService uPatrolTaskService, List<Map<String, String>> cruisePoints) {
        this.uPatrolTaskService = uPatrolTaskService;
        this.cruisePointList = cruisePoints;
    }

    @Override
    public void run() {
        if (CollectionUtils.isEmpty(cruisePointList)) {
            log.warn("cruisePoints is empty !!!");
            return;
        }

        cruisePointList.forEach(m -> {
            int cruiseResult = MapUtils.getIntValue(m, "cruiseResult");
            if (CRUISE_RESULT_ABNORMAL == cruiseResult) {
                skipPoint(m);
            } else {
                pointExecut(m);
            }
        });
    }

    private void skipPoint(Map<String, String> inspectionMap) {

    }

    private void pointExecut(Map<String, String> inspectionMap) {
        // 巡检点类型
        int cruiseType = MapUtils.getIntValue(inspectionMap, "cruiseType");
        CruiseConstant.TypeEnum cruiseTypeEnum = TypeEnum.getEnum(cruiseType);
        CruiseInspectionExecute execute = CruiseExecuteFactory.CREATE.createExecute(cruiseTypeEnum);
        execute.execute(inspectionMap);
    }
}

