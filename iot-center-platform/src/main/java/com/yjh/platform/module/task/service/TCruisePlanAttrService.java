package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TCruisePlanAttr;
import com.yjh.platform.module.task.dao.TCruisePlanAttrDao;

import java.util.List;
import java.util.Date;

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

    @Logs(title = "插入", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruisePlanAttr tCruisePlanAttr) {
        return this.tCruisePlanAttrDao.insert(tCruisePlanAttr);
    }

    @Logs(title = "删除", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long planId) {
        return this.tCruisePlanAttrDao.deleteByPrimaryId(planId);
    }

    @Logs(title = "更新", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruisePlanAttr tCruisePlanAttr) {
        return this.tCruisePlanAttrDao.update(tCruisePlanAttr);
    }

    @Logs(title = "主键查询", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public TCruisePlanAttr selectByPrimaryId(Long planId) {
        return this.tCruisePlanAttrDao.selectByPrimaryId(planId);
    }

    @Logs(title = "查询", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanAttr> select(Long planId, Long instanceId, Integer pointType, String areaId, String cruiseRegionIds, Integer exceptionType, Long robotId, String position, Long algorithmId, String inferadAnalyze, String irTempBox, Date createTime, Date updateTime) {
        List<TCruisePlanAttr> tCruisePlanAttrList = tCruisePlanAttrDao.select(planId, instanceId, pointType, areaId, cruiseRegionIds, exceptionType, robotId, position, algorithmId, inferadAnalyze, irTempBox, createTime, updateTime);
        return tCruisePlanAttrList;
    }

    @Logs(title = "分页查询", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruisePlanAttr> selectByPage(TCruisePlanAttr tCruisePlanAttr) {
        List<TCruisePlanAttr> tCruisePlanAttrList = tCruisePlanAttrDao.selectByPage(tCruisePlanAttr);
        return tCruisePlanAttrList;
    }

    @Logs(title = "批量插入", code = "task")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruisePlanAttr> list) {
        return this.tCruisePlanAttrDao.batchInsert(list);
    }

}

