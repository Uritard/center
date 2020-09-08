package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TUnionTask;
import com.yjh.platform.module.task.dao.TUnionTaskDao;

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
public class TUnionTaskService{

    @Autowired
    private TUnionTaskDao tUnionTaskDao;

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TUnionTask tUnionTask) {
        return this.tUnionTaskDao.insert(tUnionTask);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String unionId) {
        return this.tUnionTaskDao.deleteByPrimaryId(unionId);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(TUnionTask tUnionTask) {
        return this.tUnionTaskDao.update(tUnionTask);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public TUnionTask selectByPrimaryId(String unionId) {
        return this.tUnionTaskDao.selectByPrimaryId(unionId);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTask> select(String unionId, Long planId, String areaId, String name, Integer type, Integer ifRun, Long robotId, Integer dateType, Integer remark1, Integer remark2, String remark3, Integer taskType, Date startTime, Date createTime) {
        List<TUnionTask> tUnionTaskList = tUnionTaskDao.select(unionId, planId, areaId, name, type, ifRun, robotId, dateType, remark1, remark2, remark3, taskType, startTime, createTime);
        return tUnionTaskList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<TUnionTask> selectByPage(TUnionTask tUnionTask) {
        List<TUnionTask> tUnionTaskList = tUnionTaskDao.selectByPage(tUnionTask);
        return tUnionTaskList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TUnionTask> list) {
        return this.tUnionTaskDao.batchInsert(list);
    }

}

