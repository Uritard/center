package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.THisSignalData;
import com.yjh.platform.module.task.dao.THisSignalDataDao;

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
public class THisSignalDataService{

    @Autowired
    private THisSignalDataDao tHisSignalDataDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(THisSignalData tHisSignalData) {
        return this.tHisSignalDataDao.insert(tHisSignalData);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long Id) {
        return this.tHisSignalDataDao.deleteByPrimaryId(Id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(THisSignalData tHisSignalData) {
        return this.tHisSignalDataDao.update(tHisSignalData);
    }

    @Transactional(rollbackFor = Exception.class)
    public THisSignalData selectByPrimaryId(Long Id) {
        return this.tHisSignalDataDao.selectByPrimaryId(Id);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<THisSignalData> select(Long Id, Long meteId, Long deviceId, Date recordTime, Integer meteKind, String meteValue, String lastMeteValue) {
        List<THisSignalData> tHisSignalDataList = tHisSignalDataDao.select(Id,meteId, deviceId, recordTime, meteKind, meteValue, lastMeteValue);
        return tHisSignalDataList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<THisSignalData> selectByPage(THisSignalData tHisSignalData) {
        List<THisSignalData> tHisSignalDataList = tHisSignalDataDao.selectByPage(tHisSignalData);
        return tHisSignalDataList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<THisSignalData> list) {
        return this.tHisSignalDataDao.batchInsert(list);
    }

}

