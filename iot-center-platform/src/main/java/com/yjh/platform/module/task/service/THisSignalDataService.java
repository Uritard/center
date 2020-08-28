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

    @Logs(title = "插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int insert(THisSignalData tHisSignalData) {
        return this.tHisSignalDataDao.insert(tHisSignalData);
    }

    @Logs(title = "删除", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long Id) {
        return this.tHisSignalDataDao.deleteByPrimaryId(Id);
    }

    @Logs(title = "更新", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int update(THisSignalData tHisSignalData) {
        return this.tHisSignalDataDao.update(tHisSignalData);
    }

    @Logs(title = "主键查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public THisSignalData selectByPrimaryId(Long Id) {
        return this.tHisSignalDataDao.selectByPrimaryId(Id);
    }

    @Logs(title = "查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<THisSignalData> select(Long Id, Long meteId, Long deviceId, Date recordTime, Integer meteKind, String meteValue, String lastMeteValue) {
        List<THisSignalData> tHisSignalDataList = tHisSignalDataDao.select(Id,meteId, deviceId, recordTime, meteKind, meteValue, lastMeteValue);
        return tHisSignalDataList;
    }

    @Logs(title = "分页查询", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public List<THisSignalData> selectByPage(THisSignalData tHisSignalData) {
        List<THisSignalData> tHisSignalDataList = tHisSignalDataDao.selectByPage(tHisSignalData);
        return tHisSignalDataList;
    }

    @Logs(title = "批量插入", code = "module")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<THisSignalData> list) {
        return this.tHisSignalDataDao.batchInsert(list);
    }

}

