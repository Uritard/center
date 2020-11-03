package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.TCruiseTaskResultDetail;
import com.yjh.platform.module.task.dao.TCruiseTaskResultDetailDao;

import java.util.List;
import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author czh
* @since 2020-08-25
*/
@Service
public class TCruiseTaskResultDetailService{

    @Autowired
    private TCruiseTaskResultDetailDao tCruiseTaskResultDetailDao;

    @Logs(title = "插入", code = "TCruiseTaskResultDetail",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        return this.tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);
    }

    @Logs(title = "删除", code = "TCruiseTaskResultDetail",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String cruiseResultId) {
        return this.tCruiseTaskResultDetailDao.deleteByPrimaryId(cruiseResultId);
    }

    @Logs(title = "更新", code = "TCruiseTaskResultDetail",content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        return this.tCruiseTaskResultDetailDao.update(tCruiseTaskResultDetail);
    }

    @Logs(title = "主键查询", code = "TCruiseTaskResultDetail",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskResultDetail selectByPrimaryId(String cruiseResultId) {
        return this.tCruiseTaskResultDetailDao.selectByPrimaryId(cruiseResultId);
    }

    @Logs(title = "查询", code = "TCruiseTaskResultDetail",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskResultDetail> select(String cruiseResultId, String taskResultId, Long deviceId, Long instanceId, Date cruiseTime, Date endTime, Integer cruiseStatus, String remark) {
        List<TCruiseTaskResultDetail> tCruiseTaskResultDetailList = tCruiseTaskResultDetailDao.select(cruiseResultId, taskResultId, deviceId, instanceId, cruiseTime, endTime, cruiseStatus, remark);
        return tCruiseTaskResultDetailList;
    }

    @Logs(title = "分页查询", code = "TCruiseTaskResultDetail",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskResultDetail> selectByPage(TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        List<TCruiseTaskResultDetail> tCruiseTaskResultDetailList = tCruiseTaskResultDetailDao.selectByPage(tCruiseTaskResultDetail);
        return tCruiseTaskResultDetailList;
    }

    @Logs(title = "批量插入", code = "TCruiseTaskResultDetail",content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTaskResultDetail> list) {
        return this.tCruiseTaskResultDetailDao.batchInsert(list);
    }

}

