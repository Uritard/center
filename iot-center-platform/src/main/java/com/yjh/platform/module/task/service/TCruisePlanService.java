package com.yjh.platform.module.task.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.Result;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.task.dao.TCruisePlanDao;

import java.util.*;

import com.yjh.platform.module.user.dao.TAlgorithmConfDao;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import com.yjh.platform.module.user.entity.TDictBusiness;
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

    @Logs(title = "新增预案", code = "task",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(Map<String, Object> map) {
        System.out.println("_____________"+map+"________________");
        if (map.size()>0) {
            List<Map<String, Object>> InstanceMapList = (List<Map<String, Object>>) map.get("instanceList");
            if (InstanceMapList.size()==0){
                return ResultCodeEnum.CODE10010.getCode();
            }else {
                List<TCruisePlanAttr> tCruisePlanAttrList = new ArrayList<>();
                TCruisePlan tCruisePlan = new TCruisePlan();
                tCruisePlan.setPlanName(String.valueOf(map.get("planName")));
                Integer planType = Integer.parseInt(String.valueOf(map.get("type")));
                tCruisePlan.setType(planType);
                this.tCruisePlanDao.insert(tCruisePlan);
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

    @Logs(title = "删除", code = "task",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long planId) {
        tCruisePlanAttrDao.deleteByPrimaryId(planId);
        return this.tCruisePlanDao.deleteByPrimaryId(planId);
    }

    @Logs(title = "更新", code = "task",content = "根据web传入的参数更新")
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
            if (Objects.nonNull(map.get("subType"))) tCruisePlanAttr.setSubType(Integer.parseInt(String.valueOf(map.get("subType"))));
            tCruisePlanAttrList.add(tCruisePlanAttr);
        }
        this.tCruisePlanAttrDao.deleteByPrimaryId(planId);
        this.tCruisePlanDao.update(tCruisePlan);
        if (tCruisePlanAttrList.size()>0){
            return tCruisePlanAttrDao.batchInsert(tCruisePlanAttrList);
        } else {return 0;}
    }

    @Logs(title = "主键查询", code = "task",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePlanCount selectByPrimaryId(Long planId) {
        return this.tCruisePlanDao.selectByPrimaryId(planId);
    }

    @Logs(title = "查询", code = "task",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> select(Long planId, String planName, Integer type, String planPointTypes, Date createTime, Date updateTime) {
        List<TCruisePlan> tCruisePlanList = tCruisePlanDao.select(planId, planName, type, planPointTypes, createTime, updateTime);
        return tCruisePlanList;
    }

    @Logs(title = "分页查询", code = "task",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanCount> selectByPage(TCruisePlan tCruisePlan) {
        List<TCruisePlanCount> tCruisePlanList = tCruisePlanDao.selectByPage(tCruisePlan);
        return tCruisePlanList;
    }

    @Logs(title = "分页查询", code = "task",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanCountByPage> selectByPlanPage(TCruisePlan tCruisePlan) {
        List<TCruisePlanCountByPage> tCruisePlanList = tCruisePlanDao.selectByPlanPage(tCruisePlan);
        return tCruisePlanList;
    }

    @Logs(title = "批量插入", code = "task",content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruisePlan> list) {
        return this.tCruisePlanDao.batchInsert(list);
    }

    @Logs(title = "查询标准设备下挂巡检点", code = "task",content = "查询设备下的巡检点")
    @Transactional(rollbackFor = Exception.class)
    public List<InstanceTree> findInstanceTree(String deviceIds) {
        List<Long> deviceIdList = new ArrayList<>();
        if (StringUtils.hasText(deviceIds)) {
            for (String deviceIdTem:deviceIds.split(",")) {
                Long deviceId = Long.parseLong(deviceIdTem);
                deviceIdList.add(deviceId);
            }
        }
//        System.out.print("&&&&&&&&&&&&*"+deviceIdList+"*****************");
        List<InstanceTree> instanceTreeList = tCruisePlanDao.findInstanceTree(deviceIdList);
        return instanceTreeList;
    }

    @Logs(title = "查询巡视类型树", code = "task",content = "查询巡视类型树")
    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> cruiseTypeTree() {
        List<AreaInfo> upList = new ArrayList<>();
        AreaInfo up = new AreaInfo();
        up.setId(1L);
        up.setLabel("巡视类型树");
        up.setInfoType("cruiseType");
        List<AreaInfo> cruiseTypeTree = new ArrayList<>();
        List<TDictBusiness> cruiseTypeList = this.tCruisePlanDao.selectCruiseType();
        for (TDictBusiness item: cruiseTypeList) {
            if("218".equals(item.getDictCode())){continue;}
            AreaInfo areaInfo = new AreaInfo();
            areaInfo.setId(Long.valueOf(item.getDictCode()));
            areaInfo.setLabel(item.getDictNote());
            areaInfo.setInfoType(item.getColName());
            areaInfo.setUpId(1L);
            areaInfo.setUpName("巡视类型树");
            cruiseTypeTree.add(areaInfo);
        }
        for (AreaInfo areaInfoItem: cruiseTypeTree) {
            List<TDictBusiness> cruiseTypeChildList = this.tCruisePlanDao.selectCruiseTypeChild(areaInfoItem.getId().toString());
            if(cruiseTypeChildList == null || cruiseTypeChildList.size() == 0){
                continue;
            }
            List<AreaInfo> cruiseTypeChild = new ArrayList<>();
            for (TDictBusiness childItem: cruiseTypeChildList) {
                if("218".equals(childItem.getDictCode())){continue;}
                AreaInfo areaInfo = new AreaInfo();
                areaInfo.setId(Long.valueOf(childItem.getDictCode()));
                areaInfo.setLabel(childItem.getDictNote());
                areaInfo.setInfoType(childItem.getColName());
                areaInfo.setUpId(areaInfoItem.getId());
                areaInfo.setUpName(areaInfoItem.getLabel());
                cruiseTypeChild.add(areaInfo);
            }
            areaInfoItem.setChildren(cruiseTypeChild);
        }
        up.setChildren(cruiseTypeTree);
        upList.add(up);
        return upList;
    }
}

