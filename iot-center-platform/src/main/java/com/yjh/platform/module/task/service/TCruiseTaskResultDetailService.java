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

    @Transactional(rollbackFor = Exception.class)
    public int insert(TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        return this.tCruiseTaskResultDetailDao.insert(tCruiseTaskResultDetail);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(String cruiseResultId) {
        return this.tCruiseTaskResultDetailDao.deleteByPrimaryId(cruiseResultId);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        return this.tCruiseTaskResultDetailDao.update(tCruiseTaskResultDetail);
    }

    @Transactional(rollbackFor = Exception.class)
    public TCruiseTaskResultDetail selectByPrimaryId(String cruiseResultId) {
        return this.tCruiseTaskResultDetailDao.selectByPrimaryId(cruiseResultId);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskResultDetail> select(String cruiseResultId, String taskResultId, Long deviceId,String deviceName, Long instanceId,String instanceName, Date cruiseTime, Date endTime, Integer cruiseStatus, String remark) {
        List<TCruiseTaskResultDetail> tCruiseTaskResultDetailList = tCruiseTaskResultDetailDao.select(cruiseResultId, taskResultId, deviceId,deviceName, instanceId,instanceName, cruiseTime, endTime, cruiseStatus, remark);
        return tCruiseTaskResultDetailList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TCruiseTaskResultDetail> selectByPage(TCruiseTaskResultDetail tCruiseTaskResultDetail) {
        List<TCruiseTaskResultDetail> tCruiseTaskResultDetailList = tCruiseTaskResultDetailDao.selectByPage(tCruiseTaskResultDetail);
        return tCruiseTaskResultDetailList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<TCruiseTaskResultDetail> list) {
        return this.tCruiseTaskResultDetailDao.batchInsert(list);
    }

}

