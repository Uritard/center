package com.yjh.platform.module.patrol.service;

import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.entity.TCruisePointInstanceAttr;
import com.yjh.platform.module.patrol.dao.UPatrolPlanAttrDao;
import com.yjh.platform.module.patrol.entity.UPatrolPlanAttr;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.entity.TCruisePlan;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
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
        if (map.containsKey("subType") && StringUtils.isNotEmpty(map.get("subType").toString())) {
            Integer planSubType = Integer.parseInt(String.valueOf(map.get("subType")));
            tCruisePlan.setSubType(planSubType);
        }
        tCruisePlan.setType(planType);
        tCruisePlan.setUpRegionId(map.get("upRegionId") == null ? null : Long.parseLong(map.get("upRegionId").toString()));
        tCruisePlan.setPlanCode(map.get("planCode") == null ? "" : map.get("planCode").toString());
        tCruisePlan.setRobotId(map.get("robotId") == null ? null : Long.parseLong(map.get("robotId").toString()));
        tCruisePlan.setPlanPointTypes(map.get("simulationSteps") == null ? "" : map.get("simulationSteps").toString());//该字段用于操作票初始状态
        if (Objects.isNull(map.get("instanceList"))) return this.tCruisePlanDao.insert(tCruisePlan);
        List<Long> InstanceMapList = (List<Long>) map.get("instanceList");
        //处理操作票数据 新增的操作票不绑定设备以及区域 之前绑定的操作票进行更新
        if (InstanceMapList.size() == 0) {
            return this.tCruisePlanDao.insert(tCruisePlan);
        } else {
            Long planId = dealOperationTicket(tCruisePlan);
            if (planId != null) {
                deleteTCruisePointInstance(planId);
                this.tCruisePlanDao.update(tCruisePlan);
                this.uPatrolPlanAttrDao.deleteByPrimaryId(planId);
            } else {
                this.tCruisePlanDao.insert(tCruisePlan);
            }
        }
