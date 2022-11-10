package com.yjh.platform.module.task.service;

import com.yjh.platform.module.patrol.dao.UPatrolPlanAttrDao;
import com.yjh.platform.module.patrol.entity.UPatrolPlanAttr;
import com.yjh.platform.module.task.entity.TCruisePlanAttr;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;

import java.util.List;
import java.util.Date;

import com.yjh.platform.module.task.entity.TCruisePlanAttrDetail;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-09-04
*/
@Service
public class TCruisePlanAttrService{

    @Autowired
    private TCruisePlanAttrDao tCruisePlanAttrDao;
    @Autowired
    private UPatrolPlanAttrDao uPatrolPlanAttrDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruisePlanAttr tCruisePlanAttr) {
        return this.tCruisePlanAttrDao.insert(tCruisePlanAttr);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long planId) {
        return this.tCruisePlanAttrDao.deleteByPrimaryId(planId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruisePlanAttr tCruisePlanAttr) {
        return this.tCruisePlanAttrDao.update(tCruisePlanAttr);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanAttrDetail> selectByPrimaryId(Long planId) {
//        return this.tCruisePlanAttrDao.selectByPrimaryId(planId);
        return this.uPatrolPlanAttrDao.selectByPrimaryId(planId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanAttr> select(Long planId, Long instanceId, Integer pointType, String areaId, String cruiseRegionIds, Integer exceptionType, Long robotId, String position, Long algorithmId, String inferadAnalyze, String irTempBox, Date createTime, Date updateTime,Integer subType) {
        List<TCruisePlanAttr> tCruisePlanAttrList = tCruisePlanAttrDao.select(planId, instanceId, pointType, areaId, cruiseRegionIds, exceptionType, robotId, position, algorithmId, inferadAnalyze, irTempBox, createTime, updateTime,subType);
        return tCruisePlanAttrList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanAttr> selectByPage(TCruisePlanAttr tCruisePlanAttr) {
        List<TCruisePlanAttr> tCruisePlanAttrList = tCruisePlanAttrDao.selectByPage(tCruisePlanAttr);
        return tCruisePlanAttrList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruisePlanAttr> list) {
        return this.tCruisePlanAttrDao.batchInsert(list);
    }

}

