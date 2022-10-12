package com.yjh.platform.module.patrol.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstanceAttr;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.patrol.dao.UPatrolPlanAttrDao;
import com.yjh.platform.module.patrol.entity.UPatrolPlanAttr;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.entity.TCruisePlan;
import com.yjh.platform.module.task.entity.TCruisePlanAttr;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author lqh
 * @since 2022-10-12
 */
@Slf4j
@Service
public class UPatrolPlanAttrService {

    @Autowired
    private UPatrolPlanAttrDao uPatrolPlanAttrDao;
    @Autowired
    private TCruisePlanDao tCruisePlanDao;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;


    @Transactional(rollbackFor = Exception.class)
    public int insert(Map<String, Object> map) {
        log.info("add new plan, map content: " + map);
        if (map.size() == 0) return ResultCodeEnum.CODE10010.getCode();

        List<UPatrolPlanAttr> uPatrolPlanAttrList = new ArrayList<>();
        TCruisePlan tCruisePlan = new TCruisePlan();
        tCruisePlan.setPlanName(String.valueOf(map.get("planName")));
        Integer planType = Integer.parseInt(String.valueOf(map.get("type")));
        tCruisePlan.setType(planType);
        tCruisePlan.setUpRegionId(map.get("upRegionId") == null ? null : Long.parseLong(map.get("upRegionId").toString()));
        tCruisePlan.setPlanCode(map.get("planCode") == null ? "" : map.get("planCode").toString());
        tCruisePlan.setRobotId(map.get("robotId") == null ? null : Long.parseLong(map.get("robotId").toString()));
        tCruisePlan.setPlanPointTypes(map.get("simulationSteps") == null ? "" : map.get("simulationSteps").toString());//该字段用于操作票初始状态
        if (Objects.isNull(map.get("instanceList"))) return this.tCruisePlanDao.insert(tCruisePlan);
        List<Long> InstanceMapList = (List<Long>) map.get("instanceList");
        this.tCruisePlanDao.insert(tCruisePlan);

        Long planId = tCruisePlan.getPlanId();

        List<TCruisePointInstanceAttr> tCruisePointInstanceAttrList = tCruisePointInstanceDao.selectInstanceAttrInfo(InstanceMapList);

        for (TCruisePointInstanceAttr tCruisePointInstanceAttr : tCruisePointInstanceAttrList) {
            UPatrolPlanAttr uPatrolPlanAttr = new UPatrolPlanAttr();
            uPatrolPlanAttr.setPlanId(planId)
                    .setDeviceId(tCruisePointInstanceAttr.getDeviceId())
                    .setDeviceName(tCruisePointInstanceAttr.getDeviceName())
                    .setDeviceMeteId(tCruisePointInstanceAttr.getDeviceMeteId())
                    .setDeviceMeteName(tCruisePointInstanceAttr.getDeviceMeteName())
                    .setPositionId(tCruisePointInstanceAttr.getCruiseId())
                    .setPositionName(tCruisePointInstanceAttr.getCruiseName())
                    .setPointType(tCruisePointInstanceAttr.getCruiseType())
                    .setRobotId(tCruisePointInstanceAttr.getRobotId());
            uPatrolPlanAttrList.add(uPatrolPlanAttr);
        }
        return uPatrolPlanAttrDao.batchAdd(uPatrolPlanAttrList);
    }


}

