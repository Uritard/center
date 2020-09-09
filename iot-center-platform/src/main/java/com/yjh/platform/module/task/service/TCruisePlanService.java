package com.yjh.platform.module.task.service;

import com.yjh.platform.common.result.BusinessException;
import com.yjh.platform.common.result.ResultCodeEnum;
import com.yjh.platform.module.device.dao.TRobotInspectionDao;
import com.yjh.platform.module.device.entity.TRobotInspection;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;
import com.yjh.platform.module.task.entity.InstanceTree;
import com.yjh.platform.module.task.entity.TCruisePlan;
import com.yjh.platform.module.task.dao.TCruisePlanDao;

import java.util.*;

import com.yjh.platform.module.task.entity.TCruisePlanAttr;
import com.yjh.platform.module.user.dao.TAlgorithmConfDao;
import com.yjh.platform.module.user.entity.TAlgorithmConf;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

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
    public int insert(List<Object> list) {
        if (list.size()>0) {
            List<TCruisePlanAttr> tCruisePlanAttrList = new ArrayList<>();
            Map<String, String> map = (Map<String, String>) list.get(0);
            TCruisePlan tCruisePlan = new TCruisePlan();
            tCruisePlan.setPlanName(map.get("planName"));
            Integer planType = Integer.parseInt(map.get("planType"));
            tCruisePlan.setType(planType);
            tCruisePlan.setPlanPointTypes(map.get("identifyType"));
            this.tCruisePlanDao.insert(tCruisePlan);
            Long planId = tCruisePlan.getPlanId();
            for (Object object:list) {
                TCruisePlanAttr tCruisePlanAttr = new TCruisePlanAttr();
                tCruisePlanAttr.setPlanId(planId);
                Map<String, String> attrMap = (Map<String, String>) object;
                Long instanceId = Long.valueOf(attrMap.get("instanceId"));
                tCruisePlanAttr.setInstanceId(instanceId);
                Integer pointType = Integer.valueOf(attrMap.get("cruiseType"));
                tCruisePlanAttr.setPointType(pointType);
                switch (tCruisePlanAttr.getPointType()) {
                    case 228:
                        Long cruiseId = Long.valueOf(attrMap.get("cruiseId"));
                        TRobotInspection tRobotInspection = tRobotInspectionDao.selectByPrimaryId(cruiseId);
                        tCruisePlanAttr.setRobotId(tRobotInspection.getRobotId());
                        tCruisePlanAttr.setPosition(attrMap.get("cruiseId"));
                        break;
                    case 229:
                    case 230:
                        Long cruiseAlgorithmId = Long.valueOf(attrMap.get("cruiseId"));
                        TAlgorithmConf tAlgorithmConf = tAlgorithmConfDao.selectByPrimaryId(cruiseAlgorithmId);
                        tCruisePlanAttr.setAlgorithmId(tAlgorithmConf.getAlgorithmId());
                        break;
                    default:
                        break;
                }
                tCruisePlanAttrList.add(tCruisePlanAttr);
            }
            return tCruisePlanAttrDao.batchInsert(tCruisePlanAttrList);
        } else { return ResultCodeEnum.CODE10010.getCode(); }
    }

    @Logs(title = "删除", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long planId) {
        return this.tCruisePlanDao.deleteByPrimaryId(planId);
    }

    @Logs(title = "更新", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruisePlan tCruisePlan) {
        return this.tCruisePlanDao.update(tCruisePlan);
    }

    @Logs(title = "主键查询", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePlan selectByPrimaryId(Long planId) {
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
    public List<TCruisePlan> selectByPage(TCruisePlan tCruisePlan) {
        List<TCruisePlan> tCruisePlanList = tCruisePlanDao.selectByPage(tCruisePlan);
        return tCruisePlanList;
    }

    @Logs(title = "批量插入", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruisePlan> list) {
        return this.tCruisePlanDao.batchInsert(list);
    }

    @Logs(title = "查询巡检点树", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> findInstanceTree() {
        List<InstanceTree> instanceTreeList = tCruisePlanDao.findInstanceTree();
        Map<String, Object> map = new HashMap<>();
        if (instanceTreeList.size()>0) {
            Map<String, List<InstanceTree>> meterListMap = new HashMap<>();
            Map<String, List<InstanceTree>> positionListMap = new HashMap<>();
            Map<String, List<InstanceTree>> defectListMap = new HashMap<>();
            Map<String, List<InstanceTree>> inferadListMap = new HashMap<>();
            Map<String, List<InstanceTree>> voiceListMap = new HashMap<>();
            List<InstanceTree> meterList = new ArrayList<>();
            List<InstanceTree> positionList = new ArrayList<>();
            List<InstanceTree> defectList = new ArrayList<>();
            List<InstanceTree> inferadList = new ArrayList<>();
            List<InstanceTree> voiceList = new ArrayList<>();
            for (InstanceTree instanceTree:instanceTreeList) {
                switch (instanceTree.getIdentifyType()) {
                    case 219:
                        meterList.add(instanceTree);
                        break;
                    case 220:
                        positionList.add(instanceTree);
                        break;
                    case 221:
                        defectList.add(instanceTree);
                        break;
                    case 222:
                        inferadList.add(instanceTree);
                        break;
                    case 223:
                        voiceList.add(instanceTree);
                        break;
                    default:
                        throw new BusinessException("无此巡检类型！");
                }
            }
            meterListMap.put("表计读数", meterList);
            positionListMap.put("位置状态识别", positionList);
            defectListMap.put("外观缺陷识别", defectList);
            inferadListMap.put("红外测温", inferadList);
            voiceListMap.put("声音检测", voiceList);
            List<Object> list = new ArrayList<>();
            list.add(meterListMap);
            list.add(positionListMap);
            list.add(defectListMap);
            list.add(inferadListMap);
            list.add(voiceListMap);
            map.put(instanceTreeList.get(0).getStationName(), list);
        }
        return map;
    }

}

