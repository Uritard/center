package com.yjh.platform.module.task.service;

import com.yjh.platform.module.task.entity.THisTelemeterData;
import com.yjh.platform.module.task.dao.THisTelemeterDataDao;

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
public class THisTelemeterDataService{

    @Autowired
    private THisTelemeterDataDao tHisTelemeterDataDao;

    @Logs(title = "插入", code = "THisTelemeterData",content = "根据web传入的参数新增")
    @Transactional(rollbackFor = Exception.class)
    public int insert(THisTelemeterData tHisTelemeterData) {
        return this.tHisTelemeterDataDao.insert(tHisTelemeterData);
    }

    @Logs(title = "删除", code = "THisTelemeterData",content = "根据web传入的参数删除")
    @Transactional(rollbackFor = Exception.class)
    public int deleteByPrimaryId(Long id) {
        return this.tHisTelemeterDataDao.deleteByPrimaryId(id);
    }

    @Logs(title = "更新", code = "THisTelemeterData",content = "根据web传入的参数更新")
    @Transactional(rollbackFor = Exception.class)
    public int update(THisTelemeterData tHisTelemeterData) {
        return this.tHisTelemeterDataDao.update(tHisTelemeterData);
    }

    @Logs(title = "主键查询", code = "THisTelemeterData",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public THisTelemeterData selectByPrimaryId(Long id) {
        return this.tHisTelemeterDataDao.selectByPrimaryId(id);
    }

    @Logs(title = "查询", code = "THisTelemeterData",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<THisTelemeterData> select(Long id,Long meteId, Long deviceId, Date recordTime, Integer meteKind, String meteValue, String lastMeteValue) {
        List<THisTelemeterData> tHisTelemeterDataList = tHisTelemeterDataDao.select(id,meteId, deviceId, recordTime, meteKind, meteValue, lastMeteValue);
        return tHisTelemeterDataList;
    }

    @Logs(title = "分页查询", code = "THisTelemeterData",content = "根据web传入的参数查询")
    @Transactional(rollbackFor = Exception.class)
    public List<THisTelemeterData> selectByPage(THisTelemeterData tHisTelemeterData) {
        List<THisTelemeterData> tHisTelemeterDataList = tHisTelemeterDataDao.selectByPage(tHisTelemeterData);
        return tHisTelemeterDataList;
    }

    @Logs(title = "批量插入", code = "THisTelemeterData",content = "根据web传入的参数批量插入")
    @Transactional(rollbackFor = Exception.class)
    public int batchInsert(List<THisTelemeterData> list) {
        return this.tHisTelemeterDataDao.batchInsert(list);
    }

}

