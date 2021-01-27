package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.THisTelemeterData;
import com.yjh.platform.module.task.dao.THisTelemeterDataDao;

import java.util.List;
import java.util.Date;

import com.yjh.platform.module.task.entity.TUnionInfo;
import com.yjh.platform.module.task.entity.UnionTaskInfo;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.yjh.platform.common.logs.Logs;
import org.springframework.transaction.annotation.Transactional;

/**
* @author czh
* @since 2020-08-25
*/
@Service
public class THisTelemeterDataService{

    @Autowired
    private THisTelemeterDataDao tHisTelemeterDataDao;

    @Transactional(rollbackFor = Exception.class)
    public int insert(THisTelemeterData tHisTelemeterData) {
        return this.tHisTelemeterDataDao.insert(tHisTelemeterData);
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long id) {
        return this.tHisTelemeterDataDao.deleteByPrimaryId(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public int update(THisTelemeterData tHisTelemeterData) {
        return this.tHisTelemeterDataDao.update(tHisTelemeterData);
    }

    @Transactional(rollbackFor = Exception.class)
    public THisTelemeterData selectByPrimaryId(Long id) {
        return this.tHisTelemeterDataDao.selectByPrimaryId(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<THisTelemeterData> select(Long id,Long meteId, Long deviceId, Date recordTime, Integer meteKind, String meteValue, String lastMeteValue) {
        List<THisTelemeterData> tHisTelemeterDataList = tHisTelemeterDataDao.select(id,meteId, deviceId, recordTime, meteKind, meteValue, lastMeteValue);
        return tHisTelemeterDataList;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<THisTelemeterData> selectByPage(THisTelemeterData tHisTelemeterData) {
        List<THisTelemeterData> tHisTelemeterDataList = tHisTelemeterDataDao.selectByPage(tHisTelemeterData);
        return tHisTelemeterDataList;
    }

    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<THisTelemeterData> list) {
        return this.tHisTelemeterDataDao.batchInsert(list);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<TUnionInfo> selectAll(Date startTime, Date endTime, Integer meteKind, String deviceName,String meteName){
        return this.tHisTelemeterDataDao.selectAll(startTime,endTime,meteKind, deviceName,meteName);
    }


    @Transactional(rollbackFor = Exception.class)
    public List<UnionTaskInfo> selectUnionTask(Date startTime, Date endTime,String deviceName){
        return this.tHisTelemeterDataDao.selectUnionTask(startTime,endTime,deviceName);
    }
}

