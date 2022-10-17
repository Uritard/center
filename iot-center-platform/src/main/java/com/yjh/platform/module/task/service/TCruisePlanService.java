package com.yjh.platform.module.task.service;

import com.yjh.platform.common.logs.Logs;
import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TCruisePointInstanceDao;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.dao.TStdDevicemeteDao;
import com.yjh.platform.module.device.entity.AreaInfo;
import com.yjh.platform.module.device.entity.TCruisePointInstance;
import com.yjh.platform.module.device.entity.TCruisePointInstanceAttr;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.patrol.dao.UPatrolPlanAttrDao;
import com.yjh.platform.module.patrol.entity.UPatrolPlanAttr;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.dao.TCruisePlanDao;
import com.yjh.platform.module.task.entity.*;
import com.yjh.platform.module.user.dao.TAlgorithmConfDao;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import com.yjh.platform.module.user.entity.TDictBusiness;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
* @author tt
* @since 2020-09-07
*/
@Service
public class TCruisePlanService{

    @Autowired
    private TCruisePlanDao tCruisePlanDao;
    @Autowired
    private TCruisePointInstanceDao tCruisePointInstanceDao;
    @Autowired
    private TRobotInspectionDao tRobotInspectionDao;
    @Autowired
    private TCruisePlanAttrDao tCruisePlanAttrDao;
    @Autowired
    private TAlgorithmConfDao tAlgorithmConfDao;

    @Autowired
    private TStdDevicemeteDao tStdDevicemeteDao;
    @Autowired
    private UPatrolPlanAttrDao uPatrolPlanAttrDao;

    private Logger log = LoggerFactory.getLogger(TCruisePlanService.class);


    @Transactional(rollbackFor = Exception.class)
    public int insert(Map<String, Object> map) {
        log.info("add new plan, map content: "+map);
        if (map.size()==0) return ResultCodeEnum.CODE10010.getCode();

        List<TCruisePlanAttr> tCruisePlanAttrList = new ArrayList<>();
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
        //处理操作票数据 新增的操作票不绑定设备以及区域 之前绑定的操作票进行更新
        if (InstanceMapList.size() == 0) {
            return this.tCruisePlanDao.insert(tCruisePlan);
        } else {
            Long planId = dealOperationTicket(tCruisePlan);
            if (planId != null){
                deleteTCruisePointInstance(planId);
                this.tCruisePlanDao.update(tCruisePlan);
                this.tCruisePlanAttrDao.deleteByPrimaryId(planId);
            }else {
                this.tCruisePlanDao.insert(tCruisePlan);
            }
        }
        Long planId = tCruisePlan.getPlanId();

        List<TCruisePointInstanceAttr> tCruisePointInstanceAttrList = tCruisePointInstanceDao.batchSelectInstanceAttr(InstanceMapList);

        for (TCruisePointInstanceAttr tCruisePointInstanceAttr:tCruisePointInstanceAttrList) {
            TCruisePlanAttr tCruisePlanAttr = new TCruisePlanAttr();
            tCruisePlanAttr.setPlanId(planId);
            if (Objects.nonNull(map.get("subType")) && !Objects.equals(map.get("subType"), "")) {
                Integer subType = Integer.parseInt(String.valueOf(map.get("subType")));
                tCruisePlanAttr.setSubType(subType);
            }
            tCruisePlanAttr.setInstanceId(tCruisePointInstanceAttr.getInstanceId());

            Integer cruiseType = Integer.parseInt(String.valueOf(tCruisePointInstanceAttr.getCruiseType()));
            tCruisePlanAttr.setPointType(cruiseType);

            if (Objects.nonNull(tCruisePlanAttr.getPointType())) {
                switch (tCruisePlanAttr.getPointType()) {
                    case 228:
                        TRobotInspection tRobotInspection = tRobotInspectionDao.selectByPrimaryId(tCruisePointInstanceAttr.getCruiseId());
                        tCruisePlanAttr.setRobotId(tRobotInspection.getRobotId());
                        tCruisePlanAttr.setPosition(String.valueOf(tCruisePointInstanceAttr.getCruiseId()));
                        break;
                    case 229:
                    case 230:
                        Long cruiseAlgorithmId = tCruisePointInstanceAttr.getCruiseId();
                        TAlgorithmConf tAlgorithmConf = tAlgorithmConfDao.selectByPrimaryId(cruiseAlgorithmId);
                        if (Objects.nonNull(tAlgorithmConf)) tCruisePlanAttr.setAlgorithmId(tAlgorithmConf.getAlgorithmId());
                        break;
                    default:
                        break;
                }
            }
            tCruisePlanAttrList.add(tCruisePlanAttr);
        }
        return tCruisePlanAttrDao.batchInsert(tCruisePlanAttrList);
    }

    private void deleteTCruisePointInstance(Long planId){
        List<Long> instanceIdList = tCruisePlanAttrDao.selectByPlanId(planId);
        tCruisePointInstanceDao.deleteByInstanceId(instanceIdList);
    }