//        this.tCruisePlanDao.insert(tCruisePlan);

        Long planId = tCruisePlan.getPlanId();

        List<TCruisePointInstanceAttr> tCruisePointInstanceAttrList = tCruisePointInstanceDao.selectInstanceAttrInfo(InstanceMapList);

        for (TCruisePointInstanceAttr tCruisePointInstanceAttr : tCruisePointInstanceAttrList) {
            UPatrolPlanAttr uPatrolPlanAttr = new UPatrolPlanAttr();
            uPatrolPlanAttr.setPlanId(planId)
                    .setDeviceId(tCruisePointInstanceAttr.getDeviceId())
                    .setDeviceName(tCruisePointInstanceAttr.getDeviceName())
                    .setDeviceMeteId(tCruisePointInstanceAttr.getDeviceMeteId())
                    .setDeviceMeteName(tCruisePointInstanceAttr.getDeviceMeteName())
                    .setInstanceId(tCruisePointInstanceAttr.getInstanceId())
                    .setInstanceName(tCruisePointInstanceAttr.getInstanceName())
                    .setPositionId(tCruisePointInstanceAttr.getCruiseId())
                    .setPositionName(tCruisePointInstanceAttr.getCruiseName())
                    .setPointType(tCruisePointInstanceAttr.getCruiseType())
                    .setRobotId(tCruisePointInstanceAttr.getRobotId());
            uPatrolPlanAttrList.add(uPatrolPlanAttr);
            if (uPatrolPlanAttrList.size()%2000 == 0) {
                uPatrolPlanAttrDao.batchAdd(uPatrolPlanAttrList);
                uPatrolPlanAttrList = new ArrayList<>();
            }
        }
        if (uPatrolPlanAttrList.size() > 0){
            uPatrolPlanAttrDao.batchAdd(uPatrolPlanAttrList);
        }
        return 1;
    }

    /**
     * 处理操作票信息
     *
     * @param tCruisePlan
     * @return planId
     */
    private Long dealOperationTicket(TCruisePlan tCruisePlan) {
        Long planId = null;
        if (!StringUtils.isEmpty(tCruisePlan.getPlanCode())) {
            TCruisePlan orderTCruisePlan = tCruisePlanDao.selectByPlanCode(tCruisePlan.getPlanCode());
            if (Objects.nonNull(orderTCruisePlan)
                    && !StringUtils.isEmpty(orderTCruisePlan.getPlanCode())
                    && orderTCruisePlan.getType() == 456) {
                planId = orderTCruisePlan.getPlanId();
                tCruisePlan.setDeviceId(orderTCruisePlan.getDeviceId());
                tCruisePlan.setPlanId(planId);
            }
        }
        return planId;
    }

    private void deleteTCruisePointInstance(Long planId) {
        List<Long> instanceIdList = uPatrolPlanAttrDao.seletcInsByPlan(planId);
        tCruisePointInstanceDao.deleteByInstanceId(instanceIdList);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long planId) {
        uPatrolPlanAttrDao.deleteByPrimaryId(planId);
        return this.tCruisePlanDao.deleteByPrimaryId(planId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(Map<String, Object> planDetailMap) {
        TCruisePlan tCruisePlan = new TCruisePlan();
        Long planId = Long.valueOf(String.valueOf(planDetailMap.get("planId")));
        tCruisePlan.setPlanId(planId);
        tCruisePlan.setPlanName(String.valueOf(planDetailMap.get("planName")));

        List<Long> instanceList = (List<Long>) planDetailMap.get("instanceList");
        if (instanceList.size() == 0) return ResultCodeEnum.CODE10010.getCode();

        List<TCruisePointInstanceAttr> tCruisePointInstanceAttrList = tCruisePointInstanceDao.selectInstanceAttrInfo(instanceList);
        this.uPatrolPlanAttrDao.deleteByPrimaryId(planId);
        List<UPatrolPlanAttr> uPatrolPlanAttrList = new ArrayList<>();
        for (TCruisePointInstanceAttr tCruisePointInstanceAttr : tCruisePointInstanceAttrList) {
            UPatrolPlanAttr uPatrolPlanAttr = new UPatrolPlanAttr();
            uPatrolPlanAttr.setPlanId(planId)
                    .setDeviceId(tCruisePointInstanceAttr.getDeviceId())
                    .setDeviceName(tCruisePointInstanceAttr.getDeviceName())
                    .setDeviceMeteId(tCruisePointInstanceAttr.getDeviceMeteId())
                    .setDeviceMeteName(tCruisePointInstanceAttr.getDeviceMeteName())
                    .setPositionId(tCruisePointInstanceAttr.getCruiseId())
                    .setInstanceId(tCruisePointInstanceAttr.getInstanceId())
                    .setInstanceName(tCruisePointInstanceAttr.getInstanceName())
                    .setPositionName(tCruisePointInstanceAttr.getCruiseName())
                    .setPointType(tCruisePointInstanceAttr.getCruiseType())
                    .setRobotId(tCruisePointInstanceAttr.getRobotId());
            uPatrolPlanAttrList.add(uPatrolPlanAttr);
            if (uPatrolPlanAttrList.size()%2000 == 0) {
                uPatrolPlanAttrDao.batchAdd(uPatrolPlanAttrList);
                uPatrolPlanAttrList = new ArrayList<>();
            }
        }
        Map<String, Object> map = new HashMap<>();
        map.put("planId", planId);
        map.put("planName", String.valueOf(planDetailMap.get("planName")));
        map.put("type", planDetailMap.get("type"));
        map.put("subType", planDetailMap.get("subType"));
        this.tCruisePlanDao.updateByMap(map);
        if (uPatrolPlanAttrList.size() > 0) {
            return uPatrolPlanAttrDao.batchAdd(uPatrolPlanAttrList);
        } else {
            return 0;
        }
    }


}

