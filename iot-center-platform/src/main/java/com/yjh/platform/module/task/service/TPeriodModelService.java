package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TPeriodModel;
import com.yjh.platform.module.task.dao.TPeriodModelDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author tt
* @since 2020-09-16
*/
@Service
public class TPeriodModelService{

    @Autowired
    private TPeriodModelDao tPeriodModelDao;

    @Logs(title = "插入", code = "task",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TPeriodModel tPeriodModel) {
        return this.tPeriodModelDao.insert(tPeriodModel);
    }

    @Logs(title = "删除", code = "task",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long periodId) {
        return this.tPeriodModelDao.deleteByPrimaryId(periodId);
    }

    @Logs(title = "更新", code = "task",content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(TPeriodModel tPeriodModel) {
        return this.tPeriodModelDao.update(tPeriodModel);
    }

    @Logs(title = "主键查询", code = "task",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public TPeriodModel selectByPrimaryId(Long periodId) {
        return this.tPeriodModelDao.selectByPrimaryId(periodId);
    }

    @Logs(title = "查询", code = "task",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TPeriodModel> select(Long periodId, String cronExpression, String remark, Date updateTime, Date createTime) {
        List<TPeriodModel> tPeriodModelList = tPeriodModelDao.select(periodId, cronExpression, remark, updateTime, createTime);
        return tPeriodModelList;
    }

    @Logs(title = "分页查询", code = "task",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TPeriodModel> selectByPage(TPeriodModel tPeriodModel) {
        List<TPeriodModel> tPeriodModelList = tPeriodModelDao.selectByPage(tPeriodModel);
        return tPeriodModelList;
    }

    @Logs(title = "批量插入", code = "task",content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TPeriodModel> list) {
        return this.tPeriodModelDao.batchInsert(list);
    }

}