    /**
     * 处理操作票信息
     * @param tCruisePlan
     * @return planId
     */
    private Long dealOperationTicket(TCruisePlan tCruisePlan){
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

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long planId) {
        tCruisePlanAttrDao.deleteByPrimaryId(planId);
        return this.tCruisePlanDao.deleteByPrimaryId(planId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(Map<String, Object> planDetailMap) {
        TCruisePlan tCruisePlan = new TCruisePlan();
        Long planId = Long.valueOf(String.valueOf(planDetailMap.get("planId")));
        tCruisePlan.setPlanId(planId);
        tCruisePlan.setPlanName(String.valueOf(planDetailMap.get("planName")));

        List<Long> instanceList = (List<Long>) planDetailMap.get("instanceList");
        if (instanceList.size()==0) return ResultCodeEnum.CODE10010.getCode();

        List<TCruisePointInstanceAttr> tCruisePointInstanceAttrList = tCruisePointInstanceDao.batchSelectInstanceAttr(instanceList);

        List<TCruisePlanAttr> tCruisePlanAttrList = new ArrayList<>();
        Date date = new Date();
        for (TCruisePointInstanceAttr tCruisePointInstanceAttr:tCruisePointInstanceAttrList) {
            TCruisePlanAttr tCruisePlanAttr = new TCruisePlanAttr();
            tCruisePlanAttr.setPlanId(planId);
            tCruisePlanAttr.setInstanceId(tCruisePointInstanceAttr.getInstanceId());
            tCruisePlanAttr.setUpdateTime(date);
            if (Objects.nonNull(planDetailMap.get("subType")) && !Objects.equals(planDetailMap.get("subType"), "")) {
                Integer subType = Integer.parseInt(String.valueOf(planDetailMap.get("subType")));
                tCruisePlanAttr.setSubType(subType);
            }
            tCruisePlanAttrList.add(tCruisePlanAttr);
        }
        this.tCruisePlanAttrDao.deleteByPrimaryId(planId);
//        this.tCruisePlanDao.update(tCruisePlan);

        Map<String, Object> map = new HashMap<>();
        map.put("planId", planId);
        map.put("planName", String.valueOf(planDetailMap.get("planName")));
        this.tCruisePlanDao.updateByMap(map);
        if (tCruisePlanAttrList.size()>0){
            return tCruisePlanAttrDao.batchInsert(tCruisePlanAttrList);
        } else {return 0;}
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruisePlanCount selectByPrimaryId(Long planId) {
        return this.tCruisePlanDao.selectByPrimaryId(planId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> select(Long planId, String planName, Integer type, String planPointTypes, Date createTime, Date updateTime) {
        return tCruisePlanDao.select(planId, planName, type, planPointTypes, createTime, updateTime);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanCount> selectByPage(TCruisePlan tCruisePlan) {
        return tCruisePlanDao.selectByPage(tCruisePlan);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanCountByPage> selectByPlanPage(TCruisePlan tCruisePlan) {
        return tCruisePlanDao.selectByPlanPage(tCruisePlan);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanCountByPage> selectTicketPlanPage(Long deviceId, Long upRegionId, Integer flag) {
        return tCruisePlanDao.selectTicketPlanPage(deviceId, upRegionId, flag);
    }

    @Transactional(rollbackFor = Exception.class)
    public int updateTicket(Map<String, Object> ticketMap){
        if (Optional.ofNullable(ticketMap.get("planId")).isPresent() && !StringUtils.isEmpty(ticketMap.get("planId").toString())){
            String planId = ticketMap.get("planId").toString();
            String deviceId = ticketMap.get("deviceId").toString();
            List<Long> planIdList = Arrays.stream(planId.split(",")).map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(planIdList)){
                planIdList.forEach(p -> {
                    List<Long> instanceId = tCruisePlanAttrDao.selectByPlanId(p);
                    Long id = StringUtils.isEmpty(deviceId) ? null : Long.parseLong(deviceId);
                    instanceId.forEach(i-> tCruisePointInstanceDao.update(new TCruisePointInstance().setInstanceId(i).setDeviceId(id)));
                    tCruisePlanDao.update(new TCruisePlan().setPlanId(p).setDeviceId(id));
                });
            }
        }
        return 1;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruisePlan> list) {
        return this.tCruisePlanDao.batchInsert(list);
    }

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
        return tCruisePlanDao.findInstanceTree(deviceIdList);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<AreaInfo> cruiseTypeTree() {
        List<AreaInfo> upList = new ArrayList<>();
        AreaInfo up = new AreaInfo();
        up.setId(1L);
        up.setLabel("任务类型树");
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
            areaInfo.setUpName("任务类型树");
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

    public List<String> selectByRobotId(Long robotId) {
        return tCruisePlanDao.selectByRobotId(robotId);
    }

    public int deleteByPlanCode(String planCode) {
        TCruisePlan tCruisePlan = tCruisePlanDao.selectByPlanCode(planCode);
        Long planId = tCruisePlan.getPlanId();
        deleteTCruisePointInstance(planId);
        return this.deleteByPrimaryId(planId);
    }

    public List<InstanceTree> queryOperationInstances(String deviceId, Long robotId, Integer type) {
        return tCruisePlanDao.queryOperationInstances(deviceId, robotId, type);
    }
}

