package com.yjh.platform.module.task.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.dao.TCruisePlanDao;

import java.util.*;

import com.yjh.platform.module.user.dao.TAlgorithmConfDao;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
* @author tt
* @since 2020-09-07
*/
@Service
public class TCruisePlanService{

    @Autowired
    private TCruisePlanDao tCruisePlanDao;
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    @Autowired
    private TCruisePlanAttrDao tCruisePlanAttrDao;
    @Autowired
    private TAlgorithmConfDao tAlgorithmConfDao;

    @Logs(title = "新增预案", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int insert(Map<String, Object> map) {
        System.out.println("_____________"+map+"________________");
        if (map.size()>0) {
            if (Objects.isNull(map.get("instanceList"))){
                return ResultCodeEnum.CODE10010.getCode();
            }else {
                List<TCruisePlanAttr> tCruisePlanAttrList = new ArrayList<>();
                TCruisePlan tCruisePlan = new TCruisePlan();
                tCruisePlan.setPlanName(String.valueOf(map.get("planName")));
                Integer planType = Integer.parseInt(String.valueOf(map.get("type")));
                tCruisePlan.setType(planType);
                this.tCruisePlanDao.insert(tCruisePlan);
                List<Map<String, Object>> InstanceMapList = (List<Map<String, Object>>) map.get("instanceList");
                Long planId = tCruisePlan.getPlanId();
                for (Map<String, Object> instanceMap:InstanceMapList) {
                    TCruisePlanAttr tCruisePlanAttr = new TCruisePlanAttr();
                    tCruisePlanAttr.setPlanId(planId);
                    Long instanceId = Long.valueOf(String.valueOf(instanceMap.get("instanceId")));
                    tCruisePlanAttr.setInstanceId(instanceId);
                    if (Objects.nonNull(instanceMap.get("cruiseType"))) {
                        Integer cruiseType = Integer.parseInt(String.valueOf(instanceMap.get("cruiseType")));
                        tCruisePlanAttr.setPointType(cruiseType);
                    }
                    if (Objects.nonNull(tCruisePlanAttr.getPointType())) {
                        switch (tCruisePlanAttr.getPointType()) {
                            case 228:
                                TRobotInspection tRobotInspection = tRobotInspectionDao.selectByPrimaryId(instanceId);
                                tCruisePlanAttr.setRobotId(tRobotInspection.getRobotId());
                                tCruisePlanAttr.setPosition(String.valueOf(instanceMap.get("cruiseId")));
                                break;
                            case 229:
                            case 230:
                                Long cruiseAlgorithmId = Long.valueOf(String.valueOf(instanceMap.get("cruiseId")));
                                TAlgorithmConf tAlgorithmConf = tAlgorithmConfDao.selectByPrimaryId(cruiseAlgorithmId);
                                tCruisePlanAttr.setAlgorithmId(tAlgorithmConf.getAlgorithmId());
                                break;
                            default:
                                break;
                        }
                    }
                    tCruisePlanAttrList.add(tCruisePlanAttr);
                }
                return tCruisePlanAttrDao.batchInsert(tCruisePlanAttrList);
            }

        } else{ return ResultCodeEnum.CODE10010.getCode(); }
    }

    @Logs(title = "删除", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long planId) {
        return this.tCruisePlanDao.deleteByPrimaryId(planId);
    }

    @Logs(title = "更新", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int update(Map<String, Object> planDetailMap) {
        TCruisePlan tCruisePlan = new TCruisePlan();
        Long planId = Long.valueOf(String.valueOf(planDetailMap.get("planId")));
        tCruisePlan.setPlanId(planId);
        tCruisePlan.setPlanName(String.valueOf(planDetailMap.get("planName")));
        List<Map<String, Object>> instanceList = (List<Map<String, Object>>) planDetailMap.get("instanceList");
        List<TCruisePlanAttr> tCruisePlanAttrList = new ArrayList<>();
        Date date = new Date();
        for (Map<String, Object> map:instanceList) {
            TCruisePlanAttr tCruisePlanAttr = new TCruisePlanAttr();
            tCruisePlanAttr.setPlanId(planId);
            tCruisePlanAttr.setInstanceId(Long.valueOf(String.valueOf(map.get("instanceId"))));
            tCruisePlanAttr.setUpdateTime(date);
            tCruisePlanAttrList.add(tCruisePlanAttr);
        }
        this.tCruisePlanAttrDao.deleteByPrimaryId(planId);
        this.tCruisePlanDao.update(tCruisePlan);
        if (tCruisePlanAttrList.size()>0){
            return tCruisePlanAttrDao.batchInsert(tCruisePlanAttrList);
        } else {return 0;}
    }

    @Logs(title = "主键查询", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePlanCount selectByPrimaryId(Long planId) {
        return this.tCruisePlanDao.selectByPrimaryId(planId);
    }

    @Logs(title = "查询", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> select(Long planId, String planName, Integer type, String planPointTypes, Date createTime, Date updateTime) {
        List<TCruisePlan> tCruisePlanList = tCruisePlanDao.select(planId, planName, type, planPointTypes, createTime, updateTime);
        return tCruisePlanList;
    }

    @Logs(title = "分页查询", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanCount> selectByPage(TCruisePlan tCruisePlan) {
        List<TCruisePlanCount> tCruisePlanList = tCruisePlanDao.selectByPage(tCruisePlan);
        return tCruisePlanList;
    }

    @Logs(title = "分页查询", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanCountByPage> selectByPlanPage(TCruisePlan tCruisePlan) {
        List<TCruisePlanCountByPage> tCruisePlanList = tCruisePlanDao.selectByPlanPage(tCruisePlan);
        return tCruisePlanList;
    }

    @Logs(title = "批量插入", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruisePlan> list) {
        return this.tCruisePlanDao.batchInsert(list);
    }

    @Logs(title = "查询标准设备下挂巡检点", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public List<InstanceTree> findInstanceTree(String deviceIds) {
        List<Long> deviceIdList = new ArrayList<>();
        if (StringUtils.hasText(deviceIds)) {
            for (String deviceIdTem:deviceIds.split(",")) {
                Long deviceId = Long.parseLong(deviceIdTem);
                deviceIdList.add(deviceId);
            }
        }
        List<InstanceTree> instanceTreeList = tCruisePlanDao.findInstanceTree(deviceIdList);
        return instanceTreeList;
    }

}

