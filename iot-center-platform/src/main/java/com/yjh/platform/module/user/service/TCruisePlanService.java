package com.yjh.platform.module.user.service;

import com.yjh.platform.module.user.entity.TCruisePlan;
import com.yjh.platform.module.user.dao.TCruisePlanDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author lqh
* @since 2020-08-19
*/
@Service
public class TCruisePlanService{

    @Autowired
    private TCruisePlanDao tCruisePlanDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruisePlan tCruisePlan) {
        return this.tCruisePlanDao.insert(tCruisePlan);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long planId) {
        return this.tCruisePlanDao.deleteByPrimaryId(planId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruisePlan tCruisePlan) {
        return this.tCruisePlanDao.update(tCruisePlan);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePlan selectByPrimaryId(Long planId) {
        return this.tCruisePlanDao.selectByPrimaryId(planId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> select(Long planId, Long instanceId, Integer pointType, String cruiseRegionIds, Integer exceptionType, Long robotId, String position, Integer algorithmId, String algorithmName, String inferadAnalyze, String irTempBox, Date createTime, Date updateTime) {
        List<TCruisePlan> tCruisePlanList = tCruisePlanDao.select(planId, instanceId, pointType, cruiseRegionIds, exceptionType, robotId, position, algorithmId, algorithmName, inferadAnalyze, irTempBox, createTime, updateTime);
        return tCruisePlanList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlan> selectByPage(TCruisePlan tCruisePlan) {
        List<TCruisePlan> tCruisePlanList = tCruisePlanDao.selectByPage(tCruisePlan);
        return tCruisePlanList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruisePlan> list) {
        return this.tCruisePlanDao.batchInsert(list);
    }

}

