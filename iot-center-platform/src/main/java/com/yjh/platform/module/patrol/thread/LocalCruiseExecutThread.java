/*
 * Copyright (c) 2022 Yijiahe Technology Co., Ltd. All rights reserved.
 */

package com.yjh.platform.module.patrol.thread;

import com.alibaba.fastjson.JSON;
import com.yjh.platform.module.patrol.service.UPatrolTaskService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.yjh.platform.module.patrol.service.CruiseInspectionExecute.*;
import static com.yjh.platform.module.patrol.service.CruiseInspectionExecute.CRUISE_TYPE_ONLINE;

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
            String cruiseResult = m.get("cruiseResult");
            if ("247".equals(m.get("cruiseResult"))) {

            } else {

            }
        });
    }

    private void skipPoint(Map<String, String> inspectionMap){

    }

    private void pointExecut(Map<String, String> inspectionMap){
        // 巡检点类型
        int cruiseType = MapUtils.getIntValue(inspectionMap, "cruiseType");
        switch (cruiseType) {
            case CRUISE_TYPE_VIDEO: // 视频
            case CRUISE_TYPE_INFRARED: // 红外
                String cameraId = MapUtils.getString(m, "cameraId");
                if (StringUtils.isEmpty(cameraId)) {
                    log.error("task {} cruise data has no cameraId, {}", taskId, JSON.toJSONString(m));
                } else {
                    String cameraIp = (String)redisTemplate.opsForHash().get("camera_info:" + cameraId, "cameraIp");
                    cruiseGroup(cruiseGroupMap, cameraIp, m);
                }
                break;
            case CRUISE_TYPE_VOICE: // 声纹
                String cruiseId = MapUtils.getString(m, "cruiseId");
                if (StringUtils.isEmpty(cruiseId)) {
                    log.error("task {} cruise data has no cruiseId, {}", taskId, JSON.toJSONString(m));
                } else {
                    cruiseGroup(cruiseGroupMap, cruiseId, m);
                }
                break;
            case CRUISE_TYPE_ROBOT: // 机器人
            case CRUISE_TYPE_UAV: // 无人机
            case CRUISE_TYPE_ONLINE: // 在线监控
            default:
                log.warn("inspection not execut, cruiseType: {}, map: {}", cruiseType, JSON.toJSONString(inspectionMap));
                break;
        }
    }
}

